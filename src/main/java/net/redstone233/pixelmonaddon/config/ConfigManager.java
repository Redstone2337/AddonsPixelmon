package net.redstone233.pixelmonaddon.config;

import net.minecraft.ChatFormatting;
import net.redstone233.pixelmonaddon.AddonsPixelmon;
import net.redstone233.pixelmonaddon.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    // 存储每个世界的最后显示哈希值
    private static final Map<String, String> WORLD_HASH_STORAGE = new HashMap<>();

    // 存储文件路径（如果需要持久化）
    private static final String WORLD_HASH_FILE = "world_announcement_hashes.json";

    // ==================== 通用设置 ====================
    public static boolean shouldShowOnWorldEnter() {
        return Config.SHOW_ON_WORLD_ENTER.get();
    }

    public static boolean isDebugMode() {
        return Config.DEBUG_MODE.get();
    }

    // ==================== 显示设置 ====================
    public static String getMainTitle() {
        return Config.MAIN_TITLE.get();
    }

    public static String getSubTitle() {
        return Config.SUB_TITLE.get();
    }

    public static int getScrollSpeed() {
        return Config.SCROLL_SPEED.get();
    }

    // ==================== 颜色设置 ====================
    public static boolean useCustomRGB() {
        return Config.USE_CUSTOM_RGB.get();
    }

    public static int getMainTitleColor() {
        return Config.MAIN_TITLE_COLOR.get();
    }

    public static int getSubTitleColor() {
        return Config.SUB_TITLE_COLOR.get();
    }

    public static int getContentColor() {
        return Config.CONTENT_COLOR.get();
    }

    // ==================== 按钮设置 ====================
    public static String getConfirmButtonText() {
        return Config.CONFIRM_BUTTON_TEXT.get();
    }

    public static String getSubmitButtonText() {
        return Config.SUBMIT_BUTTON_TEXT.get();
    }

    public static String getButtonLink() {
        return Config.BUTTON_LINK.get();
    }

    // ==================== 图标设置 ====================
    public static boolean shouldShowIcon() {
        return Config.SHOW_ICON.get();
    }

    public static String getIconPath() {
        return Config.ICON_PATH.get();
    }

    public static int getIconWidth() {
        return Config.ICON_WIDTH.get();
    }

    public static int getIconHeight() {
        return Config.ICON_HEIGHT.get();
    }

    public static int getIconTextSpacing() {
        return Config.ICON_TEXT_SPACING.get();
    }

    // ==================== 背景设置 ====================
    public static boolean useCustomAnnouncementBackground() {
        return Config.USE_CUSTOM_ANNOUNCEMENT_BACKGROUND.get();
    }

    public static String getAnnouncementBackgroundPath() {
        return Config.ANNOUNCEMENT_BACKGROUND_PATH.get();
    }

    // ==================== 内容设置 ====================
    public static List<String> getAnnouncementContent() {
        try {
            // 安全地转换类型
            List<?> rawList = Config.ANNOUNCEMENT_CONTENT.get();
            List<String> result = new ArrayList<>();

            for (Object item : rawList) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }

            // 如果列表为空，返回默认内容
            if (result.isEmpty()) {
                return getDefaultAnnouncementContent();
            }

            return result;
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.warn("获取公告内容时出错，使用默认值", e);
            return getDefaultAnnouncementContent();
        }
    }

    private static List<String> getDefaultAnnouncementContent() {
        return List.of(
                "§a欢迎游玩，我们团队做的模组！",
                " ",
                "§e一些提醒：",
                "§f1. 模组仅限于1.21.7~1.21.8 NeoForge",
                "§f2. 模组目前是半成品",
                "§f3. 后面会继续更新",
                " ",
                "§b模组随缘更新",
                "§c若发现bug可以向模组作者或者仓库反馈！"
        );
    }

    public static String getLastDisplayedHash() {
        return Config.LAST_DISPLAYED_HASH.get();
    }

    public static void setLastDisplayedHash(String hash) {
        try {
            Config.LAST_DISPLAYED_HASH.set(hash);
            // 保存配置更改
            saveConfigChanges();
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("设置最后显示哈希值时出错", e);
        }
    }

    // ==================== 世界特定哈希值存储 ====================

    /**
     * 获取世界特定的配置哈希值
     */
    public static String getWorldSpecificHash(String worldName) {
        String configHash = getConfigHash();
        return generateWorldHash(worldName, configHash);
    }

    /**
     * 检查世界特定的配置是否已更改
     */
    public static boolean hasWorldConfigChanged(String worldName) {
        String currentHash = getWorldSpecificHash(worldName);
        String lastHash = getLastWorldHash(worldName);
        return !currentHash.equals(lastHash);
    }

    /**
     * 获取最后显示的世界哈希值
     */
    public static String getLastWorldHash(String worldName) {
        try {
            // 首先尝试从内存存储获取
            if (WORLD_HASH_STORAGE.containsKey(worldName)) {
                return WORLD_HASH_STORAGE.get(worldName);
            }

            // 如果内存中没有，尝试从文件加载
            loadWorldHashesFromStorage();

            // 再次尝试获取
            return WORLD_HASH_STORAGE.getOrDefault(worldName, "");
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("获取世界哈希值时出错", e);
            return "";
        }
    }

    /**
     * 更新最后显示的世界哈希值
     */
    public static void updateLastWorldHash(String worldName, String hash) {
        try {
            // 更新内存存储
            WORLD_HASH_STORAGE.put(worldName, hash);

            // 保存到持久化存储
            saveWorldHashesToStorage();

            AddonsPixelmon.LOGGER.debug("更新世界 {} 的哈希值为: {}", worldName, hash);
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("更新世界哈希值时出错", e);
        }
    }

    /**
     * 重置特定世界的显示状态
     */
    public static void resetWorldHash(String worldName) {
        updateLastWorldHash(worldName, "");
        AddonsPixelmon.LOGGER.info("已重置世界 {} 的公告显示状态", worldName);
    }

    /**
     * 重置所有世界的显示状态
     */
    public static void resetAllWorldHashes() {
        WORLD_HASH_STORAGE.clear();
        try {
            saveWorldHashesToStorage();
            AddonsPixelmon.LOGGER.info("已重置所有世界的公告显示状态");
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("重置所有世界哈希值时出错", e);
        }
    }

    /**
     * 获取所有世界的哈希值信息（用于调试命令）
     */
    public static Map<String, String> getAllWorldHashes() {
        loadWorldHashesFromStorage(); // 确保数据是最新的
        return new HashMap<>(WORLD_HASH_STORAGE);
    }

    // ==================== 存储持久化方法 ====================

    /**
     * 从持久化存储加载世界哈希值
     */
    private static void loadWorldHashesFromStorage() {
        try {
            // 这里可以使用NBT、JSON文件或其他持久化方式
            // 暂时使用简单的内存存储，在实际应用中应该实现文件存储

            // 示例：从配置文件加载（如果需要文件存储，可以在此实现）
            // File storageFile = getWorldHashStorageFile();
            // if (storageFile.exists()) {
            //     // 读取并解析存储文件
            //     String content = Files.readString(storageFile.toPath());
            //     // 解析JSON或其他格式到WORLD_HASH_STORAGE
            // }

            // 暂时使用空实现，在实际项目中需要根据选择的存储方式实现
            AddonsPixelmon.LOGGER.debug("加载世界哈希值存储");

        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("加载世界哈希值存储时出错", e);
        }
    }

    /**
     * 保存世界哈希值到持久化存储
     */
    private static void saveWorldHashesToStorage() {
        try {
            // 这里可以使用NBT、JSON文件或其他持久化方式

            // 示例：保存到配置文件（如果需要文件存储，可以在此实现）
            // File storageFile = getWorldHashStorageFile();
            // String content = // 将WORLD_HASH_STORAGE转换为JSON字符串
            // Files.writeString(storageFile.toPath(), content);

            // 暂时使用空实现，在实际项目中需要根据选择的存储方式实现
            AddonsPixelmon.LOGGER.debug("保存世界哈希值存储，共 {} 个世界", WORLD_HASH_STORAGE.size());

        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("保存世界哈希值存储时出错", e);
        }
    }

    /**
     * 获取世界哈希值存储文件
     */
    private static java.io.File getWorldHashStorageFile() {
        // 返回存储文件的路径
        // 示例：return new File(FMLPaths.CONFIGDIR.get().toFile(), WORLD_HASH_FILE);
        return new java.io.File(WORLD_HASH_FILE);
    }

    /**
     * 保存配置更改
     */
    private static void saveConfigChanges() {
        try {
            // 如果需要手动保存配置，可以在这里实现
            // 在NeoForge中，配置通常会自动保存
            AddonsPixelmon.LOGGER.debug("配置更改已保存");
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("保存配置更改时出错", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成世界特定哈希值
     */
    private static String generateWorldHash(String worldName, String configHash) {
        String combined = worldName + "|" + configHash;
        return Integer.toHexString(combined.hashCode());
    }

    // 辅助方法：从颜色值获取ChatFormatting枚举
    public static ChatFormatting getChatFormattingFromColor(int color) {
        int rgbColor = color & 0xFFFFFF;

        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor() && formatting.getColor() != null) {
                int formattingColor = formatting.getColor();
                if ((formattingColor & 0xFFFFFF) == rgbColor) {
                    return formatting;
                }
            }
        }
        return ChatFormatting.WHITE;
    }

    // 辅助方法：从ChatFormatting枚举获取颜色值
    public static int getColorFromChatFormatting(ChatFormatting formatting) {
        return formatting.getColor() != null ? formatting.getColor() : 0xFFFFFF;
    }

    /**
     * 创建公告配置对象（用于网络传输和客户端显示）
     */
    public static AnnouncementConfig createAnnouncementConfig() {
        AnnouncementConfig config = new AnnouncementConfig();

        config.mainTitle = getMainTitle();
        config.subTitle = getSubTitle();
        config.announcementContent = getAnnouncementContent();
        config.confirmButtonText = getConfirmButtonText();
        config.submitButtonText = getSubmitButtonText();
        config.buttonLink = getButtonLink();
        config.showIcon = shouldShowIcon();
        config.iconPath = getIconPath();
        config.iconWidth = getIconWidth();
        config.iconHeight = getIconHeight();
        config.iconTextSpacing = getIconTextSpacing();
        config.useCustomRGB = useCustomRGB();
        config.mainTitleColor = getMainTitleColor();
        config.subTitleColor = getSubTitleColor();
        config.contentColor = getContentColor();
        config.scrollSpeed = getScrollSpeed();
        config.useCustomAnnouncementBackground = useCustomAnnouncementBackground();
        config.announcementBackgroundPath = getAnnouncementBackgroundPath();

        return config;
    }

    /**
     * 验证配置的完整性
     */
    public static boolean validateConfiguration() {
        try {
            // 测试获取所有配置值
            getMainTitle();
            getSubTitle();
            getAnnouncementContent();
            getMainTitleColor();
            getSubTitleColor();
            getContentColor();
            shouldShowOnWorldEnter();
            isDebugMode();
            shouldShowIcon();
            getIconPath();
            getIconWidth();
            getIconHeight();
            getIconTextSpacing();
            getScrollSpeed();
            getConfirmButtonText();
            getSubmitButtonText();
            getButtonLink();
            useCustomRGB();
            useCustomAnnouncementBackground();
            getAnnouncementBackgroundPath();
            getLastDisplayedHash();

            return true;
        } catch (Exception e) {
            AddonsPixelmon.LOGGER.error("配置验证失败", e);
            return false;
        }
    }

    /**
     * 获取配置的哈希值，用于检测配置是否发生变化
     */
    public static String getConfigHash() {
        AnnouncementConfig config = createAnnouncementConfig();
        return config.getConfigHash();
    }

    /**
     * 检查配置是否已更改
     */
    public static boolean hasConfigChanged() {
        String currentHash = getConfigHash();
        String lastHash = getLastDisplayedHash();
        return !currentHash.equals(lastHash);
    }

    /**
     * 重新加载配置并返回新的配置对象
     */
    public static AnnouncementConfig reloadConfig() {
        // 在NeoForge中，配置会自动重新加载
        // 这里主要是为了返回最新的配置对象
        return createAnnouncementConfig();
    }
}