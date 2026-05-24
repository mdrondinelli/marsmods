package me.mar.foodspoilage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DryingRecipesLoader extends SimplePreparableReloadListener<DryingRecipesData> {
    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "drying_recipes";

    public static final DryingRecipesLoader INSTANCE = new DryingRecipesLoader();

    private DryingRecipesData data = DryingRecipesData.EMPTY;

    private DryingRecipesLoader() {}

    @Override
    protected DryingRecipesData prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, Integer> packOrder = packOrder(manager);
        Map<Identifier, List<Resource>> resourceStacks = manager.listResourceStacks(
                DIRECTORY,
                id -> id.getPath().endsWith(".json"));
        if (resourceStacks.isEmpty()) {
            return DryingRecipesData.EMPTY;
        }

        List<LoadedData> loaded = new ArrayList<>();
        resourceStacks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Identifier id = entry.getKey();
                    for (Resource resource : entry.getValue()) {
                        loaded.add(parse(id, resource, packOrder.getOrDefault(resource.sourcePackId(), Integer.MAX_VALUE)));
                    }
                });

        List<DryingRecipe> recipes = new ArrayList<>();
        loaded.stream()
                .sorted(Comparator.comparing(LoadedData::packOrder).reversed().thenComparing(LoadedData::id))
                .forEach(d -> recipes.addAll(d.data().recipes()));
        return new DryingRecipesData(List.copyOf(recipes));
    }

    private static Map<String, Integer> packOrder(ResourceManager manager) {
        Map<String, Integer> order = new HashMap<>();
        List<String> packIds = manager.listPacks().map(pack -> pack.packId()).toList();
        for (int i = 0; i < packIds.size(); i++) {
            order.put(packIds.get(i), i);
        }
        return order;
    }

    private static LoadedData parse(Identifier id, Resource resource, int packOrder) {
        try (var reader = resource.openAsReader()) {
            var json = GSON.fromJson(reader, JsonObject.class);
            DryingRecipesData data = DryingRecipesData.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .getOrThrow(msg -> new IllegalStateException("Failed to parse " + id + " from " + resource.sourcePackId() + ": " + msg));
            return new LoadedData(id, packOrder, data);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + id + " from " + resource.sourcePackId(), e);
        }
    }

    @Override
    protected void apply(DryingRecipesData prepared, ResourceManager manager, ProfilerFiller profiler) {
        this.data = prepared;
        MarsFoodSpoilage.LOGGER.info("Loaded {} drying recipe(s)", data.recipes().size());
    }

    @Nullable
    public DryingRecipe recipeFor(ItemStack stack) {
        for (DryingRecipe recipe : data.recipes()) {
            if (stack.is(recipe.input())) return recipe;
        }
        return null;
    }

    private record LoadedData(Identifier id, int packOrder, DryingRecipesData data) {}
}
