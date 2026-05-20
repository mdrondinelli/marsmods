package me.mar.flinttool;

import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class PrimitiveProgressionEvents {
    private static final float HAND_LOG_BREAK_SPEED = 0.05f;
    private static final float FLINT_LOG_BREAK_SPEED = 0.3f;
    private static final float FLINT_STONE_BREAK_SPEED = 0.3f;
    private static final int FLINT_LOG_BREAK_CHANCE = 6;
    private static final Set<Identifier> REMOVED_RECIPES = Set.of(
            Identifier.withDefaultNamespace("wooden_axe"),
            Identifier.withDefaultNamespace("wooden_hoe"),
            Identifier.withDefaultNamespace("wooden_pickaxe"),
            Identifier.withDefaultNamespace("wooden_shovel"),
            Identifier.withDefaultNamespace("wooden_sword"),
            Identifier.withDefaultNamespace("stone_sword"));

    @SubscribeEvent
    public void removeWoodenToolRecipes(ModifyRecipeJsonsEvent event) {
        REMOVED_RECIPES.forEach(event.getRecipeJsons()::remove);
    }

    @SubscribeEvent
    public void slowHandLogBreaking(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().getMainHandItem().isEmpty() && event.getState().is(BlockTags.LOGS)) {
            event.setNewSpeed(Math.min(event.getNewSpeed(), HAND_LOG_BREAK_SPEED));
        }

        if (event.getEntity().getMainHandItem().is(Items.FLINT) && event.getState().is(BlockTags.LOGS)) {
            event.setNewSpeed(Math.min(event.getNewSpeed(), FLINT_LOG_BREAK_SPEED));
        }

        if (event.getEntity().getMainHandItem().is(Items.FLINT) && event.getState().is(Blocks.STONE)) {
            event.setNewSpeed(Math.min(event.getNewSpeed(), FLINT_STONE_BREAK_SPEED));
        }
    }

    @SubscribeEvent
    public void allowFlintStoneHarvest(PlayerEvent.HarvestCheck event) {
        if (event.getTargetBlock().is(BlockTags.LOGS) && event.getEntity().getMainHandItem().is(Items.FLINT)) {
            event.setCanHarvest(true);
        }

        if (event.getTargetBlock().is(Blocks.STONE) && event.getEntity().getMainHandItem().is(Items.FLINT)) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public void modifyPrimitiveDrops(BlockDropsEvent event) {
        if (event.getState().is(BlockTags.LOGS) && event.getTool().isEmpty()) {
            event.getDrops().clear();
            event.setDroppedExperience(0);
            return;
        }

        if (event.getState().is(BlockTags.LOGS)
                && event.getBreaker() instanceof Player player
                && player.getMainHandItem().is(Items.FLINT)
                && player.getRandom().nextInt(FLINT_LOG_BREAK_CHANCE) == 0) {
            player.getMainHandItem().consume(1, player);
            player.onEquippedItemBroken(Items.FLINT, EquipmentSlot.MAINHAND);
        }

        if (event.getState().is(Blocks.STONE)
                && event.getBreaker() instanceof Player player
                && player.getMainHandItem().is(Items.FLINT)) {
            player.getMainHandItem().consume(1, player);
            player.onEquippedItemBroken(Items.FLINT, EquipmentSlot.MAINHAND);
        }
    }
}
