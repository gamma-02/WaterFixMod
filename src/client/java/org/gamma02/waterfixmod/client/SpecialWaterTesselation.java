package org.gamma02.waterfixmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpecialWaterTesselation {
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;

    // /execute in minecraft:overworld run tp @s 4.22 -57.07 3.87 -1389.22 35.07

    public static int defaultLightColor = 0xf00000;

    public final TextureAtlasSprite lavaStill;
    public final TextureAtlasSprite lavaFlowing;
    public final TextureAtlasSprite waterStill;
    public final TextureAtlasSprite waterFlowing;
    public final TextureAtlasSprite waterOverlay;

    public SpecialWaterTesselation(MaterialSet materialSet) {
        this.lavaStill = materialSet.get(ModelBakery.LAVA_STILL);
        this.lavaFlowing = materialSet.get(ModelBakery.LAVA_FLOW);
        this.waterStill = materialSet.get(ModelBakery.WATER_STILL);
        this.waterFlowing = materialSet.get(ModelBakery.WATER_FLOW);
        this.waterOverlay = materialSet.get(ModelBakery.WATER_OVERLAY);

    }

    public void tesselateWithoutSmoothing(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        boolean isFluidLava = fluidState.is(FluidTags.LAVA);

        TextureAtlasSprite stillTexture = isFluidLava ? this.lavaStill : this.waterStill;
        TextureAtlasSprite flowingTexture = isFluidLava ? this.lavaFlowing : this.waterFlowing;

        //potential tint:
        //4159204
        int waterTint = isFluidLava ? 16777215 : !WaterFixModClient.UseLocalBiomeTint ? 4159204 : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);

        //convert packed color to floats
        float f = (waterTint >> 16 & 0xFF) / 255.0F;
        float g = (waterTint >> 8 & 0xFF) / 255.0F;
        float h = (waterTint & 0xFF) / 255.0F;


        boolean shouldRenderDownFace = true;
        boolean shouldRenderNorthFace = true;
        boolean shouldRenderSouthFace = true;
        boolean shouldRenderWestFace = true;
        boolean shouldRenderEastFace = true;

        //get shades (these are almost factors? for like. light level? maybe? idk???)
        float downShade = blockAndTintGetter.getShade(Direction.DOWN, true);
        float upShade = blockAndTintGetter.getShade(Direction.UP, true);
        float northShade = blockAndTintGetter.getShade(Direction.NORTH, true);
        float westShade = blockAndTintGetter.getShade(Direction.WEST, true);

        Fluid fluid = fluidState.getType();

        float fluidHeight = this.getUnsmoothedHeight(fluid, blockState, fluidState);


        //IS THIS A BLOCK'S SUBCHUNK POSITION?????
        float chunkX = blockPos.getX() & 15;
        float chunkY = blockPos.getY() & 15;
        float chunkZ = blockPos.getZ() & 15;

        //this was left in for some reason
//        float v = 0.001F;

        float faceOffset = 0.0001F;

        //add up face verts?

//        Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);

        float x;
        float z;
        float ab;
        float ad;
        float y;
        float aa;
        float ac;
        float ae;

        x = stillTexture.getU(0.0F);
        y = stillTexture.getV(0.0F);
        z = x;
        aa = stillTexture.getV(1.0F);
        ab = stillTexture.getU(1.0F);
        ac = aa;
        ad = ab;
        ae = y;

        //add up face vertices
        int aj = this.getLightColor(blockAndTintGetter, blockPos);
        float ag = upShade * f;
        float ah = upShade * g;
        float ai = upShade * h;
        this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + fluidHeight - 0.0001f, chunkZ + 0.0F, ag, ah, ai, x, y, aj);
        this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + fluidHeight - 0.0001f, chunkZ + 1.0F, ag, ah, ai, z, aa, aj);
        this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + fluidHeight - 0.0001f, chunkZ + 1.0F, ag, ah, ai, ab, ac, aj);
        this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + fluidHeight - 0.0001f, chunkZ + 0.0F, ag, ah, ai, ad, ae, aj);
        if (Config.CONFIG.renderBackfacesForFluids()){
            this.vertex(vertexConsumer, +0.0F, +fluidHeight - 0.001f, +0.0F, ag, ah, ai, x, y, aj);
            this.vertex(vertexConsumer, +1.0F, +fluidHeight - 0.001f, +0.0F, ag, ah, ai, ad, ae, aj);
            this.vertex(vertexConsumer, +1.0F, +fluidHeight - 0.001f, +1.0F, ag, ah, ai, ab, ac, aj);
            this.vertex(vertexConsumer, +0.0F, +fluidHeight - 0.001f, +1.0F, ag, ah, ai, z, aa, aj);
        }


        //add down face vertices
        float xx = stillTexture.getU0();
        float zx = stillTexture.getU1();
        float abx = stillTexture.getV0();
        float adxd = stillTexture.getV1();
        int ak = this.getLightColor(blockAndTintGetter, blockPos.below());
        float aax = downShade * f;
        float acx = downShade * g;
        float aex = downShade * h;
        this.vertex(vertexConsumer, chunkX, chunkY + faceOffset, chunkZ + 1.0F, aax, acx, aex, xx, adxd, ak);
        this.vertex(vertexConsumer, chunkX, chunkY + faceOffset, chunkZ, aax, acx, aex, xx, abx, ak);
        this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + faceOffset, chunkZ, aax, acx, aex, zx, abx, ak);
        this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + faceOffset, chunkZ + 1.0F, aax, acx, aex, zx, adxd, ak);
        if(Config.CONFIG.renderBackfacesForFluids()){
            this.vertex(vertexConsumer,  + 1.0F,  + faceOffset, 0, aax, acx, aex, zx, abx, ak);
            this.vertex(vertexConsumer, 0,  + faceOffset, 0, aax, acx, aex, xx, abx, ak);
            this.vertex(vertexConsumer, 0,  + faceOffset,  + 1.0F, aax, acx, aex, xx, adxd, ak);
            this.vertex(vertexConsumer,  + 1.0F,  + faceOffset,  + 1.0F, aax, acx, aex, zx, adxd, ak);
        }


        int lightColor = this.getLightColor(blockAndTintGetter, blockPos);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float adx;
            float yx;
            float x1;
            float z1;
            float x2;
            float z2;
            boolean renderCurrentDirectionFace;
            switch (direction) {
                case NORTH:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = chunkX;
                    x2 = chunkX + 1.0F;
                    z1 = chunkZ + 0.001F;
                    z2 = chunkZ + 0.001F;
                    renderCurrentDirectionFace = shouldRenderNorthFace;
                    break;
                case SOUTH:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = chunkX + 1.0F;
                    x2 = chunkX;
                    z1 = chunkZ + 1.0F - 0.001F;
                    z2 = chunkZ + 1.0F - 0.001F;
                    renderCurrentDirectionFace = shouldRenderSouthFace;
                    break;
                case WEST:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = chunkX + 0.001F;
                    x2 = chunkX + 0.001F;
                    z1 = chunkZ + 1.0F;
                    z2 = chunkZ;
                    renderCurrentDirectionFace = shouldRenderWestFace;
                    break;
                default:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = chunkX + 1.0F - 0.001F;
                    x2 = chunkX + 1.0F - 0.001F;
                    z1 = chunkZ;
                    z2 = chunkZ + 1.0F;
                    renderCurrentDirectionFace = shouldRenderEastFace;
            }

            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite currentTexture = flowingTexture;
            if (!isFluidLava) {
                Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
                if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
                    currentTexture = this.waterOverlay;
                }
            }

            //UV stuff
            float textureU1 = currentTexture.getU(0.0F);
            float textureU2 = currentTexture.getU(0.5F);
            float textureVAdx = currentTexture.getV((1.0F - adx) * 0.5F);
            float textureVYx = currentTexture.getV((1.0F - yx) * 0.5F);
            float textureV = currentTexture.getV(0.5F);

            float shade = direction.getAxis() == Direction.Axis.Z ? northShade : westShade;

            //color
            float red = upShade * shade * f;
            float green = upShade * shade * g;
            float blue = upShade * shade * h;

            //add the vertices
            this.vertex(vertexConsumer, x1, chunkY + adx, z1, red, green, blue, textureU1, textureVAdx, lightColor);
            this.vertex(vertexConsumer, x2, chunkY + yx, z2, red, green, blue, textureU2, textureVYx, lightColor);
            this.vertex(vertexConsumer, x2, chunkY + faceOffset, z2, red, green, blue, textureU2, textureV, lightColor);
            this.vertex(vertexConsumer, x1, chunkY + faceOffset, z1, red, green, blue, textureU1, textureV, lightColor);
            if (currentTexture != this.waterOverlay && Config.CONFIG.renderBackfacesForFluids()) {
                this.vertex(vertexConsumer, (float) (x1 - direction.getUnitVec3().x() * 0.001f), chunkY + faceOffset, (float) (z1 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU1, textureV, lightColor);
                this.vertex(vertexConsumer, (float) (x2 - direction.getUnitVec3().x() * 0.001f), chunkY + faceOffset, (float) (z2 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU2, textureV, lightColor);
                this.vertex(vertexConsumer, (float) (x2 - direction.getUnitVec3().x() * 0.001f), chunkY + yx, (float) (z2 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU2, textureVYx, lightColor);
                this.vertex(vertexConsumer, (float) (x1 - direction.getUnitVec3().x() * 0.001f), chunkY + adx, (float) (z1 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU1, textureVAdx, lightColor);
            }
        }
    }

    public void tesselateWithoutSmoothing(MultiBufferSource multiBufferSource, BlockState blockState, PoseStack poseStack) {
        BlockAndTintGetter blockAndTintGetter = Minecraft.getInstance().level;
        FluidState fluidState = blockState.getFluidState();


        boolean isFluidLava = fluidState.is(FluidTags.LAVA);

        PoseStack.Pose pose = poseStack.last();

        TextureAtlasSprite stillTexture = isFluidLava ? this.lavaStill : this.waterStill;
        TextureAtlasSprite currentTexture = isFluidLava ? this.lavaFlowing : this.waterFlowing;

        VertexConsumer stillConsumer = multiBufferSource.getBuffer(RenderTypes.itemEntityTranslucentCull(stillTexture.atlasLocation()));
        VertexConsumer flowingConsumer = multiBufferSource.getBuffer(RenderTypes.itemEntityTranslucentCull(currentTexture.atlasLocation()));


        //potential tint:
        //4159204
        int waterTint = isFluidLava ? 16777215 : 4159204;

        float f = (waterTint >> 16 & 0xFF) / 255.0F;
        float g = (waterTint >> 8 & 0xFF) / 255.0F;
        float h = (waterTint & 0xFF) / 255.0F;

        boolean shouldRenderNorthFace = true;
        boolean shouldRenderSouthFace = true;
        boolean shouldRenderWestFace = true;
        boolean shouldRenderEastFace = true;

        //get shades (these are almost factors? for like. light level? maybe? idk???)
        float downShade = blockAndTintGetter.getShade(Direction.DOWN, true);
        float upShade = blockAndTintGetter.getShade(Direction.UP, true);
        float northShade = blockAndTintGetter.getShade(Direction.NORTH, true);
        float westShade = blockAndTintGetter.getShade(Direction.WEST, true);

        Fluid fluid = fluidState.getType();

        float fluidHeight = this.getUnsmoothedHeight(fluid, blockState, fluidState);


        //this was left in for some reason
//        float v = 0.001F;

        float faceOffset = 0.0001F;

        //add up face verts?

        float x;
        float z;
        float ab;
        float ad;
        float y;
        float aa;
        float ac;
        float ae;
//        if (vec3.x == 0.0 && vec3.z == 0.0) {
        x = stillTexture.getU(0.0F);
        y = stillTexture.getV(0.0F);
        z = x;
        aa = stillTexture.getV(1.0F);
        ab = stillTexture.getU(1.0F);
        ac = aa;
        ad = ab;
        ae = y;

        //add up face vertices
        int aj = defaultLightColor;
        float ag = upShade * f;
        float ah = upShade * g;
        float ai = upShade * h;
        this.vertex(stillConsumer, pose, +0.0F, +fluidHeight - 0.0001f, +0.0F, ag, ah, ai, x, y, aj);
        this.vertex(stillConsumer, pose, +0.0F, +fluidHeight - 0.0001f, +1.0F, ag, ah, ai, z, aa, aj);
        this.vertex(stillConsumer, pose, +1.0F, +fluidHeight - 0.0001f, +1.0F, ag, ah, ai, ab, ac, aj);
        this.vertex(stillConsumer, pose, +1.0F, +fluidHeight - 0.0001f, +0.0F, ag, ah, ai, ad, ae, aj);
        if (Config.CONFIG.renderBackfacesForFluids()){
            this.vertex(stillConsumer, pose, +0.0F, +fluidHeight - 0.001f, +0.0F, ag, ah, ai, x, y, aj);
            this.vertex(stillConsumer, pose, +1.0F, +fluidHeight - 0.001f, +0.0F, ag, ah, ai, ad, ae, aj);
            this.vertex(stillConsumer, pose, +1.0F, +fluidHeight - 0.001f, +1.0F, ag, ah, ai, ab, ac, aj);
            this.vertex(stillConsumer, pose, +0.0F, +fluidHeight - 0.001f, +1.0F, ag, ah, ai, z, aa, aj);
        }


        //add down face vertices
        float xx = stillTexture.getU0();
        float zx = stillTexture.getU1();
        float abx = stillTexture.getV0();
        float adxd = stillTexture.getV1();
        int ak = defaultLightColor;
        float aax = downShade * f;
        float acx = downShade * g;
        float aex = downShade * h;
        this.vertex(stillConsumer, pose, 0,  + faceOffset,  + 1.0F, aax, acx, aex, xx, adxd, ak);
        this.vertex(stillConsumer, pose, 0,  + faceOffset, 0, aax, acx, aex, xx, abx, ak);
        this.vertex(stillConsumer, pose,  + 1.0F,  + faceOffset, 0, aax, acx, aex, zx, abx, ak);
        this.vertex(stillConsumer, pose,  + 1.0F,  + faceOffset,  + 1.0F, aax, acx, aex, zx, adxd, ak);
        if(Config.CONFIG.renderBackfacesForFluids()){

            this.vertex(stillConsumer, pose,  + 1.0F,  + faceOffset, 0, aax, acx, aex, zx, abx, ak);
            this.vertex(stillConsumer, pose, 0,  + faceOffset, 0, aax, acx, aex, xx, abx, ak);
            this.vertex(stillConsumer, pose, 0,  + faceOffset,  + 1.0F, aax, acx, aex, xx, adxd, ak);
            this.vertex(stillConsumer, pose,  + 1.0F,  + faceOffset,  + 1.0F, aax, acx, aex, zx, adxd, ak);

        }


        int lightColor = defaultLightColor;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float adx;
            float yx;
            float x1;
            float z1;
            float x2;
            float z2;
            boolean renderCurrentDirectionFace;
            switch (direction) {
                case NORTH:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = 0;
                    x2 = 0 + 1.0F;
                    z1 = 0 + 0.001F;
                    z2 = 0 + 0.001F;
                    renderCurrentDirectionFace = shouldRenderNorthFace;
                    break;
                case SOUTH:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = 0 + 1.0F;
                    x2 = 0;
                    z1 = 0 + 1.0F - 0.001F;
                    z2 = 0 + 1.0F - 0.001F;
                    renderCurrentDirectionFace = shouldRenderSouthFace;
                    break;
                case WEST:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = 0 + 0.001F;
                    x2 = 0 + 0.001F;
                    z1 = 0 + 1.0F;
                    z2 = 0;
                    renderCurrentDirectionFace = shouldRenderWestFace;
                    break;
                default:
                    adx = fluidHeight;
                    yx = fluidHeight;
                    x1 = 0 + 1.0F - 0.001F;
                    x2 = 0 + 1.0F - 0.001F;
                    z1 = 0;
                    z2 = 0 + 1.0F;
                    renderCurrentDirectionFace = shouldRenderEastFace;
            }

            //UV stuff
            float textureU1 = currentTexture.getU(0.0F);
            float textureU2 = currentTexture.getU(0.5F);
            float textureVAdx = currentTexture.getV((1.0F - adx) * 0.5F);
            float textureVYx = currentTexture.getV((1.0F - yx) * 0.5F);
            float textureV = currentTexture.getV(0.5F);

            float shade = direction.getAxis() == Direction.Axis.Z ? northShade : westShade;

            //color
            float red = upShade * shade * f;
            float green = upShade * shade * g;
            float blue = upShade * shade * h;

            //add the vertices
            this.vertex(flowingConsumer, pose, x1, 0 + adx, z1, red, green, blue, textureU1, textureVAdx, lightColor);
            this.vertex(flowingConsumer, pose, x2, 0 + yx, z2, red, green, blue, textureU2, textureVYx, lightColor);
            this.vertex(flowingConsumer, pose, x2, 0 + faceOffset, z2, red, green, blue, textureU2, textureV, lightColor);
            this.vertex(flowingConsumer, pose, x1, 0 + faceOffset, z1, red, green, blue, textureU1, textureV, lightColor);
            if (currentTexture != this.waterOverlay && Config.CONFIG.renderBackfacesForFluids()) {
                this.vertex(flowingConsumer, pose, (float) (x1 - direction.getUnitVec3().x() * 0.001f), 0 + faceOffset, (float) (z1 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU1, textureV, lightColor);
                this.vertex(flowingConsumer, pose, (float) (x2 - direction.getUnitVec3().x() * 0.001f), 0 + faceOffset, (float) (z2 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU2, textureV, lightColor);
                this.vertex(flowingConsumer, pose, (float) (x2 - direction.getUnitVec3().x() * 0.001f), 0 + yx, (float) (z2 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU2, textureVYx, lightColor);
                this.vertex(flowingConsumer, pose, (float) (x1 - direction.getUnitVec3().x() * 0.001f), 0 + adx, (float) (z1 - direction.getUnitVec3().z() * 0.001f), red, green, blue, textureU1, textureVAdx, lightColor);
            }
        }
    }




    private static boolean isNeighborNotSameFluid(FluidState fluidState, FluidState fluidState2) {
        return !fluidState2.getType().isSame(fluidState.getType());
    }

    private static boolean isFaceOccludedByState(Direction direction, float f, BlockState blockState) {
        VoxelShape voxelShape = blockState.getFaceOcclusionShape(direction.getOpposite());
        if (voxelShape == Shapes.empty()) {
            return false;
        } else if (voxelShape == Shapes.block()) {
            boolean bl = f == 1.0F;
            return direction != Direction.UP || bl;
        } else {
            VoxelShape voxelShape2 = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
            return Shapes.blockOccludes(voxelShape2, voxelShape, direction);
        }
    }

    private static boolean isFaceOccludedByNeighbor(Direction direction, float f, BlockState blockState) {
        return !isFaceOccludedByState(direction, f, blockState);
    }

    private static boolean isFaceOccludedBySelf(BlockState blockState, Direction direction) {
        return isFaceOccludedByState(direction.getOpposite(), 1.0F, blockState);
    }

    public static boolean shouldRenderFace(FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        return !isFaceOccludedBySelf(blockState, direction) && isNeighborNotSameFluid(fluidState, fluidState2);
    }

    public void tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        boolean isFluidLava = fluidState.is(FluidTags.LAVA);

        TextureAtlasSprite stillTexture = isFluidLava ? this.lavaStill : this.waterStill;
        TextureAtlasSprite flowingTexture = isFluidLava ? this.lavaFlowing : this.waterFlowing;

        //main texture tint
        int fluidTint = isFluidLava ? 16777215 : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);

        float f = (fluidTint >> 16 & 0xFF) / 255.0F;
        float g = (fluidTint >> 8 & 0xFF) / 255.0F;
        float h = (fluidTint & 0xFF) / 255.0F;

        //store all of the adjacent block and fluid states
        BlockState downState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.DOWN));
        FluidState downFluid = downState.getFluidState();
        BlockState upState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.UP));
        FluidState upFluid = upState.getFluidState();
        BlockState northState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.NORTH));
        FluidState northFluid = northState.getFluidState();
        BlockState southState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.SOUTH));
        FluidState southFluid = southState.getFluidState();
        BlockState westState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.WEST));
        FluidState westFluid = westState.getFluidState();
        BlockState eastState = blockAndTintGetter.getBlockState(blockPos.relative(Direction.EAST));
        FluidState eastFluid = eastState.getFluidState();

        boolean isNotSameFluidAbove = isNeighborNotSameFluid(fluidState, upFluid);
        boolean shouldRenderDownFace = shouldRenderFace(fluidState, blockState, Direction.DOWN, downFluid) && isFaceOccludedByNeighbor(Direction.DOWN, 0.8888889F, downState);
        boolean shouldRenderNorthFace = shouldRenderFace(fluidState, blockState, Direction.NORTH, northFluid);
        boolean shouldRenderSouthFace = shouldRenderFace(fluidState, blockState, Direction.SOUTH, southFluid);
        boolean shouldRenderWestFace = shouldRenderFace(fluidState, blockState, Direction.WEST, westFluid);
        boolean shouldRenderEastFace = shouldRenderFace(fluidState, blockState, Direction.EAST, eastFluid);

        if (!(
                isNotSameFluidAbove
                || shouldRenderDownFace
                || shouldRenderEastFace
                || shouldRenderWestFace
                || shouldRenderNorthFace
                || shouldRenderSouthFace
        )) return;

        //get shades (these are almost factors? for like. light level? maybe? idk???)
        //they come from the world, so
        float downShade = blockAndTintGetter.getShade(Direction.DOWN, true);
        float upShade = blockAndTintGetter.getShade(Direction.UP, true);
        float northShade = blockAndTintGetter.getShade(Direction.NORTH, true);
        float westShade = blockAndTintGetter.getShade(Direction.WEST, true);

        Fluid fluid = fluidState.getType();

        float fluidHeight = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);

        float northEastHeight;
        float northWestHeight;
        float southEastHeight;
        float southWestHeight;

        //get and store all of the water levels for different directions,
        // defaulting for 1 if the fluid height is more than one
        if (fluidHeight >= 1.0F) {
            northEastHeight = 1.0F;
            northWestHeight = 1.0F;
            southEastHeight = 1.0F;
            southWestHeight = 1.0F;
        } else {
            float northHeight = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), northState, northFluid);
            float southHeight = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), southState, southFluid);
            float westHeight = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), eastState, eastFluid);
            float eastHeight = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), westState, westFluid);
            northEastHeight = this.calculateAverageHeight(
                    blockAndTintGetter,
                    fluid,
                    fluidHeight,
                    northHeight,
                    westHeight,
                    blockPos.relative(Direction.NORTH).relative(Direction.EAST)
            );
            northWestHeight = this.calculateAverageHeight(
                    blockAndTintGetter,
                    fluid,
                    fluidHeight,
                    northHeight,
                    eastHeight,
                    blockPos.relative(Direction.NORTH).relative(Direction.WEST)
            );
            southEastHeight = this.calculateAverageHeight(
                    blockAndTintGetter,
                    fluid,
                    fluidHeight,
                    southHeight,
                    westHeight,
                    blockPos.relative(Direction.SOUTH).relative(Direction.EAST)
            );
            southWestHeight = this.calculateAverageHeight(
                    blockAndTintGetter,
                    fluid,
                    fluidHeight,
                    southHeight,
                    eastHeight,
                    blockPos.relative(Direction.SOUTH).relative(Direction.WEST)
            );
        }

        //IS THIS A BLOCK'S SUBCHUNK POSITION?????
        float chunkX = blockPos.getX() & 15;
        float chunkY = blockPos.getY() & 15;
        float chunkZ = blockPos.getZ() & 15;

        //this was left in for some reason
//        float v = 0.001F;

        //face offset to prevent z fighting
        float faceOffset = shouldRenderDownFace ? 0.001F : 0.0F;

        //add up face vertices
        if (isNotSameFluidAbove && isFaceOccludedByNeighbor(Direction.UP, Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight)), upState)) {
            northWestHeight -= 0.001F;
            southWestHeight -= 0.001F;
            southEastHeight -= 0.001F;
            northEastHeight -= 0.001F;
            Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
            float x;
            float z;
            float ab;
            float ad;
            float y;
            float aa;
            float ac;
            float ae;
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                x = stillTexture.getU(0.0F);
                y = stillTexture.getV(0.0F);
                z = x;
                aa = stillTexture.getV(1.0F);
                ab = stillTexture.getU(1.0F);
                ac = aa;
                ad = ab;
                ae = y;
            } else {
                float af = (float) Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
                float ag = Mth.sin(af) * 0.25F;
                float ah = Mth.cos(af) * 0.25F;
                float ai = 0.5F;
                x = flowingTexture.getU(0.5F + (-ah - ag));
                y = flowingTexture.getV(0.5F + (-ah + ag));
                z = flowingTexture.getU(0.5F + (-ah + ag));
                aa = flowingTexture.getV(0.5F + (ah + ag));
                ab = flowingTexture.getU(0.5F + (ah + ag));
                ac = flowingTexture.getV(0.5F + (ah - ag));
                ad = flowingTexture.getU(0.5F + (ah - ag));
                ae = flowingTexture.getV(0.5F + (-ah - ag));
            }

            int aj = this.getLightColor(blockAndTintGetter, blockPos);
            float ag = upShade * f;
            float ah = upShade * g;
            float ai = upShade * h;
            this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + northWestHeight, chunkZ + 0.0F, ag, ah, ai, x, y, aj);
            this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + southWestHeight, chunkZ + 1.0F, ag, ah, ai, z, aa, aj);
            this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + southEastHeight, chunkZ + 1.0F, ag, ah, ai, ab, ac, aj);
            this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + northEastHeight, chunkZ + 0.0F, ag, ah, ai, ad, ae, aj);
            if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
                this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + northWestHeight, chunkZ + 0.0F, ag, ah, ai, x, y, aj);
                this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + northEastHeight, chunkZ + 0.0F, ag, ah, ai, ad, ae, aj);
                this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + southEastHeight, chunkZ + 1.0F, ag, ah, ai, ab, ac, aj);
                this.vertex(vertexConsumer, chunkX + 0.0F, chunkY + southWestHeight, chunkZ + 1.0F, ag, ah, ai, z, aa, aj);
            }
        }

        //add down face vertices
        if (shouldRenderDownFace) {
            float xx = stillTexture.getU0();
            float zx = stillTexture.getU1();
            float abx = stillTexture.getV0();
            float adx = stillTexture.getV1();
            int ak = this.getLightColor(blockAndTintGetter, blockPos.below());
            float aax = downShade * f;
            float acx = downShade * g;
            float aex = downShade * h;
            this.vertex(vertexConsumer, chunkX, chunkY + faceOffset, chunkZ + 1.0F, aax, acx, aex, xx, adx, ak);
            this.vertex(vertexConsumer, chunkX, chunkY + faceOffset, chunkZ, aax, acx, aex, xx, abx, ak);
            this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + faceOffset, chunkZ, aax, acx, aex, zx, abx, ak);
            this.vertex(vertexConsumer, chunkX + 1.0F, chunkY + faceOffset, chunkZ + 1.0F, aax, acx, aex, zx, adx, ak);
        }

        int lightColor = this.getLightColor(blockAndTintGetter, blockPos);

        //iterate over the different horizontal directions and add faces for each of them
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            float adx;
            float yx;
            float x1;
            float z1;
            float x2;
            float z2;
            boolean renderCurrentDirectionFace;

            //this sets the location of the vertices for each direction
            switch (direction) {
                case NORTH:
                    adx = northWestHeight;
                    yx = northEastHeight;
                    x1 = chunkX;
                    x2 = chunkX + 1.0F;
                    z1 = chunkZ + 0.001F;
                    z2 = chunkZ + 0.001F;
                    renderCurrentDirectionFace = shouldRenderNorthFace;
                    break;
                case SOUTH:
                    adx = southEastHeight;
                    yx = southWestHeight;
                    x1 = chunkX + 1.0F;
                    x2 = chunkX;
                    z1 = chunkZ + 1.0F - 0.001F;
                    z2 = chunkZ + 1.0F - 0.001F;
                    renderCurrentDirectionFace = shouldRenderSouthFace;
                    break;
                case WEST:
                    adx = southWestHeight;
                    yx = northWestHeight;
                    x1 = chunkX + 0.001F;
                    x2 = chunkX + 0.001F;
                    z1 = chunkZ + 1.0F;
                    z2 = chunkZ;
                    renderCurrentDirectionFace = shouldRenderWestFace;
                    break;
                default:
                    adx = northEastHeight;
                    yx = southEastHeight;
                    x1 = chunkX + 1.0F - 0.001F;
                    x2 = chunkX + 1.0F - 0.001F;
                    z1 = chunkZ;
                    z2 = chunkZ + 1.0F;
                    renderCurrentDirectionFace = shouldRenderEastFace;
            }

            //this bit here acutally handles placing the vertices on the faces
            if (renderCurrentDirectionFace && isFaceOccludedByNeighbor(direction, Math.max(adx, yx), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) {

                BlockPos blockPos2 = blockPos.relative(direction);
                TextureAtlasSprite currentTexture = flowingTexture;
                if (!isFluidLava) {
                    Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
                    if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
                        currentTexture = this.waterOverlay;
                    }
                }

                //UV stuff
                float textureU1 = currentTexture.getU(0.0F);
                float textureU2 = currentTexture.getU(0.5F);
                float textureVAdx = currentTexture.getV((1.0F - adx) * 0.5F);
                float textureVYx = currentTexture.getV((1.0F - yx) * 0.5F);
                float textureV = currentTexture.getV(0.5F);

                float shade = direction.getAxis() == Direction.Axis.Z ? northShade : westShade;

                //tint
                float red = upShade * shade * f;
                float green = upShade * shade * g;
                float blue = upShade * shade * h;

                //add the vertices
                this.vertex(vertexConsumer, x1, chunkY + adx, z1, red, green, blue, textureU1, textureVAdx, lightColor);
                this.vertex(vertexConsumer, x2, chunkY + yx, z2, red, green, blue, textureU2, textureVYx, lightColor);
                this.vertex(vertexConsumer, x2, chunkY + faceOffset, z2, red, green, blue, textureU2, textureV, lightColor);
                this.vertex(vertexConsumer, x1, chunkY + faceOffset, z1, red, green, blue, textureU1, textureV, lightColor);

                //add backface vertices
                if (currentTexture != this.waterOverlay) {
                    this.vertex(vertexConsumer, x1, chunkY + faceOffset, z1, red, green, blue, textureU1, textureV, lightColor);
                    this.vertex(vertexConsumer, x2, chunkY + faceOffset, z2, red, green, blue, textureU2, textureV, lightColor);
                    this.vertex(vertexConsumer, x2, chunkY + yx, z2, red, green, blue, textureU2, textureVYx, lightColor);
                    this.vertex(vertexConsumer, x1, chunkY + adx, z1, red, green, blue, textureU1, textureVAdx, lightColor);
                }
            }
        }
    }

    private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float g, float h, BlockPos blockPos) {

        if (h >= 1.0F || g >= 1.0F) {
            return 1.0F;
        }

        float[] averageHolder = new float[2];
        if (h > 0.0F || g > 0.0F) {

            float i = this.getHeight(blockAndTintGetter, fluid, blockPos);
            if (i >= 1.0F) {
                return 1.0F;
            }

            this.addWeightedHeight(averageHolder, i);

        }

        this.addWeightedHeight(averageHolder, f);
        this.addWeightedHeight(averageHolder, h);
        this.addWeightedHeight(averageHolder, g);


        return averageHolder[0] / averageHolder[1];

    }

    private void addWeightedHeight(float[] average, float fluidHeight) {
        if (fluidHeight >= 0.8F) {
            average[0] += fluidHeight * 10.0F;
            average[1] += 10.0F;
        } else if (fluidHeight >= 0.0F) {
            average[0] += fluidHeight;
            average[1]++;
        }
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
        BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
        return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeight(
            BlockAndTintGetter blockAndTintGetter,
            Fluid fluid,
            BlockPos blockPos,
            BlockState blockState,
            FluidState fluidState
    ) {
        if (fluid.isSame(fluidState.getType())) {
            BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.above());
            return fluid.isSame(blockState2.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    private float getUnsmoothedHeight(Fluid fluid, BlockState blockState, FluidState fluidState){
        if (fluid.isSame(fluidState.getType())) {
            return fluidState.getOwnHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    private void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float red, float green, float blue, float u, float v, int l) {
        vertexConsumer.addVertex(f, g, h).setColor(red, green, blue, 1.0F).setUv(u, v).setLight(l).setNormal(0.0F, 1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY);
    }


    private void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, float h, float red, float green, float blue, float u, float v, int l) {

        vertexConsumer.addVertex(pose, f, g, h).setColor(red, green, blue, 1.0F).setUv(u, v).setLight(l).setNormal(0.0F, 1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY);
    }

    private int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int i = LevelRenderer.getLightColor(blockAndTintGetter, blockPos);
        int j = LevelRenderer.getLightColor(blockAndTintGetter, blockPos.above());
        int k = i & 0xFF;
        int l = j & 0xFF;
        int m = i >> 16 & 0xFF;
        int n = j >> 16 & 0xFF;
        return (Math.max(k, l)) | (Math.max(m, n)) << 16;
    }

}
