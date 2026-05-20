package github.cosmicdan.sleepingoverhaul.forge;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingBouncePacket;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingPacket;
import github.cosmicdan.sleepingoverhaul.networking.TimelapseChangePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            ReallySleepingPacket.TYPE,
            ReallySleepingPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() ->
                SleepingOverhaul.serverState.tryReallySleepingRecv(payload, context.player()))
        );
        registrar.playToClient(
            TimelapseChangePacket.TYPE,
            TimelapseChangePacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() ->
                SleepingOverhaul.clientState.recvTimelapseChange(payload, context.player()))
        );
        registrar.playToClient(
            ReallySleepingBouncePacket.TYPE,
            ReallySleepingBouncePacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() ->
                SleepingOverhaul.clientState.recvTrySleepBounce(payload, context.player()))
        );
    }
}
