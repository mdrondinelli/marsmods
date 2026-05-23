package me.mar.torches.condition;

import me.mar.torches.config.TorchConfig;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DropUnlitCondition implements LootItemCondition {

    public static final String NAME = "drop_unlit";

    private static final DropUnlitCondition INSTANCE = new DropUnlitCondition();

    public static final MapCodec<DropUnlitCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    private DropUnlitCondition() {}

    @Override
    public MapCodec<DropUnlitCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        return TorchConfig.VANILLA_TORCHES_DROP_UNLIT.get();
    }
}
