package me.mar.foodspoilage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemContainerContents;

public final class SpoilageService {
    private static final TagKey<Item> SPOILS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MarsFoodSpoilage.MODID, "spoils"));

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

    @Nullable
    public static SpoilageProfile profileFor(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(SPOILS)) {
            return null;
        }
        SpoilageProfile profile = SpoilageRulesLoader.INSTANCE.profileFor(stack);
        if (profile == null) {
            return null;
        }
        double factor = 1.0 / MarsSpoilageConfig.TIMESPEED.get();
        return new SpoilageProfile(
                Math.round(profile.shelfLifeTicks() * factor),
                Math.round(profile.staleThresholdTicks() * factor));
    }

    /**
     * Settle pending decay at the current rate, then set the new rate.
     *
     * Invariant: every rate change on a stack must go through this method. It guarantees that
     * decay accumulated under the previous rate is persisted (lastUpdateTick advanced to now)
     * before the new rate takes effect. Without this, swapping rate retroactively reinterprets
     * elapsed time at the new rate.
     */
    public static void applyRate(ServerLevel level, ItemStack stack, float newRate) {
        if (stack.isEmpty()) {
            return;
        }
        SpoilageProfile profile = profileFor(stack);
        if (profile == null) {
            return;
        }
        long now = level.getGameTime();
        FreshnessData existing = stack.get(ModDataComponents.FRESHNESS.get());
        if (existing == null) {
            FreshnessData created = new FreshnessData(now, profile.shelfLifeTicks(), profile.staleThresholdTicks(), newRate);
            stack.set(ModDataComponents.FRESHNESS.get(), created);
            updateConsumableForStateChange(stack, SpoilageState.FRESH, created.state());
            updateFoodForStateChange(stack, SpoilageState.FRESH, created.state());
            return;
        }
        FreshnessData settled = existing.updatedTo(now);
        if (settled.spoilageRate() == newRate
                && settled.remainingFreshTicks() == existing.remainingFreshTicks()) {
            return;
        }
        SpoilageState oldState = existing.state();
        SpoilageState newState = settled.state();
        FreshnessData next = settled.spoilageRate() == newRate ? settled : settled.withRate(newRate);
        stack.set(ModDataComponents.FRESHNESS.get(), next);
        if (oldState != newState) {
            updateConsumableForStateChange(stack, oldState, newState);
            updateFoodForStateChange(stack, oldState, newState);
        }
    }

    private static boolean touchOwnFreshness(ServerLevel level, ItemStack stack) {
        SpoilageProfile profile = profileFor(stack);
        if (profile == null) {
            return false;
        }

        long now = level.getGameTime();
        FreshnessData existing = stack.get(ModDataComponents.FRESHNESS.get());
        if (existing == null) {
            FreshnessData updated = new FreshnessData(now, profile.shelfLifeTicks(), profile.staleThresholdTicks(), 1.0f);
            stack.set(ModDataComponents.FRESHNESS.get(), updated);
            updateConsumableForStateChange(stack, SpoilageState.FRESH, updated.state());
            updateFoodForStateChange(stack, SpoilageState.FRESH, updated.state());
            return true;
        }

        FreshnessData updated = existing.updatedTo(now);
        if (updated.state() != existing.state()) {
            stack.set(ModDataComponents.FRESHNESS.get(), updated);
            updateConsumableForStateChange(stack, existing.state(), updated.state());
            updateFoodForStateChange(stack, existing.state(), updated.state());
            return true;
        }
        return false;
    }

    private static void updateConsumableForStateChange(ItemStack stack, SpoilageState oldState, SpoilageState newState) {
        Consumable current = stack.get(DataComponents.CONSUMABLE);
        if (current == null) {
            return;
        }

        double ratio = eatDurationMultiplier(newState) / eatDurationMultiplier(oldState);
        Consumable updated = multipliedConsumable(current, ratio);
        if (!updated.equals(current)) {
            stack.set(DataComponents.CONSUMABLE, updated);
        }
    }

    private static double eatDurationMultiplier(SpoilageState state) {
        return switch (state) {
            case FRESH -> 1.0;
            case STALE -> MarsSpoilageConfig.STALE_EAT_DURATION_MULTIPLIER.get();
            case SPOILED -> MarsSpoilageConfig.SPOILED_EAT_DURATION_MULTIPLIER.get();
        };
    }

    private static double nutritionMultiplier(SpoilageState state) {
        return switch (state) {
            case FRESH -> 1.0;
            case STALE -> MarsSpoilageConfig.STALE_NUTRITION_MULTIPLIER.get();
            case SPOILED -> MarsSpoilageConfig.SPOILED_NUTRITION_MULTIPLIER.get();
        };
    }

    private static double saturationMultiplier(SpoilageState state) {
        return switch (state) {
            case FRESH -> 1.0;
            case STALE -> MarsSpoilageConfig.STALE_SATURATION_MULTIPLIER.get();
            case SPOILED -> MarsSpoilageConfig.SPOILED_SATURATION_MULTIPLIER.get();
        };
    }

    private static void updateFoodForStateChange(ItemStack stack, SpoilageState oldState, SpoilageState newState) {
        FoodProperties current = stack.get(DataComponents.FOOD);
        if (current == null) {
            return;
        }

        double oldNutritionMult = nutritionMultiplier(oldState);
        double oldSaturationMult = saturationMultiplier(oldState);
        int newNutrition = oldNutritionMult > 0.0
                ? Math.max(0, (int) Math.round(current.nutrition() * (nutritionMultiplier(newState) / oldNutritionMult)))
                : current.nutrition();
        float newSaturation = oldSaturationMult > 0.0
                ? Math.max(0.0f, (float) (current.saturation() * (saturationMultiplier(newState) / oldSaturationMult)))
                : current.saturation();

        if (newNutrition == current.nutrition() && newSaturation == current.saturation()) {
            return;
        }
        stack.set(DataComponents.FOOD, new FoodProperties(newNutrition, newSaturation, current.canAlwaysEat()));
    }

    private static Consumable multipliedConsumable(Consumable original, double multiplier) {
        float consumeSeconds = (float) (original.consumeSeconds() * multiplier);
        return new Consumable(
                consumeSeconds,
                original.animation(),
                original.sound(),
                original.hasConsumeParticles(),
                original.onConsumeEffects());
    }

    public static void enterRack(ServerLevel level, ItemStack stack) {
        applyRate(level, stack, MarsSpoilageConfig.RACK_SPOILAGE_RATE.get().floatValue());
    }

    public static void exitRack(ServerLevel level, ItemStack stack) {
        applyRate(level, stack, 1.0f);
    }

    private static boolean touchNestedContainer(ServerLevel level, ItemStack stack) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return false;
        }

        List<ItemStack> items = contents.allItemsCopyStream().toList();
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
