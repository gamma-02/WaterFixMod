package org.gamma02.waterfixmod.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.gamma02.waterfixmod.NetworkingStuff;
import org.gamma02.waterfixmod.mixin.client.LevelRenderStateAccessor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FallingBlockSubmitter {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ConcurrentLinkedQueue<UUID> ENTITIES_TO_TRY_REMOVE = new ConcurrentLinkedQueue<>();

    public final HashMap<UUID, BlockEntity> blockEntityCache = new HashMap<>();

    public void submitForFallingBlockRendering(SubmitNodeCollector instance, PoseStack poseStack,
                                               MovingBlockRenderState movingBlockRenderState,
                                               FallingBlockRenderState state) {

        Block b = state.movingBlockRenderState.blockState.getBlock();

        if (!(b instanceof BedBlock) && !(b instanceof AbstractBannerBlock) && !(b instanceof AbstractChestBlock<?>))
            instance.submitBlock(poseStack, movingBlockRenderState.blockState, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);


        if (Config.CONFIG.renderBlockEntitesInFallingBlocks() && b instanceof EntityBlock entityBlock) {
            getAndSubmitBlockEntity(instance, poseStack, movingBlockRenderState, entityBlock, state);
        }
    }

    private void getAndSubmitBlockEntity(SubmitNodeCollector instance, PoseStack poseStack,
                                         MovingBlockRenderState movingBlockRenderState,
                                         EntityBlock entityBlock, FallingBlockRenderState state) {

        UUID id = ((HasExtraBlockData) state).waterFixMod$getUUID();

        BlockEntity entity = this.blockEntityCache.computeIfAbsent(id, (k) -> constructBlockEntity(movingBlockRenderState, entityBlock, id));

        if (entity == null)
            return;

        if(!entity.getType().isValid(movingBlockRenderState.blockState))
            this.blockEntityCache.replace(id, constructBlockEntity(movingBlockRenderState, entityBlock, id));


        var blockRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);

        if (blockRenderer == null)
            return;

        if (((HasExtraBlockData) state).waterFixMod$getNBT() != null) {
            tryLoadNBT((HasExtraBlockData) state, entity);
        }

        BlockEntityRenderState blockEntityRenderState = blockRenderer.createRenderState();
        blockRenderer.extractRenderState(entity, blockEntityRenderState, 0, ((LevelRenderStateAccessor) Minecraft.getInstance().levelRenderer)
                        .getLevelRenderState().cameraRenderState.pos,
                null);

        blockRenderer.submit(
                blockEntityRenderState,
                poseStack,
                instance,
                ((LevelRenderStateAccessor) Minecraft.getInstance().levelRenderer)
                        .getLevelRenderState().cameraRenderState
        );
    }

    private static void tryLoadNBT(HasExtraBlockData state, BlockEntity entity) {
        try {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
                assert Minecraft.getInstance().level != null;
                entity.loadWithComponents(TagValueInput.create(scopedCollector, Minecraft.getInstance().level.registryAccess(), Objects.requireNonNull(state.waterFixMod$getNBT())));
            }
        } catch (Throwable var11) {
            LOGGER.error("Failed to load rendering data for block entity {}, with data {}", entity.getType(), state.waterFixMod$getNBT(), var11);
        }
    }

    private static @Nullable BlockEntity constructBlockEntity(
            MovingBlockRenderState movingBlockRenderState,
            EntityBlock entityBlock,
            UUID id) {
        BlockEntity nbe = entityBlock.newBlockEntity(movingBlockRenderState.blockPos, movingBlockRenderState.blockState);

        if (nbe != null && Minecraft.getInstance().level != null)
            nbe.setLevel(Minecraft.getInstance().level);

        sendFallingBlockEntityDataRequest(id);


        return nbe;
    }

    public void deleteDeadBlockEntities() {
        Level l = Minecraft.getInstance().level;
        assert l != null;

        while(!ENTITIES_TO_TRY_REMOVE.isEmpty()){
            UUID id = ENTITIES_TO_TRY_REMOVE.remove();

            Entity entity = l.getEntity(id);
            if(entity == null || entity.isRemoved()){
                blockEntityCache.remove(id);
            }
        }
    }

    public static void sendFallingBlockEntityDataRequest(UUID entity){
        ClientPlayNetworking.send(new NetworkingStuff.DataRequestPayload(entity));
    }
}
