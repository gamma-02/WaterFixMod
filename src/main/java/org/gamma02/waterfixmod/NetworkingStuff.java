package org.gamma02.waterfixmod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class NetworkingStuff {

    public static final Identifier HELLO_PACKET = Identifier.parse("waterfixmod:hello");
    public static final Identifier DATA_REQUEST_PACKET = Identifier.parse("waterfixmod:request_block_data");
    public static final Identifier DATA_RESPONSE_PACKET = Identifier.parse("waterfixmod:send_block_data");


    //ONLY USED ON THE SERVER
    //List of clients that have sent our "hello" packet,
    //so we know they've got our mod on them.
    public static ArrayList<UUID> CLIENTS_WITH_MOD = new ArrayList<>();

    //this method is called on the main thread whenever the server receives a packet with id "waterfixmod:hello"
    public static void handleHelloPayload(ServerPlayNetworking.Context helloContext) {
        CLIENTS_WITH_MOD.add(helloContext.player().getUUID());
        ServerPlayNetworking.registerReceiver(
                helloContext.player().connection,
                NetworkingStuff.DataRequestPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() -> sendDataRequestResponse(payload.entityUUIDToRequest(), ctx))
        );
    }

    //this constructs and sends the response to a received DataRequestPayload
    private static void sendDataRequestResponse(UUID idFromPayload, ServerPlayNetworking.Context ctx) {
        Entity entityFromPacket = ctx.player().level().getEntity(idFromPayload);

        if(!(entityFromPacket instanceof FallingBlockEntity fallingBlock))
            return;

        ctx.responseSender().sendPacket(
                new DataResponsePayload(idFromPayload, fallingBlock.blockData)
        );
    }


    //custom payload classes, they just hold, decode, and write the data
    public record HelloPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HelloPayload> TYPE = new CustomPacketPayload.Type<>(HELLO_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, HelloPayload> CODEC = StreamCodec.of(HelloPayload::write, HelloPayload::new);

        private static void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, HelloPayload helloPayload) {}

        public HelloPayload(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this();
        }

        @Override
        public @NonNull Type<HelloPayload> type() {
            return TYPE;
        }

    }

    public record DataRequestPayload(UUID entityUUIDToRequest) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DataRequestPayload> TYPE = new CustomPacketPayload.Type<>(DATA_REQUEST_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, DataRequestPayload> CODEC = StreamCodec.of(DataRequestPayload::write, DataRequestPayload::new);

        public DataRequestPayload(RegistryFriendlyByteBuf byteBuf) {
            this(byteBuf.readUUID());
        }

        private static void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, DataRequestPayload dataRequestPayload) {
            registryFriendlyByteBuf.writeUUID(dataRequestPayload.entityUUIDToRequest);
        }

        @Override
        public @NonNull Type<DataRequestPayload> type() {
            return TYPE;
        }
    }

    public record DataResponsePayload(UUID requestedEntity,
                                      CompoundTag blockEntityData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DataResponsePayload> TYPE = new CustomPacketPayload.Type<>(DATA_REQUEST_PACKET);
        public static final StreamCodec<RegistryFriendlyByteBuf, DataResponsePayload> CODEC = StreamCodec.of(DataResponsePayload::write, DataResponsePayload::new);

        public DataResponsePayload(RegistryFriendlyByteBuf byteBuf) {
            this(byteBuf.readUUID(), byteBuf.readNbt());
        }

        private static void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, DataResponsePayload dataResponsePayload) {
            registryFriendlyByteBuf.writeUUID(dataResponsePayload.requestedEntity);
            registryFriendlyByteBuf.writeNbt(dataResponsePayload.blockEntityData);
        }

        @Override
        public @NonNull Type<DataResponsePayload> type() {
            return TYPE;
        }
    }


}
