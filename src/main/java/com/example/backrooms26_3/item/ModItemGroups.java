package com.example.backrooms26_3.item;

import com.example.backrooms26_3.Backrooms263;
import com.example.backrooms26_3.block.ModBlocks;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
	public static final ResourceKey<CreativeModeTab> BACKROOMS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Backrooms263.id("backrooms"));

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BACKROOMS, FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(ModBlocks.FLUORESCENT_LIGHT))
				.title(Component.translatable("itemGroup.backrooms26_3.backrooms"))
				.build());

		CreativeModeTabEvents.modifyOutputEvent(BACKROOMS).register(entries -> {
			entries.accept(ModBlocks.YELLOW_WALLPAPER);
			entries.accept(ModBlocks.MOIST_CARPET);
			entries.accept(ModBlocks.CEILING_TILE);
			entries.accept(ModBlocks.FLUORESCENT_LIGHT);
			entries.accept(ModBlocks.FLUORESCENT_LIGHT_OFF);
		});
	}
}
