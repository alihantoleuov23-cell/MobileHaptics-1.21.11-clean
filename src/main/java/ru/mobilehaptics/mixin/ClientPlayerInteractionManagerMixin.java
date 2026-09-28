package ru.mobilehaptics.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mobilehaptics.HapticManager;

@Mixin(ClientPlayerInteractionManager.class)
public final class ClientPlayerInteractionManagerMixin {

    @Inject(
            method = "breakBlock",
            at = @At("RETURN")
    )
    private void mobileHaptics$breakBlock(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValueZ()) {
            HapticManager.breakBlock();
        }
    }

    @Inject(
            method = "interactBlock",
            at = @At("RETURN")
    )
    private void mobileHaptics$interactBlock(
            PlayerEntity player,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ActionResult result = cir.getReturnValue();

        if (result != null && result.isAccepted()) {
            HapticManager.placeBlock();
        }
    }
}