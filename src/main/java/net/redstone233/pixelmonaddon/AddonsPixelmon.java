package net.redstone233.pixelmonaddon;

import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.redstone233.pixelmonaddon.handlers.WorldEnterHandler;
import net.redstone233.pixelmonaddon.network.NetworkHandler;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AddonsPixelmon.MOD_ID)
public class AddonsPixelmon {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "apd";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AddonsPixelmon(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("开始初始化模组内容...");
        long startTime = System.currentTimeMillis();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("通用更新完成，耗时： {}ms", System.currentTimeMillis() - startTime);
// Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (AddonsPixelmon) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        IEventBus gameEventBus = NeoForge.EVENT_BUS;

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        LOGGER.info("模组物品栏创建完成，耗时： {}ms", System.currentTimeMillis() - startTime);

        // 注册事件处理器
        gameEventBus.addListener(WorldEnterHandler::onPlayerLoggedIn);
        gameEventBus.addListener(WorldEnterHandler::onPlayerChangedDimension);
        gameEventBus.addListener(WorldEnterHandler::onPlayerRespawn);
        LOGGER.info("事件处理器注册完成，耗时： {}ms", System.currentTimeMillis() - startTime);

        // 注册网络处理器
        modEventBus.addListener(NetworkHandler::registerPayloadHandlers);
        LOGGER.info("网络处理器注册完成，耗时： {}ms", System.currentTimeMillis() - startTime);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "AP-Settings.toml");
        LOGGER.info("配置文件注册完成，耗时： {}ms", System.currentTimeMillis() - startTime);
        // 初始化配置
        Config.init(modEventBus, modContainer);
        LOGGER.info("配置文件初始化完成，耗时： {}ms", System.currentTimeMillis() - startTime);
        LOGGER.info("模组数据加载完成，耗时： {}ms", System.currentTimeMillis() - startTime);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
