package com.example.backrooms26_3.datagen;

import com.example.backrooms26_3.block.ModBlocks;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;

public class ModModelProvider extends FabricModelProvider {
	public ModModelProvider(FabricPackOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
		blockModelGenerators.createTrivialCube(ModBlocks.YELLOW_WALLPAPER);
		blockModelGenerators.createTrivialCube(ModBlocks.MOIST_CARPET);
		blockModelGenerators.createTrivialCube(ModBlocks.CEILING_TILE);
		blockModelGenerators.createTrivialCube(ModBlocks.FLUORESCENT_LIGHT);
		blockModelGenerators.createTrivialCube(ModBlocks.FLUORESCENT_LIGHT_OFF);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerators) {
	}
}
