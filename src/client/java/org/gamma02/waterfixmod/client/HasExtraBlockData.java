package org.gamma02.waterfixmod.client;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface HasExtraBlockData {

    @Nullable
    CompoundTag waterFixMod$getNBT();
    void waterFixMod$setNBT(CompoundTag nbt);

    void waterFixMod$setUUID(UUID id);
    @NonNull UUID waterFixMod$getUUID();
}
