package me.mar.flinttool;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FlintToolItem extends AxeItem {
    public static final TagKey<Block> INCORRECT_FOR_FLINT_TOOL = BlockTags.create(Identifier.fromNamespaceAndPath(MarsFlintTool.MODID, "incorrect_for_flint_tool"));
    public static final TagKey<Item> FLINT_TOOL_REPAIR_MATERIALS = ItemTags.create(Identifier.fromNamespaceAndPath(MarsFlintTool.MODID, "flint_tool_repair_materials"));
    private static final float MINING_SPEED = 0.5f;
    private static final ToolMaterial FLINT_TOOL_MATERIAL = new ToolMaterial(INCORRECT_FOR_FLINT_TOOL, 8, MINING_SPEED, 0.0f, 5, FLINT_TOOL_REPAIR_MATERIALS);

    public FlintToolItem(Item.Properties properties) {
        super(FLINT_TOOL_MATERIAL, 6.0f, -3.2f, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(INCORRECT_FOR_FLINT_TOOL)) {
            return MINING_SPEED;
        }

        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(INCORRECT_FOR_FLINT_TOOL)) {
            return false;
        }

        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return true;
        }

        return super.isCorrectToolForDrops(stack, state);
    }
}
