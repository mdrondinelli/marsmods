package github.cosmicdan.sleepingoverhaul;

import github.cosmicdan.sleepingoverhaul.client.ClientConfig;
import github.cosmicdan.sleepingoverhaul.client.ClientState;
import github.cosmicdan.sleepingoverhaul.server.ClientStateDummy;
import github.cosmicdan.sleepingoverhaul.server.ServerConfig;
import github.cosmicdan.sleepingoverhaul.server.ServerState;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@SuppressWarnings({"StaticNonFinalField", "PublicField"})
public class SleepingOverhaul {
    public static final String MOD_ID = "sleepingoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static IModPlatform MODPLATFORM;

    public static ServerState serverState = null;
    public static IClientState clientState = null;

    public static ServerConfig serverConfig = null;
    public static ClientConfig clientConfig = null;

    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    public SleepingOverhaul(final IModPlatform modPlatform) {
        MODPLATFORM = modPlatform;
        final Pair<ServerConfig, ModConfigSpec> specPairServer = new ModConfigSpec.Builder().configure(ServerConfig::new);
        serverConfig = specPairServer.getLeft();
        MODPLATFORM.registerConfigServer(specPairServer.getRight());
        serverState = new ServerState();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            final Pair<ClientConfig, ModConfigSpec> specPairClient = new ModConfigSpec.Builder().configure(ClientConfig::new);
            clientConfig = specPairClient.getLeft();
            MODPLATFORM.registerConfigClient(specPairClient.getRight());
            clientState = new ClientState();
        } else {
            clientState = new ClientStateDummy();
        }

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(serverState);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (serverState.isTimelapseActive()) {
            LivingEntity entity = event.getEntity();
            if (entity instanceof ServerPlayer serverPlayer) {
                DamageSource source = event.getSource();
                if (!source.getMsgId().equals(TimelapseKillDamageSource.MSG_ID)) {
                    final float adjustedDamage = serverState.getPlayerHurtAdj(serverPlayer, source, event.getAmount());
                    if (Float.isNaN(adjustedDamage)) {
                        event.setCanceled(true);
                    } else if (Float.isInfinite(adjustedDamage)) {
                        event.setCanceled(true);
                        serverPlayer.hurtServer((net.minecraft.server.level.ServerLevel) serverPlayer.level(), new TimelapseKillDamageSource(), Float.MAX_VALUE);
                    }
                }
            }
        }
    }
}
