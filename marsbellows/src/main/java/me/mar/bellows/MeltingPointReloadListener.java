package me.mar.bellows;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class MeltingPointReloadListener extends SimplePreparableReloadListener<MeltingPointReloadListener.Data> {
    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "melting_points";

    public static final MeltingPointReloadListener INSTANCE = new MeltingPointReloadListener();

    private Data data = Data.EMPTY;

    private MeltingPointReloadListener() {
    }

    @Override
    protected Data prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, Integer> packOrder = packOrder(manager);
        Map<Identifier, List<Resource>> resourceStacks = manager.listResourceStacks(
                DIRECTORY,
                id -> id.getPath().endsWith(".json"));
        if (resourceStacks.isEmpty()) {
            return Data.EMPTY;
        }

        List<LoadedRule> loaded = new ArrayList<>();
        resourceStacks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Identifier id = entry.getKey();
                    for (Resource resource : entry.getValue()) {
                        loaded.addAll(parse(id, resource, packOrder.getOrDefault(resource.sourcePackId(), Integer.MAX_VALUE)));
                    }
                });

        Map<Identifier, Integer> recipeRules = new HashMap<>();
        List<IngredientRule> ingredientRules = new ArrayList<>();
        loaded.stream()
                .sorted(Comparator.comparingInt(LoadedRule::packOrder).reversed().thenComparing(LoadedRule::id))
                .forEach(loadedRule -> {
                    Rule rule = loadedRule.rule();
                    if (rule instanceof RecipeRule recipeRule) {
                        recipeRules.putIfAbsent(recipeRule.recipe(), recipeRule.meltingPoint());
                    } else if (rule instanceof IngredientRule ingredientRule) {
                        ingredientRules.add(ingredientRule);
                    }
                });
        return new Data(Map.copyOf(recipeRules), List.copyOf(ingredientRules));
    }

    private static Map<String, Integer> packOrder(ResourceManager manager) {
        Map<String, Integer> order = new HashMap<>();
        List<String> packIds = manager.listPacks().map(pack -> pack.packId()).toList();
        for (int i = 0; i < packIds.size(); i++) {
            order.put(packIds.get(i), i);
        }
        return order;
    }

    private static List<LoadedRule> parse(Identifier id, Resource resource, int packOrder) {
        try (var reader = resource.openAsReader()) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            List<LoadedRule> rules = new ArrayList<>();
            if (json.has("values")) {
                JsonArray values = json.getAsJsonArray("values");
                for (JsonElement element : values) {
                    rules.add(new LoadedRule(id, packOrder, parseRule(id, element.getAsJsonObject())));
                }
            } else {
                rules.add(new LoadedRule(id, packOrder, parseRule(id, json)));
            }
            return rules;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + id + " from " + resource.sourcePackId(), e);
        }
    }

    private static Rule parseRule(Identifier id, JsonObject json) {
        int meltingPoint = json.get("melting_point").getAsInt();
        if (json.has("recipe")) {
            return new RecipeRule(Identifier.parse(json.get("recipe").getAsString()), meltingPoint);
        }
        if (json.has("ingredient")) {
            return new IngredientRule(parseIngredient(json.get("ingredient")), meltingPoint);
        }
        throw new IllegalStateException("Melting point rule " + id + " must define recipe or ingredient");
    }

    private static MeltingPointIngredient parseIngredient(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return parseIngredientString(json.getAsString());
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("item")) {
            return new ItemIngredient(Identifier.parse(object.get("item").getAsString()));
        }
        if (object.has("tag")) {
            return new TagIngredient(Identifier.parse(object.get("tag").getAsString()));
        }
        throw new IllegalStateException("Melting point ingredient must be a string or define item/tag");
    }

    private static MeltingPointIngredient parseIngredientString(String value) {
        if (value.startsWith("#")) {
            return new TagIngredient(Identifier.parse(value.substring(1)));
        }
        return new ItemIngredient(Identifier.parse(value));
    }

    @Override
    protected void apply(Data prepared, ResourceManager manager, ProfilerFiller profiler) {
        this.data = prepared;
        MarsBellows.LOGGER.info("Loaded {} recipe melting point(s) and {} ingredient melting point rule(s)",
                prepared.recipeRules().size(), prepared.ingredientRules().size());
    }

    public OptionalInt meltingPoint(RecipeHolder<?> recipe, ItemStack input) {
        Identifier recipeId = ((RecipeHolder<? extends Recipe<?>>) recipe).id().identifier();
        Integer recipeValue = this.data.recipeRules().get(recipeId);
        if (recipeValue != null) {
            return OptionalInt.of(recipeValue);
        }
        for (IngredientRule rule : this.data.ingredientRules()) {
            if (rule.ingredient().test(input)) {
                return OptionalInt.of(rule.meltingPoint());
            }
        }
        return OptionalInt.empty();
    }

    private sealed interface Rule permits RecipeRule, IngredientRule {
    }

    private record RecipeRule(Identifier recipe, int meltingPoint) implements Rule {
    }

    private record IngredientRule(MeltingPointIngredient ingredient, int meltingPoint) implements Rule {
    }

    private sealed interface MeltingPointIngredient permits ItemIngredient, TagIngredient {
        boolean test(ItemStack stack);
    }

    private record ItemIngredient(Identifier item) implements MeltingPointIngredient {
        @Override
        public boolean test(ItemStack stack) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, this.item);
            return stack.is(holder -> holder.is(key));
        }
    }

    private record TagIngredient(Identifier tag) implements MeltingPointIngredient {
        @Override
        public boolean test(ItemStack stack) {
            return stack.is(TagKey.create(Registries.ITEM, this.tag));
        }
    }

    record Data(Map<Identifier, Integer> recipeRules, List<IngredientRule> ingredientRules) {
        static final Data EMPTY = new Data(Map.of(), List.of());
    }

    private record LoadedRule(Identifier id, int packOrder, Rule rule) {
    }
}
