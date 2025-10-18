package net.redstone233.pixelmonaddon.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.redstone233.pixelmonaddon.button.ScrollableTextWidget;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnouncementScreen extends Screen {
    private final AnnouncementConfig config;
    private ScrollableTextWidget scrollableText;
    private int tickCount = 0;
    private ResourceLocation iconTexture;
    private ResourceLocation backgroundTexture;

    private StringWidget titleWidget;
    private StringWidget subtitleWidget;

    public AnnouncementScreen(AnnouncementConfig config) {
        super(Component.literal("服务器公告"));
        this.config = config != null ? config : new AnnouncementConfig();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonY = this.height - 30;

        // 加载图标纹理
        loadIconTexture();

        // 加载背景纹理
        loadBackgroundTexture();

        // 创建标题和副标题
        createTitleWidgets(centerX);

        // 创建公告内容
        MutableComponent contentText = createAnnouncementContent();

        // 创建滚动文本部件
        int contentColor = config.useCustomRGB ? config.contentColor : 0xFFFFFFFF;

        if ((contentColor & 0xFFFFFF) == (0xB4303030 & 0xFFFFFF)) {
            contentColor = 0xFFFFFFFF;
        }

        scrollableText = new ScrollableTextWidget(
                centerX - 150, 80, 300, this.height - 150,
                contentText, this.font, this.minecraft,
                contentColor
        );
        addRenderableWidget(scrollableText);

        // 创建按钮
        createButtons(centerX, buttonWidth, buttonHeight, buttonY);

        System.out.println("公告屏幕初始化完成");
    }

    private void loadIconTexture() {
        if (config.showIcon && config.iconPath != null && !config.iconPath.isEmpty()) {
            try {
                iconTexture = ResourceLocation.parse(config.iconPath);
            } catch (Exception e) {
                System.err.println("无法加载图标纹理: " + config.iconPath);
                iconTexture = null;
            }
        }
    }

    private void loadBackgroundTexture() {
        if (config.useCustomAnnouncementBackground &&
                config.announcementBackgroundPath != null &&
                !config.announcementBackgroundPath.isEmpty()) {
            try {
                backgroundTexture = ResourceLocation.parse(config.announcementBackgroundPath);
            } catch (Exception e) {
                System.err.println("无法加载公告背景纹理: " + config.announcementBackgroundPath);
                backgroundTexture = null;
            }
        }
    }

    private void createTitleWidgets(int centerX) {
        int titleX = centerX;
        if (config.showIcon && iconTexture != null) {
            int iconAreaWidth = config.iconWidth + config.iconTextSpacing;
            titleX = centerX + iconAreaWidth / 2;
        }

        // 主标题
        String mainTitleText = config.mainTitle != null ? config.mainTitle : "服务器公告";
        MutableComponent mainTitle = createStyledText(mainTitleText, config.mainTitleColor, true)
                .withStyle(ChatFormatting.BOLD);

        titleWidget = new StringWidget(titleX - 100, 30, 200, 20, mainTitle, this.font);
        titleWidget.alignCenter();
        addRenderableWidget(titleWidget);

        // 副标题
        String subTitleText = config.subTitle != null ? config.subTitle : "最新通知";
        MutableComponent subTitle = createStyledText(subTitleText, config.subTitleColor, false);

        subtitleWidget = new StringWidget(titleX - 100, 55, 200, 20, subTitle, this.font);
        subtitleWidget.alignCenter();
        addRenderableWidget(subtitleWidget);
    }

    private MutableComponent createStyledText(String text, int color, boolean useCustomRGB) {
        MutableComponent component = Component.literal(text);
        if (config.useCustomRGB) {
            return component.withStyle(Style.EMPTY.withColor(color));
        } else {
            ChatFormatting formatting = findMatchingFormatting(color);
            return component.withStyle(formatting);
        }
    }

    private ChatFormatting findMatchingFormatting(int color) {
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

    private MutableComponent createAnnouncementContent() {
        MutableComponent root = Component.empty();
        List<String> contentLines = getContentStrings();

        for (int i = 0; i < contentLines.size(); i++) {
            String line = contentLines.get(i);

            if (line.trim().isEmpty()) {
                root.append(Component.literal("\n"));
                continue;
            }

            MutableComponent lineText = parseFormattedText(line);
            root.append(lineText);

            if (i < contentLines.size() - 1) {
                root.append(Component.literal("\n"));
            }
        }

        return root;
    }

    private MutableComponent parseFormattedText(String text) {
        MutableComponent result = Component.empty();
        Pattern pattern = Pattern.compile("(&#[0-9a-fA-F]{6}|§[0-9a-fk-or]|[^&§]+)");
        Matcher matcher = pattern.matcher(text);
        Style currentStyle = Style.EMPTY;

        while (matcher.find()) {
            String segment = matcher.group();

            if (segment.startsWith("&#")) {
                try {
                    int rgb = Integer.parseInt(segment.substring(2), 16);
                    currentStyle = currentStyle.withColor(rgb);
                } catch (NumberFormatException e) {
                    System.err.println("无效的RGB颜色代码: " + segment);
                }
            } else if (segment.startsWith("§")) {
                ChatFormatting formatting = ChatFormatting.getByCode(segment.charAt(1));
                if (formatting != null) {
                    if (formatting == ChatFormatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else if (formatting.isColor()) {
                        currentStyle = currentStyle.withColor(formatting);
                    }
                }
            } else {
                MutableComponent textSegment = Component.literal(segment).setStyle(currentStyle);
                result.append(textSegment);
            }
        }

        return result;
    }

    private List<String> getContentStrings() {
        List<String> defaultContent = List.of(
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

        return config.announcementContent != null && !config.announcementContent.isEmpty()
                ? config.announcementContent
                : defaultContent;
    }

    private void createButtons(int centerX, int buttonWidth, int buttonHeight, int buttonY) {
        // 确定按钮
        String confirmText = config.confirmButtonText != null ? config.confirmButtonText : "确定";
        Component confirmButtonText = createStyledText(confirmText, 0xFFFFFF, false);

        addRenderableWidget(Button.builder(confirmButtonText, button -> this.onClose())
                .pos(centerX - buttonWidth - 5, buttonY)
                .size(buttonWidth, buttonHeight)
                .build());

        // 前往投递按钮
        String submitText = config.submitButtonText != null ? config.submitButtonText : "前往投递";
        Component submitButtonText = createStyledText(submitText, 0xFFFFFF, false);
        String buttonLink = Objects.requireNonNullElse(config.buttonLink, "https://example.com");

        addRenderableWidget(Button.builder(submitButtonText, button -> openLink(buttonLink))
                .pos(centerX + 5, buttonY)
                .size(buttonWidth, buttonHeight)
                .build());
    }

    private void openLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Util.getPlatform().openUri(URI.create(url));
        } catch (Exception e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("无法打开链接: " + e.getMessage()), false);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        renderBackground(guiGraphics);

        // 绘制图标
        renderIcon(guiGraphics);

        // 渲染其他部件
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 自动滚动
        handleAutoScroll();
    }

    private void renderBackground(GuiGraphics guiGraphics) {
        if (config.useCustomAnnouncementBackground && backgroundTexture != null) {
            try {
                guiGraphics.blit(backgroundTexture, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            } catch (Exception e) {
                guiGraphics.fill(0, 0, this.width, this.height, 0xB4303030);
            }
        } else {
            guiGraphics.fill(0, 0, this.width, this.height, 0xB4303030);
        }
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        if (config.showIcon && iconTexture != null) {
            int iconX = (this.width / 2) - 150 - config.iconWidth - config.iconTextSpacing;
            int iconY = 30;

            try {
                guiGraphics.blit(
                        iconTexture,
                        iconX, iconY,
                        0, 0,
                        config.iconWidth,
                        config.iconHeight,
                        config.iconWidth,
                        config.iconHeight
                );
            } catch (Exception e) {
                System.err.println("无法绘制图标: " + e.getMessage());
            }
        }
    }

    private void handleAutoScroll() {
        if (scrollableText != null && tickCount % 2 == 0) {
            double maxScroll = scrollableText.getTotalHeight() - scrollableText.getHeight();
            if (maxScroll > 0) {
                double scrollAmount = scrollableText.getScrollAmount() + (config.scrollSpeed / 20.0);
                if (scrollAmount > maxScroll) scrollAmount = 0;
                scrollableText.setScrollAmount(Math.min(scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
