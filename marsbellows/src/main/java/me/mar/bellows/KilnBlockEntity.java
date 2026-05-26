package me.mar.bellows;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class KilnBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, StackedContentsCompatible {
    public static final int BASE_TEMPERATURE = 1100;
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
        if (wasLit != isLit) {
            changed = true;
            level.setBlock(pos, state.setValue(KilnBlock.LIT, isLit), 3);
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
        return BASE_TEMPERATURE;
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
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("cooking_time_spent", this.cookingTimer);
        output.putInt("cooking_total_time", this.cookingTotalTime);
        output.putInt("lit_time_remaining", this.litTimeRemaining);
        output.putInt("lit_total_time", this.litTotalTime);
        output.putInt("required_temperature", this.requiredTemperature);
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
