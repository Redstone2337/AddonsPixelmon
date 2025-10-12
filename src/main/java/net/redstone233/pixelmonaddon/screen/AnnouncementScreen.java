package net.redstone233.pixelmonaddon.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.redstone233.pixelmonaddon.AddonsPixelmon;
import net.redstone233.pixelmonaddon.Config;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

public class AnnouncementScreen extends Screen {
    private final Screen parent;
    private static boolean hasShown = false;

    public AnnouncementScreen(Screen parent) {
        super(Component.literal(Config.ANNOUNCEMENT_TITLE.get()));
        this.parent = parent;
        Config.logDebug("公告屏幕已创建");
    }

    public static boolean shouldShowAnnouncement() {
        boolean shouldShow = Config.SHOW_ANNOUNCEMENT.get() && !hasShown;
        Config.logDebug("是否显示公告: " + shouldShow + " (配置: " + Config.SHOW_ANNOUNCEMENT.get() + ", 已显示: " + hasShown + ")");
        return shouldShow;
    }

    public static void markAsShown() {
        hasShown = true;
        Config.logDebug("标记公告为已显示");
    }

    @Override
    protected void init() {
        super.init();

        Config.logDebug("初始化公告屏幕组件");

        // 初始化公告列表 - 调整位置为标题下方
        int listTop = 50; // 标题下方留出更多空间
        int listBottom = this.height - 80;
        AnnouncementList announcementList = new AnnouncementList(this.minecraft, this.width, listBottom - listTop, listTop, 20);
        this.addRenderableWidget(announcementList);

        // 添加公告内容
        List<? extends String> announcementLines = Config.ANNOUNCEMENT_BODY.get();
        for (String line : announcementLines) {
            announcementList.addEntry(Component.literal(line));
        }

        Config.logDebug("添加了 " + announcementLines.size() + " 行公告内容");

        // 按钮配置
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonY = this.height - 40;
        int totalButtons = getVisibleButtonCount();
        int totalWidth = totalButtons * buttonWidth + (totalButtons - 1) * 10;
        int startX = (this.width - totalWidth) / 2;

        int buttonIndex = 0;

        // 确定按钮
        if (Config.DISPLAY_CONFIRM_BUTTON.get()) {
            Button confirmButton = Button.builder(Component.literal("确定"), button -> {
                        Config.logDebug("确定按钮被点击");
                        this.onClose();
                    })
                    .bounds(startX + buttonIndex * (buttonWidth + 10), buttonY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(confirmButton);
            buttonIndex++;
            Config.logDebug("确定按钮已创建");
        }

        // 直链按钮
        if (Config.DISPLAY_LINK_BUTTON.get()) {
            String linkUrl = Config.ON_BUTTON_LINK.get();
            Button linkButton = Button.builder(Component.literal("立即前往"), button -> {
                        Config.logDebug("直链按钮被点击，链接: " + linkUrl);
                        if (!linkUrl.isEmpty()) {
                            try {
                                Util.getPlatform().openUri(URI.create(linkUrl));
                            } catch (Exception e) {
                                Config.logDebug("打开链接失败: " + e.getMessage());
                            }
                        }
                    })
                    .bounds(startX + buttonIndex * (buttonWidth + 10), buttonY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(linkButton);
            buttonIndex++;
            Config.logDebug("直链按钮已创建，链接: " + linkUrl);
        }

        // 取消按钮
        if (Config.DISPLAY_CANCEL_BUTTON.get()) {
            Button cancelButton = Button.builder(Component.literal("取消"), button -> {
                        Config.logDebug("取消按钮被点击");
                        this.onClose();
                    })
                    .bounds(startX + buttonIndex * (buttonWidth + 10), buttonY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(cancelButton);
            Config.logDebug("取消按钮已创建");
        }

        Config.logDebug("按钮初始化完成，可见按钮数量: " + totalButtons);
    }

    private int getVisibleButtonCount() {
        int count = 0;
        if (Config.DISPLAY_CONFIRM_BUTTON.get()) count++;
        if (Config.DISPLAY_LINK_BUTTON.get()) count++;
        if (Config.DISPLAY_CANCEL_BUTTON.get()) count++;
        return count;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        int titleY = 20;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);

        // 绘制版本信息 - 在标题下方同一行，分别位于左下角和右下角
        int versionY = titleY + this.font.lineHeight + 5; // 标题下方5像素

        // 游戏版本 - 左下角
        String gameVersion = "Minecraft " + getMinecraftVersion();
        guiGraphics.drawString(this.font, gameVersion, 10, versionY, 0xAAAAAA, false);

        // 模组版本 - 右下角
        String modVersion = getModVersionString();
        int textWidth = this.font.width(modVersion);
        guiGraphics.drawString(this.font, modVersion, this.width - textWidth - 10, versionY, 0xAAAAAA, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Config.logDebug("公告屏幕渲染完成");
    }

    private String getMinecraftVersion() {
        try {
            // 获取Minecraft版本
            return FMLLoader.versionInfo().mcVersion(); // 通常格式为 "1.21.1" 或类似
        } catch (Exception e) {
            Config.logDebug("获取Minecraft版本失败: " + e.getMessage());
            return "1.21.1";
        }
    }

    private String getModVersionString() {
        try {
            var modContainer = ModList.get().getModContainerById(AddonsPixelmon.MOD_ID).orElse(null); // 替换为您的模组ID
            if (modContainer != null) {
                String version = modContainer.getModInfo().getVersion().toString();
                // 格式化为类似 "APD 0.1+build.4" 的格式
                return modContainer.getModInfo().getDisplayName() + " " + version;
            }
        } catch (Exception e) {
            Config.logDebug("获取模组版本失败: " + e.getMessage());
        }
        return "APD 0.1+build.4";
    }

    @Override
    public void onClose() {
        markAsShown();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
        Config.logDebug("公告屏幕关闭");
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return Config.DISPLAY_CANCEL_BUTTON.get();
    }

    // 公告列表内部类
    private static class AnnouncementList extends ObjectSelectionList<AnnouncementList.Entry> {
        public AnnouncementList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        public void addEntry(Component text) {
            this.addEntry(new Entry(text));
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final Component text;

            public Entry(Component text) {
                this.text = text;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                guiGraphics.drawString(AnnouncementList.this.minecraft.font, this.text, left + 5, top + 2, 0xFFFFFF, false);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return false;
            }
        }
    }
}