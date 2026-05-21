package me.mar.foodspoilage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class SpoilageRulesLoader extends SimplePreparableReloadListener<List<SpoilageRule>> {
    private static final Gson GSON = new Gson();
    private static final Identifier RULES_FILE = Identifier.fromNamespaceAndPath(
            MarsFoodSpoilage.MODID, "spoilage_rules.json");

    public static final SpoilageRulesLoader INSTANCE = new SpoilageRulesLoader();

    private List<SpoilageRule> rules = List.of();

    private SpoilageRulesLoader() {}

    @Override
    protected List<SpoilageRule> prepare(ResourceManager manager, ProfilerFiller profiler) {
        var resource = manager.getResource(RULES_FILE);
        if (resource.isEmpty()) {
            MarsFoodSpoilage.LOGGER.warn("spoilage_rules.json not found — no food will spoil");
            return List.of();
        }
        try (var reader = resource.get().openAsReader()) {
            var json = GSON.fromJson(reader, JsonArray.class);
            return SpoilageRule.CODEC.listOf()
                    .parse(JsonOps.INSTANCE, json)
                    .getOrThrow(msg -> new IllegalStateException("Failed to parse spoilage_rules.json: " + msg));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read spoilage_rules.json", e);
        }
    }

    @Override
    protected void apply(List<SpoilageRule> rules, ResourceManager manager, ProfilerFiller profiler) {
        this.rules = rules;
        MarsFoodSpoilage.LOGGER.info("Loaded {} spoilage rule(s)", rules.size());
    }

    @Nullable
    public SpoilageProfile profileFor(ItemStack stack) {
        for (SpoilageRule rule : rules) {
            if (stack.is(rule.tag())) {
                return rule.profile().orElse(null);
            }
        }
        return null;
    }
}
