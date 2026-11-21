package com.example.backrooms26_3;

import com.example.backrooms26_3.block.ModBlocks;
import com.example.backrooms26_3.command.ModCommands;
import com.example.backrooms26_3.item.ModItemGroups;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Backrooms263 implements ModInitializer {
	public static final String MOD_ID = "backrooms26_3";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModItemGroups.init();
		ModCommands.init();

		LOGGER.info("You are not supposed to be here.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
