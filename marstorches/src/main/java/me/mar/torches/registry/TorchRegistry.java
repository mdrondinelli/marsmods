package me.mar.torches.registry;

import me.mar.torches.MarsTorches;
import me.mar.torches.block.MarsTorchBlock;
import me.mar.torches.block.MarsWallTorchBlock;
import me.mar.torches.condition.DropUnlitCondition;
import me.mar.torches.item.LitTorchItem;
import me.mar.torches.item.MatchboxItem;
import me.mar.torches.item.UnlitTorchItem;
import me.mar.torches.worldgen.TorchBiomeModifier;
import me.mar.torches.worldgen.TorchFeature;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TorchRegistry {

    // Worldgen disabled — see knowledgebase/marstorches-worldgen.md for future StructurePlaceEvent approach
    public static final DeferredRegister<Feature<?>> FEATURE_REGISTRY = DeferredRegister.create(Registries.FEATURE, MarsTorches.MODID);
    public static final DeferredHolder<Feature<?>, TorchFeature> TORCH_FEATURE = FEATURE_REGISTRY.register(TorchFeature.NAME, () -> new TorchFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTRY = DeferredRegister.create(Registries.CONFIGURED_FEATURE, MarsTorches.MODID);
    public static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<NoneFeatureConfiguration, TorchFeature>> TORCH_CONFIGURED_FEATURE =
            CONFIGURED_FEATURE_REGISTRY.register(TorchFeature.NAME, () -> new ConfiguredFeature<>(TORCH_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

    public static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTRY = DeferredRegister.create(Registries.PLACED_FEATURE, MarsTorches.MODID);
    public static final DeferredHolder<PlacedFeature, PlacedFeature> TORCH_PLACED_FEATURE =
            PLACED_FEATURE_REGISTRY.register(TorchFeature.NAME, () -> new PlacedFeature(TORCH_CONFIGURED_FEATURE, List.of()));

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MarsTorches.MODID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<TorchBiomeModifier>> TORCH_BIOME_MODIFIER =
            BIOME_MODIFIER_SERIALIZERS.register(TorchFeature.NAME, () -> RecordCodecBuilder.mapCodec(builder ->
                    builder.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(TorchBiomeModifier::feature))
                           .apply(builder, TorchBiomeModifier::new)));

    public static final DeferredRegister.Blocks BLOCK_REGISTRY = DeferredRegister.createBlocks(MarsTorches.MODID);
    public static final DeferredHolder<Block, Block> TORCH_BLOCK = BLOCK_REGISTRY.registerBlock(
            MarsTorchBlock.NAME, MarsTorchBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.TORCH).lightLevel(MarsTorchBlock.getLightValueFromState()));
    public static final DeferredHolder<Block, Block> TORCH_WALL_BLOCK = BLOCK_REGISTRY.registerBlock(
            MarsWallTorchBlock.NAME, MarsWallTorchBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.TORCH).lightLevel(MarsTorchBlock.getLightValueFromState()));

    public static final DeferredRegister.Items ITEM_REGISTRY = DeferredRegister.createItems(MarsTorches.MODID);
    public static final DeferredHolder<Item, Item> UNLIT_TORCH_ITEM = ITEM_REGISTRY.registerItem(UnlitTorchItem.NAME, properties -> new UnlitTorchItem(properties, Direction.DOWN));
    public static final DeferredHolder<Item, Item> LIT_TORCH_ITEM = ITEM_REGISTRY.registerItem(LitTorchItem.NAME, properties -> new LitTorchItem(properties, Direction.DOWN));
    public static final DeferredHolder<Item, Item> MATCHBOX_ITEM = ITEM_REGISTRY.registerItem(MatchboxItem.NAME, MatchboxItem::new);
    public static final DeferredHolder<Item, Item> GLOWSTONE_PASTE_ITEM = ITEM_REGISTRY.registerItem("glowstone_paste", Item::new);
    public static final DeferredHolder<Item, Item> GLOWSTONE_CRYSTAL_ITEM = ITEM_REGISTRY.registerItem("glowstone_crystal", Item::new);

    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_REGISTRY =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MarsTorches.MODID);
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<DropUnlitCondition>> DROP_UNLIT_CONDITION =
            LOOT_CONDITION_REGISTRY.register(DropUnlitCondition.NAME, () -> DropUnlitCondition.MAP_CODEC);

    private TorchRegistry() {}
}
