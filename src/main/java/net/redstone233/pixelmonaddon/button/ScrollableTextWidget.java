package net.redstone233.pixelmonaddon.button;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ScrollableTextWidget extends AbstractWidget {
    private final Minecraft minecraft;
    private final Font font;
    private final List<Component> textLines;
    private double scrollAmount;
    private boolean scrolling;
    private int totalHeight;
    private final int scrollbarWidth = 6;
    private final int scrollbarPadding = 2;
    private final int color;

    public ScrollableTextWidget(int x, int y, int width, int height, Component message, Font font, Minecraft minecraft, int color) {
        super(x, y, width, height, message);
        this.minecraft = minecraft;
        this.font = font;
        this.color = color;
        this.textLines = new ArrayList<>();
        this.scrollAmount = 0;
        this.totalHeight = 0;
        updateTextLines();
    }

    public void updateTextLines() {
        textLines.clear();

        try {
            if (getMessage() == null) {
                textLines.add(Component.literal("暂无公告内容"));
            } else {
                String plainText = getMessage().getString();
                String[] lines = plainText.split("\n");
                for (String line : lines) {
                    textLines.add(parseFormattingCodes(line));
                }
            }

            totalHeight = textLines.size() * (font.lineHeight + 2);

        } catch (Exception e) {
            System.err.println("文本处理失败: " + e.getMessage());
            textLines.add(Component.literal("文本渲染错误"));
            totalHeight = font.lineHeight;
        }
    }

    private Component parseFormattingCodes(String text) {
        MutableComponent result = Component.empty();
        StringBuilder currentText = new StringBuilder();
        ChatFormatting currentFormatting = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                char codeChar = text.charAt(i + 1);
                ChatFormatting formatting = ChatFormatting.getByCode(codeChar);

                if (formatting != null) {
                    if (!currentText.isEmpty()) {
                        MutableComponent segment = Component.literal(currentText.toString());
                        if (currentFormatting != null) {
                            segment = segment.withStyle(currentFormatting);
                        }
                        result.append(segment);
                        currentText.setLength(0);
                    }

                    currentFormatting = formatting.isColor() ? formatting : currentFormatting;
                    i++;
                } else {
                    currentText.append(c);
                }
            } else {
                currentText.append(c);
            }
        }

        if (!currentText.isEmpty()) {
            MutableComponent segment = Component.literal(currentText.toString());
            if (currentFormatting != null) {
                segment = segment.withStyle(currentFormatting);
            }
            result.append(segment);
        }

        return result;
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        updateTextLines();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x80404040);

        // 绘制边框
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF808080);
        guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF808080);
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF808080);
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF808080);

        // 启用裁剪
        int clipX = getX() + scrollbarPadding;
        int clipY = getY() + scrollbarPadding;
        int clipWidth = width - scrollbarWidth - scrollbarPadding * 2;
        int clipHeight = height - scrollbarPadding * 2;

        guiGraphics.enableScissor(clipX, clipY, clipX + clipWidth, clipY + clipHeight);

        // 绘制文本
        int yOffset = getY() + scrollbarPadding - (int) scrollAmount;
        for (Component line : textLines) {
            if (yOffset + font.lineHeight >= getY() && yOffset <= getY() + height) {
                int textColor = this.color;
                guiGraphics.drawString(font, line, getX() + scrollbarPadding, yOffset, textColor, false);
            }
            yOffset += font.lineHeight + 2;
        }

        // 禁用裁剪
        guiGraphics.disableScissor();

        // 绘制滚动条
        drawScrollbar(guiGraphics);
    }

    private void drawScrollbar(GuiGraphics guiGraphics) {
        if (totalHeight > height) {
            int scrollbarHeight = (int) ((float) height * height / totalHeight);
            scrollbarHeight = Math.max(scrollbarHeight, 20);

            int scrollbarY = (int) (getY() + (scrollAmount / (totalHeight - height)) * (height - scrollbarHeight));
            scrollbarY = Mth.clamp(scrollbarY, getY(), getY() + height - scrollbarHeight);

            // 滚动条背景
            guiGraphics.fill(getX() + width - scrollbarWidth, getY(),
                    getX() + width, getY() + height,
                    0x55AAAAAA);

            // 滚动条滑块
            guiGraphics.fill(getX() + width - scrollbarWidth + 1, scrollbarY,
                    getX() + width - 1, scrollbarY + scrollbarHeight,
                    0xFF888888);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.scrollable_text"));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.visible && this.scrolling && totalHeight > height) {
            double maxScroll = totalHeight - height;
            double relativeY = mouseY - getY();
            scrollAmount = (relativeY / height) * maxScroll;
            scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.visible && totalHeight > height) {
            double maxScroll = totalHeight - height;
            scrollAmount = Mth.clamp(scrollAmount - scrollY * 20, 0, maxScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && button == 0) {
            if (mouseX >= getX() + width - scrollbarWidth && mouseX <= getX() + width) {
                scrolling = true;

                if (totalHeight > height) {
                    double relativeY = mouseY - getY();
                    double maxScroll = totalHeight - height;
                    scrollAmount = (relativeY / height) * maxScroll;
                    scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(double scrollAmount) {
        this.scrollAmount = scrollAmount;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int getColor() {
        return color;
    }
}