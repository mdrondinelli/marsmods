package me.mar.foodspoilage.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.mar.foodspoilage.FreshnessDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
abstract class SpoilageClientGuiMixin {
    @WrapOperation(
            method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getHighlightTip(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;")
    )
    private Component addFreshnessToHotbarHighlight(ItemStack stack, Component displayName, Operation<Component> original) {
        Component highlightTip = original.call(stack, displayName);
        Minecraft minecraft = Minecraft.getInstance();
        long gameTime = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        return FreshnessDisplay.nameWithFreshness(stack, highlightTip, gameTime);
    }
}
