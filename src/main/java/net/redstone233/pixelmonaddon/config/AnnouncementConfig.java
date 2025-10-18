package net.redstone233.pixelmonaddon.config;

import java.util.List;

/**
 * 公告配置数据类，用于在网络数据包中传输配置
 */
public class AnnouncementConfig {
    public String mainTitle;
    public String subTitle;
    public List<String> announcementContent;
    public String confirmButtonText;
    public String submitButtonText;
    public String buttonLink;
    public boolean showIcon;
    public String iconPath;
    public int iconWidth;
    public int iconHeight;
    public int iconTextSpacing;
    public boolean useCustomRGB;
    public int mainTitleColor;
    public int subTitleColor;
    public int contentColor;
    public double scrollSpeed;
    public boolean useCustomAnnouncementBackground;
    public String announcementBackgroundPath;

    public AnnouncementConfig() {
        // 空构造函数用于反序列化
    }

    /**
     * 从配置管理器创建配置实例
     */
    public static AnnouncementConfig fromConfigManager() {
        return ConfigManager.createAnnouncementConfig();
    }

    /**
     * 验证配置的完整性
     */
    public boolean isValid() {
        return mainTitle != null &&
                subTitle != null &&
                announcementContent != null &&
                !announcementContent.isEmpty() &&
                confirmButtonText != null &&
                submitButtonText != null &&
                buttonLink != null;
    }

    /**
     * 获取配置的哈希值，用于检测配置是否改变
     */
    public String getConfigHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(mainTitle).append(subTitle).append(confirmButtonText)
                .append(submitButtonText).append(buttonLink).append(showIcon)
                .append(iconPath).append(useCustomRGB).append(useCustomAnnouncementBackground)
                .append(announcementBackgroundPath);

        for (String line : announcementContent) {
            sb.append(line);
        }

        return Integer.toHexString(sb.toString().hashCode());
    }
}