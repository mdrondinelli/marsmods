package me.mar.torches;

import java.util.Set;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

public class TorchEvents {

    private static final Set<Identifier> REMOVED_RECIPES = Set.of(
            Identifier.withDefaultNamespace("torch"));

    @SubscribeEvent
    public void removeVanillaTorchRecipe(ModifyRecipeJsonsEvent event) {
        REMOVED_RECIPES.forEach(event.getRecipeJsons()::remove);
    }
}
