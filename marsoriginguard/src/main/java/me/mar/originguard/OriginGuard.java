package me.mar.originguard;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class OriginGuard {
    private static volatile Resolved resolvedCache;
    private static volatile int cachedConfigHash;

    private OriginGuard() {}

    public static boolean isBlocked(RegistryAccess registryAccess, ChunkPos pos, Structure structure) {
        int radius = OriginGuardConfig.RADIUS_BLOCKS.get();
        if (radius <= 0) {
            return false;
        }
        int cx = pos.getMiddleBlockX();
        int cz = pos.getMiddleBlockZ();
        long distSq = (long) cx * cx + (long) cz * cz;
        long radiusSq = (long) radius * radius;
        if (distSq > radiusSq) {
            return false;
        }
        Resolved blocked = resolveBlocked();
        if (blocked.keys.isEmpty() && blocked.tags.isEmpty()) {
            return false;
        }
        Registry<Structure> reg = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        Optional<ResourceKey<Structure>> maybeKey = reg.getResourceKey(structure);
        if (maybeKey.isEmpty()) {
            return false;
        }
        ResourceKey<Structure> key = maybeKey.get();
        if (blocked.keys.contains(key)) {
            return true;
        }
        if (!blocked.tags.isEmpty()) {
            Holder.Reference<Structure> holder = reg.getOrThrow(key);
            for (TagKey<Structure> tag : blocked.tags) {
                if (holder.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Resolved resolveBlocked() {
        List<? extends String> raw = OriginGuardConfig.BLOCKED_STRUCTURES.get();
        int hash = raw.hashCode();
        Resolved cached = resolvedCache;
        if (cached != null && cachedConfigHash == hash) {
            return cached;
        }
        Set<ResourceKey<Structure>> keys = new HashSet<>();
        Set<TagKey<Structure>> tags = new HashSet<>();
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            if (entry.startsWith("#")) {
                Identifier rl = Identifier.tryParse(entry.substring(1));
                if (rl != null) {
                    tags.add(TagKey.create(Registries.STRUCTURE, rl));
                }
            } else {
                Identifier rl = Identifier.tryParse(entry);
                if (rl != null) {
                    keys.add(ResourceKey.create(Registries.STRUCTURE, rl));
                }
            }
        }
        Resolved out = new Resolved(keys, tags);
        resolvedCache = out;
        cachedConfigHash = hash;
        return out;
    }

    private record Resolved(Set<ResourceKey<Structure>> keys, Set<TagKey<Structure>> tags) {}
}
