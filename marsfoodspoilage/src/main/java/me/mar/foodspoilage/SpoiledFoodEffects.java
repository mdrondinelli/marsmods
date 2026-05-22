package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpoiledFoodEffects(
        double poisonChance,
        double nauseaChance,
        double weaknessChance,
        double slownessChance,
        double hungerChance) {
    public static final Codec<SpoiledFoodEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("poison_chance", 0.0).forGetter(SpoiledFoodEffects::poisonChance),
            Codec.DOUBLE.optionalFieldOf("nausea_chance", 0.0).forGetter(SpoiledFoodEffects::nauseaChance),
            Codec.DOUBLE.optionalFieldOf("weakness_chance", 0.0).forGetter(SpoiledFoodEffects::weaknessChance),
            Codec.DOUBLE.optionalFieldOf("slowness_chance", 0.0).forGetter(SpoiledFoodEffects::slownessChance),
            Codec.DOUBLE.optionalFieldOf("hunger_chance", 0.0).forGetter(SpoiledFoodEffects::hungerChance))
            .apply(instance, SpoiledFoodEffects::new));

    public SpoiledFoodEffects {
        poisonChance = clampChance(poisonChance);
        nauseaChance = clampChance(nauseaChance);
        weaknessChance = clampChance(weaknessChance);
        slownessChance = clampChance(slownessChance);
        hungerChance = clampChance(hungerChance);
    }

    private static double clampChance(double chance) {
        if (Double.isNaN(chance)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, chance));
    }
}
