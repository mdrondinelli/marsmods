package me.mar.foodspoilage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class SpoilageEvents {
    private static final int TOUCH_INTERVAL_TICKS = 20;
    private static final int POISON_DURATION_TICKS = 15 * 20;
    private static final int NAUSEA_DURATION_TICKS = 30 * 20;
    private static final int WEAKNESS_DURATION_TICKS = 90 * 20;
    private static final int SLOWNESS_DURATION_TICKS = 60 * 20;
    private static final int HUNGER_DURATION_TICKS = 30 * 20;

    @SubscribeEvent
    public void touchPlayerInventory(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % TOUCH_INTERVAL_TICKS != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        touchInventory(level, player.getInventory());
        if (player.containerMenu != player.inventoryMenu) {
            touchMenu(level, player.containerMenu);
        }
    }

    @SubscribeEvent
    public void touchOpenedContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            touchMenu((ServerLevel) player.level(), event.getContainer());
        }
    }

    @SubscribeEvent
    public void touchJoinedItemEntity(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity itemEntity
                && event.getLevel() instanceof ServerLevel level) {
            SpoilageService.applyRate(level, itemEntity.getItem(), 1.0f);
        }
    }

    @SubscribeEvent
    public void touchTossedItem(ItemTossEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, event.getEntity().getItem());
        }
    }

    @SubscribeEvent
    public void touchGroundItem(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % TOUCH_INTERVAL_TICKS == 0
                && event.getEntity() instanceof ItemEntity itemEntity
                && itemEntity.level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, itemEntity.getItem());
        }
    }

    @SubscribeEvent
    public void touchCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, event.getCrafting());
        }
    }

    @SubscribeEvent
    public void touchSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, event.getSmelting());
        }
    }

    @SubscribeEvent
    public void touchBeforePickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, event.getItemEntity().getItem());
        }
    }

    @SubscribeEvent
    public void touchBeforeEating(LivingEntityUseItemEvent.Start event) {
        ItemStack stack = event.getItem();
        if (!stack.has(DataComponents.FOOD) || event.getDuration() <= 0) {
            return;
        }

        if (event.getEntity().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, stack);
        }

        event.setDuration(stack.getUseDuration(event.getEntity()));
    }

    @SubscribeEvent
    public void applySpoiledFoodEffects(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = event.getItem();
        if (!stack.has(DataComponents.FOOD) || SpoilageService.profileFor(stack) == null
                || SpoilageService.getState(stack, level.getGameTime()) != SpoilageState.SPOILED) {
            return;
        }

        SpoiledFoodEffects effects = SpoilageRulesLoader.INSTANCE.spoiledEffectsFor(stack);
        if (effects == null) {
            return;
        }

        applySpoiledFoodEffects(event.getEntity(), effects);
    }

    @SubscribeEvent
    public void addFreshnessTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        FreshnessData data = stack.get(ModDataComponents.FRESHNESS.get());
        if (data == null) {
            return;
        }

        long gameTime = event.getEntity() == null ? 0 : event.getEntity().level().getGameTime();
        FreshnessData updated = data.updatedTo(gameTime);
        SpoilageState state = updated.state();

        Component name = event.getToolTip().get(0);
        event.getToolTip().set(0, FreshnessDisplay.nameWithFreshness(stack, name, gameTime));

        if (state != SpoilageState.SPOILED) {
            double ticksPerHour = 1000.0 / MarsSpoilageConfig.TIMESPEED.get();
            long hours = Math.max(1, Math.round(updated.remainingFreshTicks() / ticksPerHour));
            Component timeLine;
            if (hours < 24) {
                timeLine = hours == 1
                        ? Component.translatable("tooltip.marsfoodspoilage.spoils_in_hour")
                        : Component.translatable("tooltip.marsfoodspoilage.spoils_in_hours", hours);
            } else {
                long days = hours / 24;
                timeLine = days == 1
                        ? Component.translatable("tooltip.marsfoodspoilage.spoils_in_day")
                        : Component.translatable("tooltip.marsfoodspoilage.spoils_in_days", days);
            }
            event.getToolTip().add(timeLine);
        }
    }

    private static void touchInventory(ServerLevel level, Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (SpoilageService.touchStack(level, inventory.getItem(slot))) {
                inventory.setChanged();
            }
        }
    }

    private static void touchMenu(ServerLevel level, AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (SpoilageService.touchStack(level, slot.getItem())) {
                slot.setChanged();
            }
        }
        menu.broadcastChanges();
    }

    private static void applySpoiledFoodEffects(LivingEntity entity, SpoiledFoodEffects effects) {
        if (roll(entity, effects.poisonChance())) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));
        }
        if (roll(entity, effects.nauseaChance())) {
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, NAUSEA_DURATION_TICKS, 0));
        }
        if (roll(entity, effects.weaknessChance())) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION_TICKS, 0));
        }
        if (roll(entity, effects.slownessChance())) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, 0));
        }
        if (roll(entity, effects.hungerChance())) {
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_DURATION_TICKS, 0));
        }
    }

    private static boolean roll(LivingEntity entity, double chance) {
        return chance > 0.0 && entity.getRandom().nextDouble() < chance;
    }
}
