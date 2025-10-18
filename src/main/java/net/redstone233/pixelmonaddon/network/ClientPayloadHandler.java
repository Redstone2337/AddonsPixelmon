// ClientPayloadHandler.java
package net.redstone233.pixelmonaddon.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.redstone233.pixelmonaddon.screen.AnnouncementScreen;

public class ClientPayloadHandler {

    public static void handleAnnouncement(final AnnouncementPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // 检查世界哈希值是否已显示过
            String currentWorldHash = getCurrentWorldHash();
            if (!payload.worldHash().equals(currentWorldHash)) {
                // 显示公告屏幕
                Minecraft.getInstance().setScreen(new AnnouncementScreen(payload.config()));
                // 保存当前世界哈希值
                saveDisplayedWorldHash(payload.worldHash());
            }
        });
    }

    private static String getCurrentWorldHash() {
        // 从本地存储获取已显示的世界哈希值
        // 这里可以使用NBT、配置文件或其他持久化方式
        return ""; // 实现获取逻辑
    }

    private static void saveDisplayedWorldHash(String hash) {
        // 保存已显示的世界哈希值到本地存储
        // 实现保存逻辑
    }
}