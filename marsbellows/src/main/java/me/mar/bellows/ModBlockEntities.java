package me.mar.bellows;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MarsBellows.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KilnBlockEntity>> KILN =
            BLOCK_ENTITIES.register(KilnBlock.NAME,
                    () -> new BlockEntityType<>(KilnBlockEntity::new, ModBlocks.KILN.get()));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
