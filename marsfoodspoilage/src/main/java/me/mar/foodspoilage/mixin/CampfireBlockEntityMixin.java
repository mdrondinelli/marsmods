package me.mar.foodspoilage.mixin;

import me.mar.foodspoilage.MarsSpoilageConfig;
import me.mar.foodspoilage.SpoilageService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {

    @Shadow public abstract NonNullList<ItemStack> getItems();

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void marsspoilage$onCookTickHead(
            ServerLevel level, BlockPos pos, BlockState state,
            CampfireBlockEntity be, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> check,
            CallbackInfo ci) {
        float rate = MarsSpoilageConfig.CAMPFIRE_SPOILAGE_RATE.get().floatValue();
        for (ItemStack stack : be.getItems()) {
            SpoilageService.applyRate(level, stack, rate);
        }
    }

    @Inject(method = "cooldownTick", at = @At("HEAD"))
    private static void marsspoilage$onCooldownTickHead(
            Level level, BlockPos pos, BlockState state,
            CampfireBlockEntity be,
            CallbackInfo ci) {
        if (!(level instanceof ServerLevel sl)) {
            return;
        }
        for (ItemStack stack : be.getItems()) {
            SpoilageService.applyRate(sl, stack, 1.0f);
        }
    }

}
