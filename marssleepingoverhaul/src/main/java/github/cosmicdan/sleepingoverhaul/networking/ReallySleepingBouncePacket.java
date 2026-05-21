package github.cosmicdan.sleepingoverhaul.networking;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ReallySleepingBouncePacket(boolean reallySleeping) implements CustomPacketPayload {
    public static final Type<ReallySleepingBouncePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SleepingOverhaul.MOD_ID, "is_really_sleeping_bounce"));
    public static final StreamCodec<ByteBuf, ReallySleepingBouncePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReallySleepingBouncePacket::reallySleeping,
            ReallySleepingBouncePacket::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
