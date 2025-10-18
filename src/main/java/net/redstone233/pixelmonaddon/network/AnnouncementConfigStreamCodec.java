package net.redstone233.pixelmonaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementConfigStreamCodec {

    public static final StreamCodec<FriendlyByteBuf, AnnouncementConfig> STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public @NotNull AnnouncementConfig decode(@NotNull FriendlyByteBuf buf) {
                    AnnouncementConfig config = new AnnouncementConfig();

                    // 解码所有字段
                    config.mainTitle = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.subTitle = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.announcementContent = stringListStreamCodec().decode(buf);
                    config.confirmButtonText = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.submitButtonText = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.buttonLink = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.showIcon = ByteBufCodecs.BOOL.decode(buf);
                    config.iconPath = ByteBufCodecs.STRING_UTF8.decode(buf);
                    config.iconWidth = ByteBufCodecs.INT.decode(buf);
                    config.iconHeight = ByteBufCodecs.INT.decode(buf);
                    config.iconTextSpacing = ByteBufCodecs.INT.decode(buf);
                    config.useCustomRGB = ByteBufCodecs.BOOL.decode(buf);
                    config.mainTitleColor = ByteBufCodecs.INT.decode(buf);
                    config.subTitleColor = ByteBufCodecs.INT.decode(buf);
                    config.contentColor = ByteBufCodecs.INT.decode(buf);
                    config.scrollSpeed = ByteBufCodecs.INT.decode(buf);
                    config.useCustomAnnouncementBackground = ByteBufCodecs.BOOL.decode(buf);
                    config.announcementBackgroundPath = ByteBufCodecs.STRING_UTF8.decode(buf);

                    return config;
                }

                @Override
                public void encode(@NotNull FriendlyByteBuf buf, AnnouncementConfig config) {
                    // 编码所有字段
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.mainTitle);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.subTitle);
                    stringListStreamCodec().encode(buf, config.announcementContent);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.confirmButtonText);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.submitButtonText);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.buttonLink);
                    ByteBufCodecs.BOOL.encode(buf, config.showIcon);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.iconPath);
                    ByteBufCodecs.INT.encode(buf, config.iconWidth);
                    ByteBufCodecs.INT.encode(buf, config.iconHeight);
                    ByteBufCodecs.INT.encode(buf, config.iconTextSpacing);
                    ByteBufCodecs.BOOL.encode(buf, config.useCustomRGB);
                    ByteBufCodecs.INT.encode(buf, config.mainTitleColor);
                    ByteBufCodecs.INT.encode(buf, config.subTitleColor);
                    ByteBufCodecs.INT.encode(buf, config.contentColor);
                    ByteBufCodecs.INT.encode(buf, (int) config.scrollSpeed);
                    ByteBufCodecs.BOOL.encode(buf, config.useCustomAnnouncementBackground);
                    ByteBufCodecs.STRING_UTF8.encode(buf, config.announcementBackgroundPath);
                }
            };

    private static StreamCodec<FriendlyByteBuf, List<String>> stringListStreamCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull List<String> decode(@NotNull FriendlyByteBuf buf) {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                List<String> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                }
                return list;
            }

            @Override
            public void encode(@NotNull FriendlyByteBuf buf, @NotNull List<String> list) {
                ByteBufCodecs.VAR_INT.encode(buf, list.size());
                for (String item : list) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, item);
                }
            }
        };
    }
}