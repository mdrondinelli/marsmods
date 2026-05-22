package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record FreshnessStateProperty() implements SelectItemModelProperty<SpoilageState> {
    public static final SelectItemModelProperty.Type<FreshnessStateProperty, SpoilageState> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new FreshnessStateProperty()), SpoilageState.CODEC);

    @Override
    @Nullable
    public SpoilageState get(ItemStack stack, @Nullable ClientLevel level,
            @Nullable LivingEntity entity, int seed, ItemDisplayContext ctx) {
        FreshnessData data = stack.get(ModDataComponents.FRESHNESS.get());
        if (data == null) {
            return null;
        }
        long gameTime = level != null ? level.getGameTime() : 0;
        return data.updatedTo(gameTime).state();
    }

    @Override
    public Codec<SpoilageState> valueCodec() {
        return SpoilageState.CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<FreshnessStateProperty, SpoilageState> type() {
        return TYPE;
    }
}
