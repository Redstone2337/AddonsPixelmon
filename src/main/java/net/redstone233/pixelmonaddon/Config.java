package net.redstone233.pixelmonaddon;

import java.util.Arrays;
import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // 调试模式
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;
    public static final ModConfigSpec.BooleanValue SHOW_ANNOUNCEMENT;

    // 公告设置
    public static final ModConfigSpec.ConfigValue<String> ANNOUNCEMENT_TITLE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ANNOUNCEMENT_BODY;

    // 屏幕设置
    public static final ModConfigSpec.BooleanValue DISPLAY_CONFIRM_BUTTON;
    public static final ModConfigSpec.BooleanValue DISPLAY_CANCEL_BUTTON;
    public static final ModConfigSpec.BooleanValue DISPLAY_LINK_BUTTON;
    public static final ModConfigSpec.ConfigValue<String> ON_BUTTON_LINK;

    static {
        BUILDER.push("APD Configuration");

        DEBUG_MODE = BUILDER
                .comment("用于输出是否公告显示成功或渲染成功")
                .define("debugMode", false);

        SHOW_ANNOUNCEMENT = BUILDER
                .comment("用于设定玩家进入则显示一次，下次在进入就不会二次显示")
                .define("showAnnouncement", true);

        BUILDER.pop();

        BUILDER.push("AnnouncementSettings");

        ANNOUNCEMENT_TITLE = BUILDER
                .comment("设置公告标题")
                .define("announcementTitle", "测试公告");

        ANNOUNCEMENT_BODY = BUILDER
                .comment("设置公告文体")
                .defineList("announcementBody",
                        Arrays.asList("这是第1行", "这是第2行", "这是第3行"),
                        obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("AnnouncementScreenSettings");

        DISPLAY_CONFIRM_BUTTON = BUILDER
                .comment("设置是否显示确定按钮(仅支持true或者false)")
                .define("displayConfirmButton", true);

        DISPLAY_CANCEL_BUTTON = BUILDER
                .comment("设置是否显示取消按钮(仅支持true或者false)")
                .define("displayCancelButton", true);

        DISPLAY_LINK_BUTTON = BUILDER
                .comment("设置是否显示直链按钮(仅支持true或者false)")
                .define("displayLinkButton", false);

        ON_BUTTON_LINK = BUILDER
                .comment("设置直连按钮的链接")
                .define("onButtonLink", "");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void logDebug(String message) {
        if (DEBUG_MODE.get()) {
            System.out.println("[APD Debug] " + message);
        }
    }
}
