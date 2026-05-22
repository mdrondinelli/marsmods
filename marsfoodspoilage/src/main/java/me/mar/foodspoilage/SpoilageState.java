package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum SpoilageState {
    FRESH,
    STALE,
    SPOILED;

    public static final Codec<SpoilageState> CODEC = Codec.STRING
            .xmap(s -> SpoilageState.valueOf(s.toUpperCase(Locale.ROOT)),
                  state -> state.name().toLowerCase(Locale.ROOT));
}
