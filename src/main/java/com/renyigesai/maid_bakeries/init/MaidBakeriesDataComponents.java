package com.renyigesai.maid_bakeries.init;

import com.mojang.serialization.Codec;
import com.renyigesai.maid_bakeries.MaidBakeries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MaidBakeriesDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTER =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MaidBakeries.MODID);

    public static final Supplier<DataComponentType<String>> TEXT =
            REGISTER.register("flavor_text",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build()
            );

    public static final Supplier<DataComponentType<Integer>> INT =
            REGISTER.register("int",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );
}
