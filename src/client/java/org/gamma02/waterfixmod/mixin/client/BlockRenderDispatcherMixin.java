package org.gamma02.waterfixmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.gamma02.waterfixmod.client.Config;
import org.gamma02.waterfixmod.client.SpecialWaterTesselation;
import org.gamma02.waterfixmod.client.WaterFixModClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

    @Shadow
    @Final
    private MaterialSet materials;

    @Unique
    SpecialWaterTesselation tesselator = null;

    //replace the original tesselator when we are configured to
    @WrapOperation(method = "renderLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"))
    void invokeSpecialTesselation(LiquidBlockRenderer instance, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Operation<Void> original){
        if(Config.CONFIG.useSpecialWaterTesselationForNormalWater()){
             Objects.requireNonNull(this.tesselator).tesselateWithoutSmoothing(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
        }else{
            original.call(instance, blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
        }
    }

    //render fluid inside of blocks when renderSingleBlock is called
    @Inject(method = "renderSingleBlock", at = @At("HEAD"))
    void invokeSpecialTesselationForFluidsInSingleBlockRendering(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci){
        if(blockState.getFluidState().getType() == Fluids.EMPTY)
            return;

        try {
            (Objects.requireNonNull(this.tesselator)).tesselateWithoutSmoothing(multiBufferSource, blockState, poseStack);
        } catch (Throwable var9) {
            CrashReport crashReport = CrashReport.forThrowable(var9, "Tesselating liquid without world");
            throw new ReportedException(crashReport);
        }

    }


    @Inject(method = "onResourceManagerReload", at = @At("HEAD"))
    void instantiateSpecialWaterTesselation(ResourceManager resourceManager, CallbackInfo ci){
        tesselator = new SpecialWaterTesselation(this.materials);
    }
}
