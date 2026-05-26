package me.mar.bellows;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, MarsBellows.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<KilnMenu>> KILN =
            MENU_TYPES.register(KilnBlock.NAME, () -> new MenuType<>(KilnMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
