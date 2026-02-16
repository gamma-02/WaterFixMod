package org.gamma02.waterfixmod.client;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface HasRendererNBT {

    @Nullable
    CompoundTag waterFixMod$getNBT();
    void waterFixMod$setNBT(CompoundTag nbt);
}
