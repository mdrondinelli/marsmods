package me.mar.torches.item;

import me.mar.torches.config.TorchConfig;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.context.UseOnContext;

public class MatchboxItem extends FlintAndSteelItem {

    public static final String NAME = "matchbox";
    public MatchboxItem(Properties properties) {
        super(properties.durability(TorchConfig.MATCHBOX_DURABILITY_MAX));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        int configured = TorchConfig.MATCHBOX_DURABILITY.get();
        return configured > 0 ? configured : TorchConfig.MATCHBOX_DURABILITY_MAX;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!TorchConfig.MATCHBOX_CREATES_FIRE.get()) {
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        ItemStack stack = (ItemStack) instance;
        if (TorchConfig.MATCHBOX_DURABILITY.get() <= 0) {
            return new ItemStackTemplate(this);
        } else if (stack.getDamageValue() + 1 > stack.getMaxDamage()) {
            return null;
        }
        ItemStack newStack = new ItemStack(this);
        newStack.setDamageValue(stack.getDamageValue() + 1);
        return ItemStackTemplate.fromNonEmptyStack(newStack);
    }
}
