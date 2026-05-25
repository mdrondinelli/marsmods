package me.mar.foodspoilage;

import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

final class ModBusEvents {
    private ModBusEvents() {
    }

    static void makeFoodUnstackable(ModifyDefaultComponentsEvent event) {
        event.modifyMatching(
                (item, components) -> components.get(DataComponents.FOOD) != null
                        && !item.builtInRegistryHolder().is(ModTags.Items.DOES_NOT_SPOIL),
                (components, context, item) -> components.set(DataComponents.MAX_STACK_SIZE, 1));
    }
}
