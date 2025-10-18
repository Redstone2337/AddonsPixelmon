// NetworkHandler.java
package net.redstone233.pixelmonaddon.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                AnnouncementPayload.TYPE,
                AnnouncementPayload.STREAM_CODEC,
                ClientPayloadHandler::handleAnnouncement
        );
    }
}