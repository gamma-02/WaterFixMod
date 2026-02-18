package org.gamma02.waterfixmod.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.gamma02.waterfixmod.NetworkingStuff;

public class WaterFixModClient implements ClientModInitializer {

    public static boolean UseLocalBiomeTint = true;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((t) -> {
            Config.reloadConfig();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, thing) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reloadWaterFixConfig").executes(context -> {
                context.getSource().sendFeedback(Component.literal("Reloading config!"));
                Config.reloadConfig();

                return 1;
            }));
        });

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            ClientPlayNetworking.send(new NetworkingStuff.HelloPayload());
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingStuff.DataResponsePayload.TYPE, ((payload, context) -> {
            context.client().execute(() -> {
                Entity entityFromPayload = context.client().level.getEntity(payload.requestedEntity());

                if(!(entityFromPayload instanceof FallingBlockEntity fallingBlock))
                    return;

                fallingBlock.blockData = payload.blockEntityData();
            });
        }));
    }


}
