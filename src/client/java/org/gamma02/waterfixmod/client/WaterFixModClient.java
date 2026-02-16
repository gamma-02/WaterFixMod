package org.gamma02.waterfixmod.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.network.chat.Component;

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
    }
}
