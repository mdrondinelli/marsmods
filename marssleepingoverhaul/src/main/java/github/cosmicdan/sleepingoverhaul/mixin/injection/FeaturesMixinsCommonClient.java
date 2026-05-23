package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.mixin.proxy.ChatScreenProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class FeaturesMixinsCommonClient {}

@Mixin(LivingEntity.class)
abstract class FeaturesMixinsCommonClientLivingEntity {
    /**
     * For leaf litter sleeping: return the direction toward the adjacent leaf litter so the renderer
     * uses a fixed orientation instead of falling back to the player's arbitrary yaw.
     */
    @Inject(method = "getBedOrientation", at = @At("RETURN"), cancellable = true)
    private void leafLitterBedOrientation(CallbackInfoReturnable<Direction> cir) {
        if (cir.getReturnValue() != null) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getSleepingPos().isEmpty()) return;
        BlockPos pos = self.getSleepingPos().get();
        BlockState state = self.level().getBlockState(pos);
        if (!state.is(Blocks.LEAF_LITTER) || state.getValue(SegmentableBlock.AMOUNT) != SegmentableBlock.MAX_SEGMENT) return;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState adj = self.level().getBlockState(pos.relative(dir));
            if (adj.is(Blocks.LEAF_LITTER) && adj.getValue(SegmentableBlock.AMOUNT) == SegmentableBlock.MAX_SEGMENT) {
                cir.setReturnValue(dir);
                return;
            }
        }
    }
}

@Mixin(ChatScreen.class)
abstract class FeaturesMixinsCommonClientChatScreen extends Screen implements ChatScreenProxy {
    @Shadow
    protected EditBox input;

    protected FeaturesMixinsCommonClientChatScreen(Component title) {
        super(title);
    }

    /**
     * For inBedChatFixes; allows the chat box to lose focus
     */
    @WrapOperation(
            method = "init",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setCanLoseFocus(Z)V")
    )
    private void onInit(EditBox instance, boolean canLoseFocus, Operation<Void> original) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen) {
            input.setCanLoseFocus(true);
        } else {
            original.call(instance, canLoseFocus);
        }
    }

    /**
     * For inBedChatFixes; allows clicking the Chat Box to focus it
     */
    @Inject(
            method = "mouseClicked",
            at = @At("HEAD")
    )
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen) {
            if (input.mouseClicked(event, false)) {
                input.setFocused(true);
                this.setFocused(input);
            }
        }
    }

    @Unique
    @Override
    public EditBox so2_$getInput() {
        return input;
    }
}

@Mixin(InBedChatScreen.class)
abstract class FeaturesMixinsCommonClientInBedChatScreen extends ChatScreen {
    @Shadow
    protected abstract void sendWakeUp();

    public FeaturesMixinsCommonClientInBedChatScreen(String initial, boolean isDraft) {
        super(initial, isDraft);
    }

    /**
     * For inBedChatFixes:
     * - Only sends the message if chat box is actually focused;
     * - Enables Ctrl+Tab to switch focus to/from the chat box;
     * - Enables ENTER to actually work on buttons (if chat box is NOT focused)
     * MC 26.x: InBedChatScreen no longer overrides keyPressed, so we inject the override
     * directly (unannotated method injection) instead of @WrapMethod.
     */
    public boolean keyPressed(KeyEvent event) {
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get()) {
            int keyCode = event.key();
            int modifiers = event.modifiers();
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.sendWakeUp();
            }
            if (input.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == GLFW.GLFW_MOD_CONTROL) {
                    ComponentPath nextFocusPath = super.nextFocusPath(new FocusNavigationEvent.ArrowNavigation(ScreenDirection.UP));
                    if (nextFocusPath != null)
                        changeFocus(nextFocusPath);
                    return true;
                } else if (!this.minecraft.player.chatAbilities().canSendMessages()) {
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    this.handleChatInput(this.input.getValue(), true);
                    this.minecraft.setScreen(null);
                    this.input.setValue("");
                    this.minecraft.gui.getChat().resetChatScroll();
                    return true;
                }
            } else {
                if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == GLFW.GLFW_MOD_CONTROL) {
                    input.setFocused(true);
                    this.setFocused(input);
                    return true;
                }
            }
            return super.keyPressed(event);
        } else {
            return super.keyPressed(event);
        }
    }
}

@Mixin(Screen.class)
abstract class FeaturesMixinsCommonClientScreen {
    /**
     * For inBedChatFixes; blocks Tab/Arrow navigation if input is focused
     */
    @WrapOperation(
            method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;changeFocus(Lnet/minecraft/client/gui/ComponentPath;)V")
    )
    private void onKeyPressedChangeFocus(Screen instance, ComponentPath path, Operation<Void> original) {
        //noinspection ConstantValue
        if (SleepingOverhaul.clientConfig.inBedChatFixes.get() && (Object) this instanceof InBedChatScreen inBedChatScreen) {
            if (((ChatScreenProxy) inBedChatScreen).so2_$getInput().isFocused()) {
                return;
            }
        }
        original.call(instance, path);
    }
}
