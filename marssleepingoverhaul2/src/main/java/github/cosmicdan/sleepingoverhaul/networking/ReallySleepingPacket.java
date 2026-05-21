package github.cosmicdan.sleepingoverhaul.networking;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ReallySleepingPacket(boolean reallySleeping) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReallySleepingPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SleepingOverhaul.MOD_ID, "is_really_sleeping"));
    public static final StreamCodec<ByteBuf, ReallySleepingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReallySleepingPacket::reallySleeping,
            ReallySleepingPacket::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
