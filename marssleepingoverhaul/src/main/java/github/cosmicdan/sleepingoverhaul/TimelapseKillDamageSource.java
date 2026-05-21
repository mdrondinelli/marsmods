package github.cosmicdan.sleepingoverhaul;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class TimelapseKillDamageSource extends DamageSource {
    public static final String MSG_ID = "sleepingoverhaul2.timelapseKill";

    public TimelapseKillDamageSource() {
        super(new Holder.Direct<>(new DamageType(MSG_ID, DamageScaling.NEVER, 0.0f), DataComponentMap.EMPTY));
    }
}
