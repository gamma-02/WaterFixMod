package org.gamma02.waterfixmod.mixin.client;

import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.nbt.CompoundTag;
import org.gamma02.waterfixmod.client.HasExtraBlockData;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(FallingBlockRenderState.class)
public class FallingBlockRenderStateMixin implements HasExtraBlockData {

    //this is just to add these two fields to the FallingBlockRenderState
    @Unique
    @Nullable
    public CompoundTag blockEntityRenderNBT = null;

    @Unique
    public UUID entityId = null;

    @Override
    @Unique
    public @Nullable CompoundTag waterFixMod$getNBT() {
        return blockEntityRenderNBT;
    }

    @Override
    @Unique
    public void waterFixMod$setNBT(CompoundTag nbt) {
        blockEntityRenderNBT = nbt;
    }

    @Override
    public void waterFixMod$setUUID(UUID id) {
        entityId = id;
    }

    @Override
    public @NonNull UUID waterFixMod$getUUID() {
        return entityId;
    }
}
