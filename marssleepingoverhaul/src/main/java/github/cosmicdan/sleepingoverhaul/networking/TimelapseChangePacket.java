package github.cosmicdan.sleepingoverhaul.networking;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TimelapseChangePacket(long timelapseEnd) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TimelapseChangePacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SleepingOverhaul.MOD_ID, "timelapse_change"));
    public static final StreamCodec<ByteBuf, TimelapseChangePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, TimelapseChangePacket::timelapseEnd,
            TimelapseChangePacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
