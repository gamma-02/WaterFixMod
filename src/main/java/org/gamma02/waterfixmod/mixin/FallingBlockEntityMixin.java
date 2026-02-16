package org.gamma02.waterfixmod.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    @Shadow@Nullable public CompoundTag blockData;
    @Unique
    private static final EntityDataAccessor<String> BLOCK_ENTITY_DATA = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.STRING);


    public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    public void injectNewSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BLOCK_ENTITY_DATA, "");
    }

    @Override
    public void onSyncedDataUpdated(@NonNull EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (entityDataAccessor.equals(BLOCK_ENTITY_DATA)) {
            try {
                this.blockData = TagParser.parseCompoundAsArgument(new StringReader(this.getEntityData().get(BLOCK_ENTITY_DATA)));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "RETURN"))
    public void setBlockEntityData(ValueInput valueInput, CallbackInfo ci){
        if(this.blockData != null)
            this.getEntityData().set(BLOCK_ENTITY_DATA, blockData.toString());
        else
            this.getEntityData().set(BLOCK_ENTITY_DATA, "");
    }

}
