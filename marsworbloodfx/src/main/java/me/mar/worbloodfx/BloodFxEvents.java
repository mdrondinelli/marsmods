package me.mar.worbloodfx;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class BloodFxEvents {
    private static final String BLOOD_DECAL_TAG = MarsWorBloodFx.MODID + ".blood_decal";
    private static final int CLEANUP_INTERVAL_TICKS = 21;
    private static final double HIT_SPLATTER_RANDOM_OFFSET = 0.25D;
    private static final Map<ServerLevel, Map<UUID, BleedingState>> BLEEDING_ENTITIES = new WeakHashMap<>();
    private static final TagKey<Block> BLOOD_CANNOT_SPAWN_ON = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MarsWorBloodFx.MODID, "blood_cannot_spawn_on"));

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (event.getNewDamage() <= 0.0F || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        BloodProfile profile = BloodProfile.forEntity(entity);
        if (profile == null) {
            return;
        }
        if (!passesArmorFxChance(level, entity)) {
            return;
        }

        spawnHitParticles(level, entity, profile);
        trySpawnGroundSplatter(level, entity, profile);
        startOrExtendBleeding(level, entity, profile, event.getNewDamage());
    }

    @SubscribeEvent
    public void tickBloodFx(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (!MarsWorBloodFxConfig.ENABLE_BLEEDING_TRAILS.get()) {
            BLEEDING_ENTITIES.remove(level);
        } else if (gameTime % MarsWorBloodFxConfig.BLEED_INTERVAL_TICKS.get() == 0) {
            tickBleedingEntities(level, gameTime);
        }
        if (gameTime % CLEANUP_INTERVAL_TICKS == 0) {
            cleanupSplatters(level);
        }
    }

    @SubscribeEvent
    public void brushSplatter(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.getItemStack().getItem() != Items.BRUSH
                || !isBloodDecal(event.getTarget())) {
            return;
        }

        Entity target = event.getTarget();
        BloodProfile profile = BloodProfile.forDecal(target);
        boolean removed = level.getRandom().nextDouble() < MarsWorBloodFxConfig.BRUSH_CLEANUP_CHANCE.get();
        level.playSound(null, target.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 0.45F, removed ? 1.2F : 0.8F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, profile.particleState),
                target.getX(), target.getY(), target.getZ(), removed ? 8 : 3, 0.25D, 0.02D, 0.25D, 0.01D);
        if (removed) {
            target.discard();
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void spawnHitParticles(ServerLevel level, LivingEntity entity, BloodProfile profile) {
        int count = MarsWorBloodFxConfig.HIT_PARTICLE_COUNT.get();
        if (!MarsWorBloodFxConfig.ENABLE_PARTICLES.get() || count <= 0) {
            return;
        }

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, profile.particleState),
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                count, 0.0D, 0.8D, 0.0D, 0.001D);
    }

    private static void trySpawnGroundSplatter(ServerLevel level, LivingEntity entity, BloodProfile profile) {
        if (!MarsWorBloodFxConfig.ENABLE_GROUND_SPLATTERS.get() || !entity.onGround()
                || level.getRandom().nextDouble() >= MarsWorBloodFxConfig.GROUND_SPLATTER_CHANCE.get()) {
            return;
        }

        spawnGroundSplatter(level, entity, profile, true);
    }

    private static boolean passesArmorFxChance(ServerLevel level, LivingEntity entity) {
        int armor = entity.getArmorValue();
        if (armor <= 0) {
            return true;
        }

        double reduction = Math.min(
                MarsWorBloodFxConfig.MAX_ARMOR_FX_REDUCTION.get(),
                armor * MarsWorBloodFxConfig.ARMOR_FX_REDUCTION_PER_POINT.get());
        return level.getRandom().nextDouble() < 1.0D - reduction;
    }

    private static boolean spawnGroundSplatter(ServerLevel level, LivingEntity entity, BloodProfile profile,
            boolean randomizePosition) {
        double x = entity.getX();
        double z = entity.getZ();
        if (randomizePosition) {
            x += randomCentered(level, HIT_SPLATTER_RANDOM_OFFSET);
            z += randomCentered(level, HIT_SPLATTER_RANDOM_OFFSET);
        }

        BlockPos supportPos = BlockPos.containing(x, entity.getY() - 0.1D, z);
        if (!hasValidSupportingBlock(level, supportPos) || isChunkAtSplatterCap(level, supportPos)) {
            return false;
        }

        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level, entityDisplay -> {
            entityDisplay.addTag(BLOOD_DECAL_TAG);
            entityDisplay.addTag(profile.decalTag);
            entityDisplay.setNoGravity(true);
            entityDisplay.setYRot(level.getRandom().nextFloat() * 360.0F);
            entityDisplay.getSlot(0).set(bloodSplatterStack(profile));
        }, supportPos.above(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, false, false);
        if (display == null) {
            return false;
        }

        display.setPos(x, entity.getY() + 0.5D, z);
        level.addFreshEntity(display);
        return true;
    }

    private static double randomCentered(ServerLevel level, double radius) {
        return (level.getRandom().nextDouble() * 2.0D - 1.0D) * radius;
    }

    private static void startOrExtendBleeding(ServerLevel level, LivingEntity entity, BloodProfile profile, float damage) {
        if (!MarsWorBloodFxConfig.ENABLE_BLEEDING_TRAILS.get()
                || damage < MarsWorBloodFxConfig.MIN_BLEED_DAMAGE.get()) {
            return;
        }

        Map<UUID, BleedingState> levelBleeding = BLEEDING_ENTITIES.computeIfAbsent(level, unused -> new java.util.HashMap<>());
        UUID id = entity.getUUID();
        BleedingState existing = levelBleeding.get(id);
        if (existing == null && levelBleeding.size() >= MarsWorBloodFxConfig.MAX_BLEEDING_ENTITIES_PER_LEVEL.get()) {
            return;
        }

        long now = level.getGameTime();
        int duration = bleedDuration(damage);
        long endTick = Math.min(now + MarsWorBloodFxConfig.BLEED_MAX_DURATION_TICKS.get(),
                Math.max(existing == null ? now : existing.endTick, now) + duration);
        if (existing == null) {
            levelBleeding.put(id, new BleedingState(endTick, profile, now, entity.position(), now));
        } else {
            existing.endTick = endTick;
            existing.profile = profile;
        }
    }

    private static int bleedDuration(float damage) {
        double duration = MarsWorBloodFxConfig.BLEED_BASE_DURATION_TICKS.get()
                + damage * MarsWorBloodFxConfig.BLEED_DURATION_PER_DAMAGE.get();
        return Math.max(1, (int) Math.round(Math.min(duration, MarsWorBloodFxConfig.BLEED_MAX_DURATION_TICKS.get())));
    }

    private static void tickBleedingEntities(ServerLevel level, long gameTime) {
        Map<UUID, BleedingState> levelBleeding = BLEEDING_ENTITIES.get(level);
        if (levelBleeding == null || levelBleeding.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, BleedingState>> iterator = levelBleeding.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BleedingState> entry = iterator.next();
            BleedingState state = entry.getValue();
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity livingEntity) || entity.isRemoved() || !entity.isAlive()
                    || gameTime >= state.endTick) {
                iterator.remove();
                continue;
            }

            int interval = MarsWorBloodFxConfig.BLEED_INTERVAL_TICKS.get();
            if (gameTime < state.nextDripTick) {
                continue;
            }
            state.nextDripTick = gameTime + interval;

            if (level.getRandom().nextDouble() >= MarsWorBloodFxConfig.BLEED_DRIP_CHANCE.get()) {
                continue;
            }

            spawnBleedDripParticles(level, livingEntity, state.profile);
            trySpawnTrailSplatter(level, livingEntity, state, gameTime);
        }

        if (levelBleeding.isEmpty()) {
            BLEEDING_ENTITIES.remove(level);
        }
    }

    private static void spawnBleedDripParticles(ServerLevel level, LivingEntity entity, BloodProfile profile) {
        if (!MarsWorBloodFxConfig.ENABLE_PARTICLES.get()) {
            return;
        }

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, profile.particleState),
                entity.getX(), entity.getY() + 0.15D, entity.getZ(),
                2, 0.2D, 0.05D, 0.2D, 0.005D);
    }

    private static void trySpawnTrailSplatter(ServerLevel level, LivingEntity entity, BleedingState state, long gameTime) {
        if (!MarsWorBloodFxConfig.ENABLE_GROUND_SPLATTERS.get() || !entity.onGround()) {
            return;
        }

        double minDistance = MarsWorBloodFxConfig.TRAIL_MIN_DISTANCE.get();
        boolean movedEnough = entity.position().distanceToSqr(state.lastSplatterPos) >= minDistance * minDistance;
        boolean stationaryReady = gameTime - state.lastStationarySplatterTick
                >= MarsWorBloodFxConfig.STATIONARY_SPLATTER_INTERVAL_TICKS.get();
        if ((movedEnough || stationaryReady) && spawnGroundSplatter(level, entity, state.profile, true)) {
            state.lastSplatterPos = entity.position();
            state.lastStationarySplatterTick = gameTime;
        }
    }

    private static void cleanupSplatters(ServerLevel level) {
        for (Display.ItemDisplay display : level.getEntities(EntityType.ITEM_DISPLAY, BloodFxEvents::isBloodDecal)) {
            if (display.tickCount >= MarsWorBloodFxConfig.GROUND_SPLATTER_LIFETIME_TICKS.get()
                    || !hasValidSupportingBlock(level, BlockPos.containing(display.getX(), display.getY() - 0.6D, display.getZ()))) {
                display.discard();
            }
        }
    }

    private static boolean hasValidSupportingBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.is(BLOOD_CANNOT_SPAWN_ON) && state.blocksMotion();
    }

    private static boolean isChunkAtSplatterCap(ServerLevel level, BlockPos pos) {
        int cap = MarsWorBloodFxConfig.MAX_GROUND_SPLATTERS_PER_CHUNK.get();
        if (cap <= 0) {
            return true;
        }

        ChunkPos chunk = ChunkPos.containing(pos);
        AABB bounds = new AABB(chunk.getMinBlockX(), -4096.0D, chunk.getMinBlockZ(),
                chunk.getMaxBlockX() + 1.0D, 4096.0D, chunk.getMaxBlockZ() + 1.0D);
        return level.getEntities(EntityType.ITEM_DISPLAY, bounds, BloodFxEvents::isBloodDecal).size() >= cap;
    }

    private static ItemStack bloodSplatterStack(BloodProfile profile) {
        ItemStack stack = new ItemStack(Items.POISONOUS_POTATO);
        stack.set(DataComponents.ITEM_MODEL, profile.itemModel);
        return stack;
    }

    private static boolean isBloodDecal(Entity entity) {
        return entity instanceof Display.ItemDisplay && entity.entityTags().contains(BLOOD_DECAL_TAG);
    }

    private static final class BleedingState {
        private long endTick;
        private BloodProfile profile;
        private long nextDripTick;
        private Vec3 lastSplatterPos;
        private long lastStationarySplatterTick;

        private BleedingState(long endTick, BloodProfile profile, long nextDripTick, Vec3 lastSplatterPos,
                long lastStationarySplatterTick) {
            this.endTick = endTick;
            this.profile = profile;
            this.nextDripTick = nextDripTick;
            this.lastSplatterPos = lastSplatterPos;
            this.lastStationarySplatterTick = lastStationarySplatterTick;
        }
    }

    private enum BloodProfile {
        AMBER(
                "amber_blood",
                "amber_blood_splatter",
                Blocks.HONEY_BLOCK.defaultBlockState()),
        BLUE_GREEN(
                "blue_green_blood",
                "blue_green_blood_splatter",
                Blocks.OXIDIZED_COPPER.defaultBlockState()),
        RED(
                "red_blood",
                "red_blood_splatter",
                Blocks.REDSTONE_BLOCK.defaultBlockState());

        private final TagKey<EntityType<?>> entityTypeTag;
        private final Identifier itemModel;
        private final BlockState particleState;
        private final String decalTag;

        BloodProfile(String entityTypeTag, String itemModel, BlockState particleState) {
            this.entityTypeTag = TagKey.create(
                    Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MarsWorBloodFx.MODID, entityTypeTag));
            this.itemModel = Identifier.fromNamespaceAndPath(
                    MarsWorBloodFx.MODID,
                    "blocks/ground_markings/blood_splatter/" + itemModel);
            this.particleState = particleState;
            this.decalTag = MarsWorBloodFx.MODID + ".blood_profile." + name().toLowerCase();
        }

        private static BloodProfile forEntity(LivingEntity entity) {
            for (BloodProfile profile : values()) {
                if (entity.getType().builtInRegistryHolder().is(profile.entityTypeTag)) {
                    return profile;
                }
            }
            return null;
        }

        private static BloodProfile forDecal(Entity entity) {
            for (BloodProfile profile : values()) {
                if (entity.entityTags().contains(profile.decalTag)) {
                    return profile;
                }
            }
            return RED;
        }
    }
}
