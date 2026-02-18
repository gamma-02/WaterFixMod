package org.gamma02.waterfixmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class WaterFixMod implements ModInitializer {


    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(NetworkingStuff.HelloPayload.TYPE, NetworkingStuff.HelloPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkingStuff.DataRequestPayload.TYPE, NetworkingStuff.DataRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkingStuff.DataResponsePayload.TYPE, NetworkingStuff.DataResponsePayload.CODEC);

        //register the receiver for the hello packet
        ServerPlayNetworking.registerGlobalReceiver(
            NetworkingStuff.HelloPayload.TYPE,
            (payload, ctx) -> ctx.server().execute(() -> NetworkingStuff.handleHelloPayload(ctx))
        );

        //handle the disconnection of a player
        ServerPlayConnectionEvents.DISCONNECT.register((disconnectListener, server) -> {
            //unregister the listener for the request packet once the player disconnects
            if(NetworkingStuff.CLIENTS_WITH_MOD.contains(disconnectListener.player.getUUID()))
                ServerPlayNetworking.unregisterReceiver(disconnectListener, NetworkingStuff.DATA_REQUEST_PACKET);

            NetworkingStuff.CLIENTS_WITH_MOD.remove(disconnectListener.player.getUUID());
        });
    }
}
