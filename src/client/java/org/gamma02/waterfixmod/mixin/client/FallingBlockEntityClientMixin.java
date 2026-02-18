package org.gamma02.waterfixmod.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import org.gamma02.waterfixmod.client.FallingBlockSubmitter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityClientMixin extends Entity {

    public FallingBlockEntityClientMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //make sure that we remove the falling block entities
    @Override
    public void onClientRemoval() {
        super.onClientRemoval();

        FallingBlockSubmitter.ENTITIES_TO_TRY_REMOVE.add(this.getUUID());
    }
}
