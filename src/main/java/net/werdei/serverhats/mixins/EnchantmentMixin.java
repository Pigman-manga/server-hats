package net.werdei.serverhats.mixins;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.werdei.serverhats.Config;
import net.werdei.serverhats.ServerHats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin
{
    @Inject(method = "isAcceptableItem", at = @At("RETURN"), cancellable = true)
    private void allowHatEnchanting(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if (!Config.enchanting) return;
        if (ServerHats.isItemAllowed(stack))
            cir.setReturnValue(true);
    }
}
