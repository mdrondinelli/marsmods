package me.mar.wildcrops;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarsWildCrops.MODID);

    public static final DeferredBlock<Block> WILD_CARROTS = registerCrop("wild_carrots", () -> new WildCropBlock(
            cropProperties("wild_carrots", Blocks.CARROTS),
            Items.CARROT));

    public static final DeferredBlock<Block> WILD_POTATOES = registerCrop("wild_potatoes", () -> new WildCropBlock(
            cropProperties("wild_potatoes", Blocks.POTATOES),
            Items.POTATO));

    public static final DeferredBlock<Block> WILD_BEETROOTS = registerCrop("wild_beetroots", () -> new WildBeetrootBlock(
            cropProperties("wild_beetroots", Blocks.BEETROOTS)));

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private static DeferredBlock<Block> registerCrop(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    private static BlockBehaviour.Properties cropProperties(String name, Block source) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MarsWildCrops.MODID, name));
        return BlockBehaviour.Properties.ofFullCopy(source).setId(key).noOcclusion();
    }
}
