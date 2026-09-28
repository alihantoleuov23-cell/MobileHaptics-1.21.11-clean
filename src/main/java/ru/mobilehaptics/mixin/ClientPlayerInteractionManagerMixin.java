package ru.mobilehaptics.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mobilehaptics.HapticManager;

@Mixin(MultiPlayerGameMode.class)
public final class ClientPlayerInteractionManagerMixin {

    @Unique
    private boolean mobileHaptics$holdingBlockItem;

    @Inject(
            method = "destroyBlock",
            at = @At("RETURN")
    )
    private void mobileHaptics$onBreak(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue()) {
            HapticManager.breakBlock();
        }
    }

    @Inject(
            method = "useItemOn",
            at = @At("HEAD")
    )
    private void mobileHaptics$rememberBlockItem(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        mobileHaptics$holdingBlockItem =
                player.getItemInHand(hand).getItem()
                        instanceof BlockItem;
    }

    @Inject(
            method = "useItemOn",
            at = @At("RETURN")
    )
    private void mobileHaptics$onPlace(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (mobileHaptics$holdingBlockItem
                && cir.getReturnValue().consumesAction()) {

            HapticManager.placeBlock();
        }

        mobileHaptics$holdingBlockItem = false;
    }
}