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
    private final ContainerData kilnData;

    public KilnMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(3), new SimpleContainerData(KilnBlockEntity.DATA_COUNT));
    }

    public KilnMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.KILN.get(), RecipeType.SMELTING, RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE, containerId, inventory, container, data);
        this.kilnData = data;
        KilnResultSlot resultSlot = new KilnResultSlot(inventory.player, container, 2, 116, 35);
        resultSlot.index = 2;
        this.slots.set(2, resultSlot);
    }

    public int getCurrentTemperature() {
        return this.kilnData.get(KilnBlockEntity.DATA_CURRENT_TEMPERATURE);
    }

    public int getRequiredTemperature() {
        return this.kilnData.get(KilnBlockEntity.DATA_REQUIRED_TEMPERATURE);
    }
}
