// ClientPayloadHandler.java
package net.redstone233.pixelmonaddon.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.redstone233.pixelmonaddon.AddonsPixelmon;
import net.redstone233.pixelmonaddon.screen.AnnouncementScreen;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClientPayloadHandler {

    private static final String DISPLAYED_WORLDS_FILE = "displayed_worlds.dat";

    public static void handleAnnouncement(final AnnouncementPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Set<String> displayedWorlds = loadDisplayedWorldHashes();
            if (!displayedWorlds.contains(payload.worldHash())) {
                Minecraft.getInstance().setScreen(new AnnouncementScreen(payload.config()));
                displayedWorlds.add(payload.worldHash());
                saveDisplayedWorldHashes(displayedWorlds);
            }
        });
    }

    private static Set<String> loadDisplayedWorldHashes() {
        try {
            File file = getDisplayedWorldsFile();
            if (file.exists()) {
                CompoundTag nbt = NbtIo.read((DataInput) getDisplayedWorldsFile());
                if (nbt != null) {
                    return new HashSet<>(nbt.getAllKeys());
                }
            }
        } catch (IOException e) {
            AddonsPixelmon.LOGGER.error("发生错误：", e);
        }
        return new HashSet<>();
    }

    private static void saveDisplayedWorldHashes(Set<String> worldHashes) {
        try {
            CompoundTag nbt = new CompoundTag();
            for (String hash : worldHashes) {
                nbt.putBoolean(hash, true);
            }
            NbtIo.write(nbt, (DataOutput) getDisplayedWorldsFile());
        } catch (IOException e) {
            AddonsPixelmon.LOGGER.error("发生错误：", e);
        }
    }

    private static File getDisplayedWorldsFile() {
        return new File(Minecraft.getInstance().gameDirectory, "config/" + DISPLAYED_WORLDS_FILE);
    }
}