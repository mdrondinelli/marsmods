package me.mar.foodspoilage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class SpoilageEvents {
    private static final int TOUCH_INTERVAL_TICKS = 20;

    public void makeFoodUnstackable(ModifyDefaultComponentsEvent event) {
        event.modifyMatching(
                (item, components) -> components.get(DataComponents.FOOD) != null,
                (components, context, item) -> components.set(DataComponents.MAX_STACK_SIZE, 1));
    }

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
            SpoilageService.touchStack(level, itemEntity.getItem());
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
    public void touchBeforePickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer().level() instanceof ServerLevel level) {
            SpoilageService.touchStack(level, event.getItemEntity().getItem());
        }
    }

    @SubscribeEvent
    public void touchBeforeEating(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = event.getItem();
        SpoilageService.touchStack(level, stack);
        SpoilageProfile profile = SpoilageService.profileFor(stack);
        if (profile == null || !profile.cancelSpoiledConsumption()) {
            return;
        }

        if (SpoilageService.getState(stack, level.getGameTime()) == SpoilageState.SPOILED) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("message.marsfoodspoilage.spoiled_food"));
            }
        }
    }

    @SubscribeEvent
    public void addFreshnessTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (SpoilageService.profileFor(stack) == null) {
            return;
        }

        long gameTime = event.getEntity() == null ? 0 : event.getEntity().level().getGameTime();
        SpoilageState state = SpoilageService.getState(stack, gameTime);
        event.getToolTip().add(Component.translatable("tooltip.marsfoodspoilage.freshness." + state.name().toLowerCase()));
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
}
