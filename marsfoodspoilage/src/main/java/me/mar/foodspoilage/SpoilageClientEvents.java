package me.mar.foodspoilage;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

@EventBusSubscriber(modid = MarsFoodSpoilage.MODID, value = Dist.CLIENT)
public class SpoilageClientEvents {
    @SubscribeEvent
    public static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MarsFoodSpoilage.MODID, "freshness_state"),
                FreshnessStateProperty.TYPE);
    }
}
