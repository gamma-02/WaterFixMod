package org.gamma02.waterfixmod.mixin.client;

import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.nbt.CompoundTag;
import org.gamma02.waterfixmod.client.HasRendererNBT;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FallingBlockRenderState.class)
public class FallingBlockRenderStateMixin implements HasRendererNBT {
    @Unique
    @Nullable
    public CompoundTag blockEntityRenderNBT = null;

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
}
