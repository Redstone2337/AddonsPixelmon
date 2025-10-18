// SinglePlayerHandler.java
package net.redstone233.pixelmonaddon.handlers;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import net.redstone233.pixelmonaddon.config.ConfigManager;
import net.redstone233.pixelmonaddon.screen.AnnouncementScreen;

import static net.redstone233.pixelmonaddon.handlers.WorldEnterHandler.hasConfigChanged;
import static net.redstone233.pixelmonaddon.handlers.WorldEnterHandler.updateLastDisplayedHash;

public class SinglePlayerHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().isLocalServer()) {
            // 单人游戏，直接检查并显示
            checkAndShowAnnouncement();
        }
    }

    private static void checkAndShowAnnouncement() {
        if (ConfigManager.shouldShowOnWorldEnter()) {
            String worldHash = generateWorldHash();

            if (hasConfigChanged(worldHash)) {
                AnnouncementConfig config = ConfigManager.createAnnouncementConfig();
                Minecraft.getInstance().setScreen(new AnnouncementScreen(config));

                updateLastDisplayedHash(worldHash);
            }
        }
    }

    private static String generateWorldHash() {
        // 单人游戏的世界哈希值生成逻辑
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            String worldName = mc.level.dimension().location().toString();
            String configHash = ConfigManager.getConfigHash();
            return Integer.toHexString((worldName + configHash).hashCode());
        }
        return "";
    }

    // ... 其他辅助方法 ...
}