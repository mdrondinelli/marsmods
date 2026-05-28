package me.mar.dryingrack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;

public class DryingRackBlockEntity extends BlockEntity {
    public static final int SLOTS = 1;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    long dryingStartTick = -1L;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copyWithCount(1));
                if (level instanceof ServerLevel sl) {
                    NeoForge.EVENT_BUS.post(new DryingRackEvents.Enter(sl, worldPosition, items.get(i)));
                    DryingRecipe recipe = DryingRecipesLoader.INSTANCE.recipeFor(items.get(i));
                    dryingStartTick = recipe != null ? sl.getGameTime() : -1L;
                }
                return true;
            }
        }
        return false;
    }

    public ItemStack removeLastItem() {
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack stack = items.set(i, ItemStack.EMPTY);
                dryingStartTick = -1L;
                if (level instanceof ServerLevel sl) {
                    NeoForge.EVENT_BUS.post(new DryingRackEvents.Exit(sl, worldPosition, stack));
                }
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (level.getGameTime() % 20 != 0) return;
        if (be.dryingStartTick < 0) return;
        ItemStack current = be.items.get(0);
        if (current.isEmpty()) return;
        DryingRecipe recipe = DryingRecipesLoader.INSTANCE.recipeFor(current);
        if (recipe == null) {
            be.dryingStartTick = -1L;
            return;
        }
        if (level.getGameTime() - be.dryingStartTick >= recipe.durationTicks()) {
            be.items.set(0, new ItemStack(recipe.output()));
            NeoForge.EVENT_BUS.post(new DryingRackEvents.Enter((ServerLevel) level, pos, be.items.get(0)));
            be.dryingStartTick = -1L;
            be.markUpdated();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        dryingStartTick = input.getLongOr("drying_start_tick", -1L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putLong("drying_start_tick", dryingStartTick);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), MarsDryingRack.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ContainerHelper.saveAllItems(output, items, true);
            return output.buildResult();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel sl) {
            for (ItemStack stack : items) {
                NeoForge.EVENT_BUS.post(new DryingRackEvents.Exit(sl, worldPosition, stack));
            }
            Containers.dropContents(level, pos, items);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.STICK, 4));
        }
    }

    public void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
