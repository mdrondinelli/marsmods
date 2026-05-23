package me.mar.torches.worldgen;

import me.mar.torches.registry.TorchRegistry;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record TorchBiomeModifier(Holder<PlacedFeature> feature) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.AFTER_EVERYTHING) {
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TorchRegistry.TORCH_BIOME_MODIFIER.get();
    }
}
