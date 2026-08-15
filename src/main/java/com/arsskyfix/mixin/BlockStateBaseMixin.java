package com.arsskyfix.mixin;

import com.hollingsworth.arsnouveau.common.block.SkyWeave;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Inject(method = "isValidSpawn", at = @At("HEAD"), cancellable = true)
    private void arsskyfix$noSpawnOnSkyBlock(BlockGetter level, BlockPos pos, EntityType<?> type,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (((BlockState) (Object) this).getBlock() instanceof SkyWeave) {
            cir.setReturnValue(false);
        }
    }
}
