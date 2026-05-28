package me.mar.bellows;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class KilnBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, StackedContentsCompatible, RecipeCraftingHolder {
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC =
            Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);
    public static final int BASE_TEMPERATURE = 1100;
    public static final int MAX_BELLOWS_BOOST_TEMPERATURE = 1100;
    public static final int BELLOWS_BOOST_PER_USE = 300;
    public static final int BELLOWS_BOOST_DECAY_PER_TICK = 2;
    public static final int HOT_TEMPERATURE = 1538;
    private static final int HOT_SOUND_INTERVAL_TICKS = 20;
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int DATA_CURRENT_TEMPERATURE = 4;
    public static final int DATA_REQUIRED_TEMPERATURE = 5;
    public static final int DATA_COUNT = 6;

    private static final Component DEFAULT_NAME = Component.translatable("container.marsbellows.kiln");
    private static final int[] NO_SIDED_SLOTS = new int[0];
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_RESULT = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int litTimeRemaining;
    private int litTotalTime;
    private int cookingTimer;
    private int cookingTotalTime;
    private int requiredTemperature;
    private int bellowsBoostTemperature;
    private int hotSoundTimer;
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case DATA_LIT_TIME -> litTimeRemaining;
                case DATA_LIT_DURATION -> litTotalTime;
                case DATA_COOKING_PROGRESS -> cookingTimer;
                case DATA_COOKING_TOTAL_TIME -> cookingTotalTime;
                case DATA_CURRENT_TEMPERATURE -> currentTemperature();
                case DATA_REQUIRED_TEMPERATURE -> requiredTemperature;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case DATA_LIT_TIME -> litTimeRemaining = value;
                case DATA_LIT_DURATION -> litTotalTime = value;
                case DATA_COOKING_PROGRESS -> cookingTimer = value;
                case DATA_COOKING_TOTAL_TIME -> cookingTotalTime = value;
                case DATA_REQUIRED_TEMPERATURE -> requiredTemperature = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KILN.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, KilnBlockEntity kiln) {
        boolean changed = false;
        boolean wasLit = kiln.isLit();
        if (kiln.litTimeRemaining > 0) {
            kiln.litTimeRemaining--;
        }
        boolean wasHot = kiln.isHot();
        if (kiln.bellowsBoostTemperature > 0) {
            kiln.bellowsBoostTemperature = Math.max(0, kiln.bellowsBoostTemperature - BELLOWS_BOOST_DECAY_PER_TICK);
        }

        ItemStack fuel = kiln.items.get(SLOT_FUEL);
        ItemStack ingredient = kiln.items.get(SLOT_INPUT);
        RecipeHolder<? extends AbstractCookingRecipe> recipe = kiln.recipeFor(level, ingredient);
        ItemStack result = recipe == null ? ItemStack.EMPTY : recipe.value().assemble(new SingleRecipeInput(ingredient));
        kiln.requiredTemperature = recipe == null
                ? 0
                : MeltingPointReloadListener.INSTANCE.meltingPoint(recipe, ingredient).orElse(Integer.MAX_VALUE);

        boolean canSmelt = recipe != null
                && !result.isEmpty()
                && canBurn(kiln.items, kiln.getMaxStackSize(), result);
        if (canSmelt && kiln.cookingTotalTime <= 0) {
            kiln.cookingTotalTime = recipe.value().cookingTime();
        }

        if (kiln.isLit() || !fuel.isEmpty() && canSmelt) {
            if (!kiln.isLit() && canSmelt) {
                int burnDuration = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                kiln.litTimeRemaining = burnDuration;
                kiln.litTotalTime = burnDuration;
                if (burnDuration > 0) {
                    consumeFuel(kiln.items, fuel);
                    changed = true;
                }
            }

            if (kiln.isLit() && canSmelt) {
                if (kiln.requiredTemperature <= kiln.currentTemperature()) {
                    kiln.cookingTimer++;
                    if (kiln.cookingTimer >= kiln.cookingTotalTime) {
                        kiln.cookingTimer = 0;
                        kiln.cookingTotalTime = recipe.value().cookingTime();
                        burn(kiln.items, ingredient, result);
                        kiln.setRecipeUsed(recipe);
                        changed = true;
                    }
                }
            } else {
                kiln.cookingTimer = 0;
            }
        } else if (kiln.cookingTimer > 0) {
            kiln.cookingTimer = Mth.clamp(kiln.cookingTimer - 2, 0, kiln.cookingTotalTime);
        }

        boolean isLit = kiln.isLit();
        if (!isLit && kiln.bellowsBoostTemperature > 0) {
            kiln.bellowsBoostTemperature = 0;
        }
        if (wasLit != isLit) {
            changed = true;
            level.setBlock(pos, state.setValue(KilnBlock.LIT, isLit), 3);
        }
        boolean isHot = kiln.isHot();
        if (wasHot != isHot) {
            changed = true;
            level.setBlock(pos, level.getBlockState(pos).setValue(KilnBlock.BOOSTED, isHot), 3);
        }
        if (isHot) {
            kiln.hotSoundTimer++;
            if (kiln.hotSoundTimer >= HOT_SOUND_INTERVAL_TICKS) {
                kiln.hotSoundTimer = 0;
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.7F, 1.2F);
            }
        } else {
            kiln.hotSoundTimer = 0;
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeFor(ServerLevel level, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return null;
        }
        return this.quickCheck.getRecipeFor(new SingleRecipeInput(ingredient), level).orElse(null);
    }

    private static void consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(SLOT_FUEL, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    private static boolean canBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack result) {
        ItemStack output = items.get(SLOT_RESULT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        int resultCount = output.getCount() + result.getCount();
        int maxResultCount = Math.min(maxStackSize, result.getMaxStackSize());
        return resultCount <= maxResultCount;
    }

    private static void burn(NonNullList<ItemStack> items, ItemStack input, ItemStack result) {
        ItemStack output = items.get(SLOT_RESULT);
        if (output.isEmpty()) {
            items.set(SLOT_RESULT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        if (input.is(Items.WET_SPONGE) && !items.get(SLOT_FUEL).isEmpty() && items.get(SLOT_FUEL).is(Items.BUCKET)) {
            items.set(SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
        }

        input.shrink(1);
    }

    private boolean isLit() {
        return this.litTimeRemaining > 0;
    }

    private int currentTemperature() {
        return BASE_TEMPERATURE + this.bellowsBoostTemperature;
    }

    private boolean isHot() {
        return this.currentTemperature() >= HOT_TEMPERATURE;
    }

    public boolean canApplyBellowsBoost() {
        return this.isLit() && this.bellowsBoostTemperature < MAX_BELLOWS_BOOST_TEMPERATURE;
    }

    public void applyBellowsBoost() {
        this.bellowsBoostTemperature = Math.min(
                MAX_BELLOWS_BOOST_TEMPERATURE,
                this.bellowsBoostTemperature + BELLOWS_BOOST_PER_USE);
        this.setChanged();
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed) {
        if (recipeUsed != null) {
            this.recipesUsed.addTo(recipeUsed.id(), 1);
        }
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> itemStacks) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> recipesToAward = this.getRecipesToAwardAndPopExperience(player.level(), player.position());
        player.awardRecipes(recipesToAward);
        for (RecipeHolder<?> recipe : recipesToAward) {
            player.triggerRecipeCrafted(recipe, this.items);
        }
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 position) {
        List<RecipeHolder<?>> recipesToAward = new ArrayList<>();
        for (var entry : this.recipesUsed.reference2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                recipesToAward.add((RecipeHolder<?>) recipe);
                createExperience(level, position, entry.getIntValue(), ((AbstractCookingRecipe) recipe.value()).experience());
            });
        }
        return recipesToAward;
    }

    private static void createExperience(ServerLevel level, Vec3 position, int amount, float value) {
        int xpReward = Mth.floor(amount * value);
        float xpFraction = Mth.frac(amount * value);
        if (xpFraction != 0.0F && level.getRandom().nextFloat() < xpFraction) {
            xpReward++;
        }
        ExperienceOrb.award(level, position, xpReward);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.cookingTimer = input.getIntOr("cooking_time_spent", 0);
        this.cookingTotalTime = input.getIntOr("cooking_total_time", 0);
        this.litTimeRemaining = input.getIntOr("lit_time_remaining", 0);
        this.litTotalTime = input.getIntOr("lit_total_time", 0);
        this.requiredTemperature = input.getIntOr("required_temperature", 0);
        this.bellowsBoostTemperature = input.getIntOr("bellows_boost_temperature", 0);
        this.recipesUsed.clear();
        this.recipesUsed.putAll(input.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("cooking_time_spent", this.cookingTimer);
        output.putInt("cooking_total_time", this.cookingTotalTime);
        output.putInt("lit_time_remaining", this.litTimeRemaining);
        output.putInt("lit_total_time", this.litTotalTime);
        output.putInt("required_temperature", this.requiredTemperature);
        output.putInt("bellows_boost_temperature", this.bellowsBoostTemperature);
        output.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new KilnMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        ItemStack oldStack = this.items.get(slot);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, stack);
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        if (slot == SLOT_INPUT && !same && this.level instanceof ServerLevel serverLevel && !insideTransaction) {
            RecipeHolder<? extends AbstractCookingRecipe> recipe = this.recipeFor(serverLevel, stack);
            this.cookingTotalTime = recipe == null ? 200 : recipe.value().cookingTime();
            this.cookingTimer = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_RESULT) {
            return false;
        }
        if (slot == SLOT_FUEL) {
            ItemStack fuelSlot = this.items.get(SLOT_FUEL);
            return stack.getBurnTime(RecipeType.SMELTING, this.level.fuelValues()) > 0
                    || stack.is(Items.BUCKET) && !fuelSlot.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return NO_SIDED_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        for (ItemStack stack : this.items) {
            contents.accountStack(stack);
        }
    }

}
