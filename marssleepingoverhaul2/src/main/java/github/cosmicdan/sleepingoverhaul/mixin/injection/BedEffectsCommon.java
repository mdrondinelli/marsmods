package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.HungerMobEffect;
import net.minecraft.world.effect.PoisonMobEffect;
import net.minecraft.world.effect.WitherMobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

public class BedEffectsCommon {}

/**
 * For bedEffectNoPoison
 */
@Mixin(PoisonMobEffect.class)
abstract class BedEffectsCommonPoisonMobEffect {
    @WrapMethod(
            method = "applyEffectTick"
    )
    private boolean onApplyEffectTick(ServerLevel level, LivingEntity entity, int amplifier, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedEffectNoPoison.get()) {
            if (entity instanceof Player && entity.isSleeping())
                return true; // do nothing but return success
        }
        return original.call(level, entity, amplifier);
    }
}

/**
 * For bedEffectNoHunger
 */
@Mixin(HungerMobEffect.class)
abstract class BedEffectsCommonHungerMobEffect {
    @WrapMethod(
            method = "applyEffectTick"
    )
    private boolean onApplyEffectTick(ServerLevel level, LivingEntity entity, int amplifier, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedEffectNoHunger.get()) {
            if (entity instanceof Player && entity.isSleeping())
                return true; // do nothing but return success
        }
        return original.call(level, entity, amplifier);
    }
}

/**
 * For bedEffectNoWither
 */
@Mixin(WitherMobEffect.class)
abstract class BedEffectsCommonWitherMobEffect {
    @WrapMethod(
            method = "applyEffectTick"
    )
    private boolean onApplyEffectTick(ServerLevel level, LivingEntity entity, int amplifier, Operation<Boolean> original) {
        if (SleepingOverhaul.serverConfig.bedEffectNoWither.get()) {
            if (entity instanceof Player && entity.isSleeping())
                return true; // do nothing but return success
        }
        return original.call(level, entity, amplifier);
    }
}
