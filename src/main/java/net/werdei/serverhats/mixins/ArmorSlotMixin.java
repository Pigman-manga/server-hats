package net.werdei.serverhats.mixins;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ArmorSlot;
import net.werdei.serverhats.ServerHats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorSlot.class)
public class ArmorSlotMixin
{
    @Shadow
    @Final
    private EquipmentSlot equipmentSlot;

    @Inject(method = "canInsert", at = @At("RETURN"), cancellable = true)
    private void allowItemEquipping(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if (equipmentSlot != EquipmentSlot.HEAD || !ServerHats.isItemAllowed(stack)) return;
        cir.setReturnValue(true);
    }
}
