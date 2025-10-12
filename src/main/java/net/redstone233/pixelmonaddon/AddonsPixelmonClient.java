package net.redstone233.pixelmonaddon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.redstone233.pixelmonaddon.screen.AnnouncementScreen;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AddonsPixelmon.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = AddonsPixelmon.MOD_ID, value = Dist.CLIENT)
public class AddonsPixelmonClient {
    public AddonsPixelmonClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // 在标题屏幕显示时弹出公告
        if (event.getScreen() instanceof TitleScreen && AnnouncementScreen.shouldShowAnnouncement()) {
            Config.logDebug("检测到标题屏幕，准备显示公告");
            event.setNewScreen(new AnnouncementScreen(event.getScreen()));
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        AddonsPixelmon.LOGGER.info("HELLO FROM CLIENT SETUP");
        AddonsPixelmon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
