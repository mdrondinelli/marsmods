package me.mar.worbloodfx;

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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class BloodFxEvents {
    private static final String BLOOD_DECAL_TAG = MarsWorBloodFx.MODID + ".blood_decal";
    private static final int CLEANUP_INTERVAL_TICKS = 21;
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

        spawnHitParticles(level, entity, profile);
        trySpawnGroundSplatter(level, entity, profile);
    }

    @SubscribeEvent
    public void cleanupSplatters(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.getGameTime() % CLEANUP_INTERVAL_TICKS != 0) {
            return;
        }

        for (Display.ItemDisplay display : level.getEntities(EntityType.ITEM_DISPLAY, BloodFxEvents::isBloodDecal)) {
            if (display.tickCount >= MarsWorBloodFxConfig.GROUND_SPLATTER_LIFETIME_TICKS.get()
                    || !hasValidSupportingBlock(level, BlockPos.containing(display.getX(), display.getY() - 0.6D, display.getZ()))) {
                display.discard();
            }
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

        BlockPos supportPos = BlockPos.containing(entity.getX(), entity.getY() - 0.1D, entity.getZ());
        if (!hasValidSupportingBlock(level, supportPos) || isChunkAtSplatterCap(level, supportPos)) {
            return;
        }

        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level, entityDisplay -> {
            entityDisplay.addTag(BLOOD_DECAL_TAG);
            entityDisplay.addTag(profile.decalTag);
            entityDisplay.setNoGravity(true);
            entityDisplay.setYRot(level.getRandom().nextFloat() * 360.0F);
            entityDisplay.getSlot(0).set(bloodSplatterStack(profile));
        }, supportPos.above(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, false, false);
        if (display == null) {
            return;
        }

        display.setPos(entity.getX(), entity.getY() + 0.5D, entity.getZ());
        level.addFreshEntity(display);
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
