package net.redstone233.pixelmonaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.redstone233.pixelmonaddon.AddonsPixelmon;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import org.jetbrains.annotations.NotNull;

public record AnnouncementPayload(AnnouncementConfig config, String worldHash) implements CustomPacketPayload {

    public static final Type<AnnouncementPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AddonsPixelmon.MOD_ID, "announcement"));

    public static final StreamCodec<FriendlyByteBuf, AnnouncementPayload> STREAM_CODEC =
            StreamCodec.composite(
                    AnnouncementConfigStreamCodec.STREAM_CODEC,
                    AnnouncementPayload::config,
                    ByteBufCodecs.STRING_UTF8,
                    AnnouncementPayload::worldHash,
                    AnnouncementPayload::new
            );

    // 移除错误的构造函数，因为record会自动生成正确的构造函数
    // 移除write方法，因为StreamCodec会处理编码

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}