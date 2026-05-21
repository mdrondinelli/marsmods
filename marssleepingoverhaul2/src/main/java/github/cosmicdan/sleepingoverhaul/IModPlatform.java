package github.cosmicdan.sleepingoverhaul;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IModPlatform {
    void registerConfigServer(ModConfigSpec spec);

    void registerConfigClient(ModConfigSpec spec);

    boolean canPlayerStartSleepNow(final ServerPlayer serverPlayer);

    boolean canPlayerContinueSleepNow(final Player player);

}
