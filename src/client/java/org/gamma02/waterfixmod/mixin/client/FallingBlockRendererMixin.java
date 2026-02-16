package org.gamma02.waterfixmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.gamma02.waterfixmod.client.Config;
import org.gamma02.waterfixmod.client.FallingBlockSubmitter;
import org.gamma02.waterfixmod.client.HasRendererNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockRendererMixin {

    FallingBlockSubmitter submitter = new FallingBlockSubmitter();

    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
    public RenderShape wrapGetRenderState(BlockState instance, Operation<RenderShape> original){
        if(Config.CONFIG.replaceFallingBlockRendering())
            return RenderShape.MODEL;
        else
            return original.call(instance);

    }

    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitMovingBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/MovingBlockRenderState;)V"))
    public void wrapSubmitMovingBlock(SubmitNodeCollector instance, PoseStack poseStack, MovingBlockRenderState movingBlockRenderState, Operation<Void> original, FallingBlockRenderState state){
        if(Config.CONFIG.replaceFallingBlockRendering()) {
            submitter.submitForFallingBlockRendering(instance, poseStack, movingBlockRenderState, state);
        }else{
            original.call(instance, poseStack, movingBlockRenderState);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/FallingBlockEntity;Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;F)V", at = @At("HEAD"))
    public void appendTileEntityDataToRenderState(FallingBlockEntity fallingBlockEntity, FallingBlockRenderState fallingBlockRenderState, float f, CallbackInfo ci){
        ((HasRendererNBT) fallingBlockRenderState).waterFixMod$setNBT(fallingBlockEntity.blockData);
    }

}
