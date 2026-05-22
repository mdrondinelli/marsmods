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

public class SpoilageRulesLoader extends SimplePreparableReloadListener<SpoilageRulesData> {
    private static final Gson GSON = new Gson();
    private static final String RULES_DIRECTORY = "spoilage_rules";

    public static final SpoilageRulesLoader INSTANCE = new SpoilageRulesLoader();

    private SpoilageRulesData rules = SpoilageRulesData.EMPTY;

    private SpoilageRulesLoader() {}

    @Override
    protected SpoilageRulesData prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, Integer> packOrder = packOrder(manager);
        Map<Identifier, List<Resource>> resourceStacks = manager.listResourceStacks(
                RULES_DIRECTORY,
                id -> id.getPath().endsWith(".json"));
        if (resourceStacks.isEmpty()) {
            MarsFoodSpoilage.LOGGER.warn("No spoilage rule files found under data/*/{}/", RULES_DIRECTORY);
            return SpoilageRulesData.EMPTY;
        }

        List<LoadedRules> loaded = new ArrayList<>();
        resourceStacks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Identifier id = entry.getKey();
                    for (Resource resource : entry.getValue()) {
                        loaded.add(parseRules(id, resource, packOrder.getOrDefault(resource.sourcePackId(), Integer.MAX_VALUE)));
                    }
                });

        List<SpoilageRule> spoilage = new ArrayList<>();
        List<SpoiledEffectsRule> spoiledEffects = new ArrayList<>();
        loaded.stream()
                .sorted(Comparator.comparing(LoadedRules::packOrder).thenComparing(LoadedRules::id))
                .forEach(loadedRules -> {
                    spoilage.addAll(loadedRules.rules().spoilage());
                    spoiledEffects.addAll(loadedRules.rules().spoiledEffects());
                });
        return new SpoilageRulesData(List.copyOf(spoilage), List.copyOf(spoiledEffects));
    }

    private static Map<String, Integer> packOrder(ResourceManager manager) {
        Map<String, Integer> packOrder = new HashMap<>();
        List<String> packIds = manager.listPacks().map(pack -> pack.packId()).toList();
        for (int i = 0; i < packIds.size(); i++) {
            packOrder.put(packIds.get(i), i);
        }
        return packOrder;
    }

    private static LoadedRules parseRules(Identifier id, Resource resource, int packOrder) {
        try (var reader = resource.openAsReader()) {
            var json = GSON.fromJson(reader, JsonObject.class);
            SpoilageRulesData rules = SpoilageRulesData.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .getOrThrow(msg -> new IllegalStateException("Failed to parse " + id + " from " + resource.sourcePackId() + ": " + msg));
            return new LoadedRules(id, packOrder, rules);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + id + " from " + resource.sourcePackId(), e);
        }
    }

    @Override
    protected void apply(SpoilageRulesData rules, ResourceManager manager, ProfilerFiller profiler) {
        this.rules = rules;
        MarsFoodSpoilage.LOGGER.info("Loaded {} spoilage rule(s) and {} spoiled effect rule(s)",
                rules.spoilage().size(), rules.spoiledEffects().size());
    }

    @Nullable
    public SpoilageProfile profileFor(ItemStack stack) {
        List<SpoilageRule> spoilage = rules.spoilage();
        for (int i = spoilage.size() - 1; i >= 0; i--) {
            SpoilageRule rule = spoilage.get(i);
            if (stack.is(rule.tag())) {
                return rule.profile().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public SpoiledFoodEffects spoiledEffectsFor(ItemStack stack) {
        List<SpoiledEffectsRule> spoiledEffects = rules.spoiledEffects();
        for (int i = spoiledEffects.size() - 1; i >= 0; i--) {
            SpoiledEffectsRule rule = spoiledEffects.get(i);
            if (stack.is(rule.tag())) {
                return rule.effects();
            }
        }
        return null;
    }

    private record LoadedRules(Identifier id, int packOrder, SpoilageRulesData rules) {
    }
}
