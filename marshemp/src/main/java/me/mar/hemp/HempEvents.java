package me.mar.hemp;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class HempEvents {
    private static final int PLANT_FIBER_DROP_CHANCE = 8;

    @SubscribeEvent
    public void addPlantFiberDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player) || !isFiberSource(event)) {
            return;
        }

        if (event.getLevel().getRandom().nextInt(PLANT_FIBER_DROP_CHANCE) != 0) {
            return;
        }

        Vec3 center = event.getPos().getCenter();
        event.getDrops().add(new ItemEntity(
                event.getLevel(),
                center.x(),
                center.y(),
                center.z(),
                new ItemStack(ModItems.PLANT_FIBER.get())));
    }

    private static boolean isFiberSource(BlockDropsEvent event) {
        return event.getState().is(Blocks.SHORT_GRASS)
                || event.getState().is(Blocks.TALL_GRASS)
                || event.getState().is(Blocks.SHORT_DRY_GRASS)
                || event.getState().is(Blocks.TALL_DRY_GRASS)
                || event.getState().is(Blocks.VINE);
    }
}
