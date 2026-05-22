package me.mar.foodspoilage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FreshnessDisplay {
    private FreshnessDisplay() {
    }

    public static Component nameWithFreshness(ItemStack stack, Component baseName, long gameTime) {
        FreshnessData data = stack.get(ModDataComponents.FRESHNESS.get());
        if (data == null) {
            return baseName;
        }

        SpoilageState state = data.updatedTo(gameTime).state();
        Component suffix = Component.translatable("tooltip.marsfoodspoilage.freshness." + state.name().toLowerCase());
        return Component.empty().append(baseName).append(" (").append(suffix).append(")");
    }
}
