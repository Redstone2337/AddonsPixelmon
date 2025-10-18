// WorldEnterHandler.java
package net.redstone233.pixelmonaddon.handlers;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import net.redstone233.pixelmonaddon.config.ConfigManager;
import net.redstone233.pixelmonaddon.network.AnnouncementPayload;

public class WorldEnterHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            checkAndSendAnnouncement(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            checkAndSendAnnouncement(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.isEndConquered()) {
            checkAndSendAnnouncement(serverPlayer);
        }
    }

    private static void checkAndSendAnnouncement(ServerPlayer player) {
        if (ConfigManager.shouldShowOnWorldEnter()) {
            // 生成世界哈希值（基于世界名称和配置哈希值）
            String worldHash = generateWorldHash(player);

            // 检查配置是否发生变化
            if (hasConfigChanged(worldHash)) {
                AnnouncementConfig config = ConfigManager.createAnnouncementConfig();
                AnnouncementPayload payload = new AnnouncementPayload(config, worldHash);

                // 发送网络包给客户端
                player.connection.send(payload);

                // 更新最后显示的哈希值
                updateLastDisplayedHash(worldHash);
            }
        }
    }

    private static String generateWorldHash(ServerPlayer player) {
        String worldName = player.level().dimension().location().toString();
        String configHash = ConfigManager.getConfigHash();
        return Integer.toHexString((worldName + configHash).hashCode());
    }

    static boolean hasConfigChanged(String currentHash) {
        // 比较当前哈希值与上次显示的哈希值
        String lastHash = getLastDisplayedHash();
        return !currentHash.equals(lastHash);
    }

    private static String getLastDisplayedHash() {
        // 从存储获取上次显示的哈希值
        // 这里可以使用玩家NBT、独立存储等
        return ""; // 实现获取逻辑
    }

    static void updateLastDisplayedHash(String hash) {
        // 更新最后显示的哈希值到存储
        // 实现更新逻辑
    }
}