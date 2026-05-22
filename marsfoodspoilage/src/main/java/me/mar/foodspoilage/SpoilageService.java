package me.mar.foodspoilage;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class SpoilageService {
    private SpoilageService() {
    }

    public static boolean touchStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        boolean changed = touchOwnFreshness(level, stack);
        changed |= touchNestedContainer(level, stack);
        return changed;
    }

    public static SpoilageState getState(ItemStack stack, long currentTick) {
        FreshnessData data = stack.get(ModDataComponents.FRESHNESS.get());
        if (data == null) {
            return SpoilageState.FRESH;
        }
        return data.updatedTo(currentTick).state();
    }

    public static SpoilageProfile profileFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        SpoilageProfile profile = SpoilageRulesLoader.INSTANCE.profileFor(stack);
        if (profile == null) {
            return null;
        }
        double multiplier = MarsSpoilageConfig.SHELF_LIFE_MULTIPLIER.get();
        if (multiplier == 1.0) {
            return profile;
        }
        return new SpoilageProfile(
                Math.round(profile.shelfLifeTicks() * multiplier),
                Math.round(profile.staleThresholdTicks() * multiplier),
                profile.cancelSpoiledConsumption());
    }

    private static boolean touchOwnFreshness(ServerLevel level, ItemStack stack) {
        SpoilageProfile profile = profileFor(stack);
        if (profile == null) {
            return false;
        }

        long now = level.getGameTime();
        FreshnessData existing = stack.get(ModDataComponents.FRESHNESS.get());
        if (existing == null) {
            FreshnessData updated = new FreshnessData(now, profile.shelfLifeTicks(), profile.shelfLifeTicks(), profile.staleThresholdTicks());
            stack.set(ModDataComponents.FRESHNESS.get(), updated);
            return true;
        }

        FreshnessData updated = existing.updatedTo(now);
        if (updated.remainingFreshTicks() == 0 && existing.remainingFreshTicks() != 0) {
            stack.set(ModDataComponents.FRESHNESS.get(), updated);
            return true;
        }
        return false;
    }

    private static boolean touchNestedContainer(ServerLevel level, ItemStack stack) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return false;
        }

        List<ItemStack> items = new ArrayList<>(contents.allItemsCopyStream().toList());
        boolean changed = false;
        for (ItemStack contained : items) {
            changed |= touchStack(level, contained);
        }

        if (changed) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        }
        return changed;
    }
}
