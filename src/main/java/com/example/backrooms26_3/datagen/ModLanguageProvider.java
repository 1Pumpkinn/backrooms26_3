package com.example.backrooms26_3.datagen;

import java.util.concurrent.CompletableFuture;

import com.example.backrooms26_3.block.ModBlocks;
import com.example.backrooms26_3.item.ModItemGroups;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.minecraft.core.HolderLookup;

public class ModLanguageProvider extends FabricLanguageProvider {
	public ModLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
		translationBuilder.add(ModBlocks.YELLOW_WALLPAPER, "Yellow Wallpaper");
		translationBuilder.add(ModBlocks.MOIST_CARPET, "Moist Carpet");
		translationBuilder.add(ModBlocks.CEILING_TILE, "Ceiling Tile");
		translationBuilder.add(ModBlocks.FLUORESCENT_LIGHT, "Fluorescent Light");
		translationBuilder.add(ModBlocks.FLUORESCENT_LIGHT_OFF, "Fluorescent Light (Off)");

		translationBuilder.add(ModItemGroups.BACKROOMS, "Backrooms");

		translationBuilder.add("generator.backrooms26_3.all_rooms", "Backrooms: All Rooms");

		translationBuilder.add("message.backrooms26_3.entered", "You noclipped out of reality.");
		translationBuilder.add("message.backrooms26_3.left", "You found your way back.");
		translationBuilder.add("message.backrooms26_3.gallery_built", "Built the room gallery (%s rooms).");
		translationBuilder.add("message.backrooms26_3.not_gallery", "This is not a Backrooms: All Rooms world.");
		translationBuilder.add("message.backrooms26_3.dimension_missing", "The Backrooms dimension is not loaded.");
	}
}
