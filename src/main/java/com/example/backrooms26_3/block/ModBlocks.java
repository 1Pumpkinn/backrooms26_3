package com.example.backrooms26_3.block;

import java.util.function.Function;

import com.example.backrooms26_3.Backrooms263;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
	public static final BlockItemId YELLOW_WALLPAPER_ID = id("yellow_wallpaper");
	public static final BlockItemId MOIST_CARPET_ID = id("moist_carpet");
	public static final BlockItemId CEILING_TILE_ID = id("ceiling_tile");
	public static final BlockItemId FLUORESCENT_LIGHT_ID = id("fluorescent_light");
	public static final BlockItemId FLUORESCENT_LIGHT_OFF_ID = id("fluorescent_light_off");

	public static final Block YELLOW_WALLPAPER = register(YELLOW_WALLPAPER_ID, Block::new, indestructible(MapColor.COLOR_YELLOW, SoundType.STONE));
	public static final Block MOIST_CARPET = register(MOIST_CARPET_ID, Block::new, indestructible(MapColor.COLOR_BROWN, SoundType.WOOL));
	public static final Block CEILING_TILE = register(CEILING_TILE_ID, Block::new, indestructible(MapColor.SNOW, SoundType.STONE));

	// Lights are two separate blocks (on / off) so the flicker and blackout events in a later batch
	// only need to swap block states of the world, and every model can come from plain datagen.
	public static final Block FLUORESCENT_LIGHT = register(FLUORESCENT_LIGHT_ID, Block::new,
			indestructible(MapColor.SNOW, SoundType.GLASS).lightLevel(state -> 15));
	public static final Block FLUORESCENT_LIGHT_OFF = register(FLUORESCENT_LIGHT_OFF_ID, Block::new,
			indestructible(MapColor.SNOW, SoundType.GLASS));

	private static BlockBehaviour.Properties indestructible(MapColor color, SoundType sound) {
		return BlockBehaviour.Properties.of()
				.mapColor(color)
				.sound(sound)
				.strength(-1.0F, 3600000.0F)
				.noLootTable();
	}

	private static BlockItemId id(String name) {
		return BlockItemId.create(Backrooms263.id(name), Backrooms263.id(name));
	}

	private static <T extends Block> T register(BlockItemId id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		T block = Registry.register(BuiltInRegistries.BLOCK, id.block(), factory.apply(properties.setId(id.block())));
		Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, new Item.Properties().setId(id.item())));
		return block;
	}

	public static void init() {
	}
}
