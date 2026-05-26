package me.mar.originguard.mixin;

import me.mar.originguard.OriginGuard;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void marsoriginguard$blockNearOrigin(
            StructureSet.StructureSelectionEntry selected,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess centerChunk,
            ChunkPos sourceChunkPos,
            SectionPos sectionPos,
            ResourceKey<Level> level,
            CallbackInfoReturnable<Boolean> cir) {
        Structure structure = selected.structure().value();
        if (OriginGuard.isBlocked(registryAccess, sourceChunkPos, structure)) {
            cir.setReturnValue(false);
        }
    }
}
