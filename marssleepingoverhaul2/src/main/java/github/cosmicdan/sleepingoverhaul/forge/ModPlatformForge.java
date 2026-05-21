package github.cosmicdan.sleepingoverhaul.forge;

import com.mojang.datafixers.util.Either;
import github.cosmicdan.sleepingoverhaul.IModPlatform;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.EventHooks;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ModPlatformForge implements IModPlatform {
    @Override
    public void registerConfigServer(ModConfigSpec spec) {
        SleepingOverhaulNeoForge.CONTAINER.registerConfig(ModConfig.Type.SERVER, spec);
    }

    @Override
    public void registerConfigClient(ModConfigSpec spec) {
        SleepingOverhaulNeoForge.CONTAINER.registerConfig(ModConfig.Type.CLIENT, spec);
    }

    @Override
    public boolean canPlayerStartSleepNow(ServerPlayer serverPlayer) {
        if (!serverPlayer.getSleepingPos().isPresent()) return false;
        final BlockPos bedPos = serverPlayer.getSleepingPos().get();
        BedRule rule = serverPlayer.level().environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, bedPos);
        boolean canSleep = rule.canSleep(serverPlayer.level())
            || SleepingOverhaul.serverConfig.featureAllowAnyDimension.get()
            || SleepingOverhaul.serverConfig.bedRestAllowDaytime.get();
        Either<Player.BedSleepingProblem, Unit> vanillaResult = canSleep
            ? Either.right(Unit.INSTANCE)
            : Either.left(rule.asProblem());
        return EventHooks.canPlayerStartSleeping(serverPlayer, bedPos, vanillaResult).right().isPresent();
    }

    @Override
    public boolean canPlayerContinueSleepNow(Player player) {
        //Player.BedSleepingProblem vanillaResult = player.level().isDay() ? Player.BedSleepingProblem.NOT_POSSIBLE_NOW : null;
        //return EventHooks.canEntityContinueSleeping(player, vanillaResult);
        // Only works on server side! If called on client, will always return false. All the more reason to use [TODO] Bed Groups.
        // Note that below is what NeoForged does, but we remove the hardcoded check for a BedBlock
        //boolean hasBed = player.getSleepingPos().map(pos -> player.level().getBlockState(pos).isBed(player.level(), pos, player)).orElse(false);
        boolean hasBed = player.getSleepingPos().isPresent();
        return EventHooks.canEntityContinueSleeping(player, hasBed ? null : Player.BedSleepingProblem.OTHER_PROBLEM);
    }
}
