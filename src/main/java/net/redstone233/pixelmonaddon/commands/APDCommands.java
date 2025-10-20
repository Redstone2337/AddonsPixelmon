// APDCommands.java
package net.redstone233.pixelmonaddon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.redstone233.pixelmonaddon.AddonsPixelmon;
import net.redstone233.pixelmonaddon.config.AnnouncementConfig;
import net.redstone233.pixelmonaddon.config.ConfigManager;
import net.redstone233.pixelmonaddon.network.AnnouncementPayload;
import net.redstone233.pixelmonaddon.screen.AnnouncementScreen;

import java.util.Map;

public class APDCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("apd")
                        .requires(source -> source.hasPermission(2)) // 需要权限等级2
                        .then(Commands.literal("debug")
                                .then(Commands.literal("show")
                                        .executes(APDCommands::showAnnouncement)
                                )
                                .then(Commands.literal("reload")
                                        .executes(APDCommands::reloadConfig)
                                )
                                .then(Commands.literal("info")
                                        .executes(APDCommands::showConfigInfo)
                                )
                                .then(Commands.literal("hash")
                                        .executes(APDCommands::showHashInfo)
                                )
                                .then(Commands.literal("test")
                                        .then(Commands.literal("server")
                                                .executes(APDCommands::testServerAnnouncement)
                                        )
                                        .then(Commands.literal("client")
                                                .executes(APDCommands::testClientAnnouncement)
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .executes(APDCommands::resetDisplayedHash)
                                )
                        )
                        .then(Commands.literal("worlds")
                                .executes(APDCommands::showWorldHashInfo)
                        )
                        .then(Commands.literal("reset")
                                .executes(APDCommands::resetDisplayedHash)
                                .then(Commands.literal("world")
                                        .executes(APDCommands::resetWorldHash)
                                )
                                .then(Commands.literal("allworlds")
                                        .executes(APDCommands::resetAllWorldHashes)
                                )
                        )
                        .then(Commands.literal("version")
                                .executes(APDCommands::showVersion)
                        )
        );
    }

    private static int showAnnouncement(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayer player) {
                AnnouncementConfig config = ConfigManager.createAnnouncementConfig();
                player.sendSystemMessage(Component.literal("§a正在显示公告屏幕..."));

                // 对于服务器命令，发送网络包
                String worldHash = "debug-command-" + System.currentTimeMillis();
                AnnouncementPayload payload = new AnnouncementPayload(config, worldHash);
                player.connection.send(payload);

                return 1;
            } else {
                source.sendFailure(Component.literal("§c只有玩家可以使用此命令"));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c显示公告时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("显示公告失败", e);
            return 0;
        }
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            AnnouncementConfig config = ConfigManager.reloadConfig();
            if (config.isValid()) {
                source.sendSuccess(() -> Component.literal("§a配置重新加载成功！"), true);
                source.sendSuccess(() -> Component.literal("§7主标题: " + config.mainTitle), false);
                source.sendSuccess(() -> Component.literal("§7内容行数: " + config.announcementContent.size()), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("§c配置验证失败"));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c重新加载配置时出错: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("重新加载配置失败", e);
            return 0;
        }
    }

    private static int showConfigInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            AnnouncementConfig config = ConfigManager.createAnnouncementConfig();

            source.sendSuccess(() -> Component.literal("§6=== APD 配置信息 ==="), false);
            source.sendSuccess(() -> Component.literal("§7主标题: §f" + config.mainTitle), false);
            source.sendSuccess(() -> Component.literal("§7副标题: §f" + config.subTitle), false);
            source.sendSuccess(() -> Component.literal("§7内容行数: §f" + config.announcementContent.size()), false);
            source.sendSuccess(() -> Component.literal("§7显示图标: §f" + config.showIcon), false);
            source.sendSuccess(() -> Component.literal("§7自定义RGB: §f" + config.useCustomRGB), false);
            source.sendSuccess(() -> Component.literal("§7滚动速度: §f" + config.scrollSpeed), false);
            source.sendSuccess(() -> Component.literal("§7自定义背景: §f" + config.useCustomAnnouncementBackground), false);
            source.sendSuccess(() -> Component.literal("§7进入世界显示: §f" + ConfigManager.shouldShowOnWorldEnter()), false);
            source.sendSuccess(() -> Component.literal("§7调试模式: §f" + ConfigManager.isDebugMode()), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c获取配置信息时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("显示配置信息失败", e);
            return 0;
        }
    }

    private static int showHashInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            String configHash = ConfigManager.getConfigHash();
            String lastDisplayedHash = ConfigManager.getLastDisplayedHash();
            boolean hasChanged = ConfigManager.hasConfigChanged();

            source.sendSuccess(() -> Component.literal("§6=== APD 哈希信息 ==="), false);
            source.sendSuccess(() -> Component.literal("§7当前配置哈希: §f" + configHash), false);
            source.sendSuccess(() -> Component.literal("§7最后显示哈希: §f" + lastDisplayedHash), false);
            source.sendSuccess(() -> Component.literal("§7配置是否变化: §f" + hasChanged), false);

            if (source.getEntity() instanceof ServerPlayer player) {
                String worldName = player.level().dimension().location().toString();
                String worldHash = ConfigManager.getWorldSpecificHash(worldName);
                source.sendSuccess(() -> Component.literal("§7世界特定哈希: §f" + worldHash), false);
                source.sendSuccess(() -> Component.literal("§7世界名称: §f" + worldName), false);
            }

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c获取哈希信息时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("显示哈希信息失败", e);
            return 0;
        }
    }

    private static int testServerAnnouncement(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayer player) {
                source.sendSuccess(() -> Component.literal("§a测试服务器公告发送..."), true);

                // 创建测试配置
                AnnouncementConfig testConfig = createTestConfig();
                String testHash = "test-server-" + System.currentTimeMillis();

                AnnouncementPayload payload = new AnnouncementPayload(testConfig, testHash);
                player.connection.send(payload);

                return 1;
            } else {
                source.sendFailure(Component.literal("§c只有玩家可以使用此命令"));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c测试服务器公告时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("测试服务器公告失败", e);
            return 0;
        }
    }

    private static int testClientAnnouncement(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayer player) {
                source.sendSuccess(() -> Component.literal("§a测试客户端公告显示..."), true);

                // 对于客户端测试，直接打开屏幕（如果可能）
                // 注意：这通常需要在客户端执行
                source.sendSuccess(() -> Component.literal("§e注意：客户端测试需要在客户端控制台执行"), false);

                return 1;
            } else {
                source.sendFailure(Component.literal("§c只有玩家可以使用此命令"));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c测试客户端公告时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("测试客户端公告失败", e);
            return 0;
        }
    }

    private static int resetDisplayedHash(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            // 重置最后显示的哈希值
            ConfigManager.setLastDisplayedHash("");

            source.sendSuccess(() -> Component.literal("§a已重置最后显示的哈希值"), true);
            source.sendSuccess(() -> Component.literal("§7下次进入世界将强制显示公告"), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c重置显示哈希时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("重置显示哈希失败", e);
            return 0;
        }
    }

    private static int showVersion(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            source.sendSuccess(() -> Component.literal("§6Pixelmon Addons Mod"), false);
            source.sendSuccess(() -> Component.literal("§7版本: §f" + AddonsPixelmon.VERSION), false);
            source.sendSuccess(() -> Component.literal("§7Mod ID: §f" + AddonsPixelmon.MOD_ID), false);
            source.sendSuccess(() -> Component.literal("§7作者: §fRedstone233"), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c获取版本信息时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("显示版本信息失败", e);
            return 0;
        }
    }

    private static AnnouncementConfig createTestConfig() {
        AnnouncementConfig config = new AnnouncementConfig();
        config.mainTitle = "§6测试公告";
        config.subTitle = "§e调试模式";
        config.announcementContent = java.util.List.of(
                "§a这是一个测试公告",
                " ",
                "§f通过 §b/apd debug test §f命令生成",
                "§f时间: " + java.time.LocalDateTime.now(),
                " ",
                "§c这是测试内容，用于验证公告功能"
        );
        config.confirmButtonText = "知道了";
        config.submitButtonText = "测试链接";
        config.buttonLink = "https://github.com/Redstone233";
        config.showIcon = true;
        config.iconPath = "minecraft:textures/item/paper.png";
        config.iconWidth = 32;
        config.iconHeight = 32;
        config.iconTextSpacing = 10;
        config.useCustomRGB = true;
        config.mainTitleColor = 0xFFAA00; // 金色
        config.subTitleColor = 0xFFFF55;  // 浅黄色
        config.contentColor = 0xFFFFFF;   // 白色
        config.scrollSpeed = 2;
        config.useCustomAnnouncementBackground = false;
        config.announcementBackgroundPath = "";

        return config;
    }

    // 在 APDCommands.java 中添加新的命令方法

    private static int showWorldHashInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Map<String, String> worldHashes = ConfigManager.getAllWorldHashes();

            source.sendSuccess(() -> Component.literal("§6=== 世界哈希信息 ==="), false);
            source.sendSuccess(() -> Component.literal("§7存储的世界数量: §f" + worldHashes.size()), false);

            if (worldHashes.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§7暂无世界哈希记录"), false);
            } else {
                worldHashes.forEach((worldName, hash) -> {
                    source.sendSuccess(() -> Component.literal("§7" + worldName + ": §f" + hash), false);
                });
            }

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c获取世界哈希信息时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("显示世界哈希信息失败", e);
            return 0;
        }
    }

    private static int resetWorldHash(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayer player) {
                String worldName = player.level().dimension().location().toString();
                ConfigManager.resetWorldHash(worldName);
                source.sendSuccess(() -> Component.literal("§a已重置世界 " + worldName + " 的显示状态"), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§c只有玩家可以使用此命令"));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c重置世界哈希时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("重置世界哈希失败", e);
            return 0;
        }
    }

    private static int resetAllWorldHashes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            ConfigManager.resetAllWorldHashes();
            source.sendSuccess(() -> Component.literal("§a已重置所有世界的显示状态"), true);
            source.sendSuccess(() -> Component.literal("§7所有世界下次进入都会显示公告"), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c重置所有世界哈希时发生错误: " + e.getMessage()));
            AddonsPixelmon.LOGGER.error("重置所有世界哈希失败", e);
            return 0;
        }
    }
}