package me.mar.bellows;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;

public class KilnMenu extends AbstractFurnaceMenu {
    public KilnMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.KILN.get(), RecipeType.SMELTING, RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE, containerId, inventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public KilnMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.KILN.get(), RecipeType.SMELTING, RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE, containerId, inventory, container, data);
    }
}
