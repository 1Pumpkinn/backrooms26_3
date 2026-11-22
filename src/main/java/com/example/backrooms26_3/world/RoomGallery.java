package com.example.backrooms26_3.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.example.backrooms26_3.Backrooms263;
import com.example.backrooms26_3.block.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Backs the "Backrooms: All Rooms" world type: a void world where every room template of the mod is placed
 * side by side on a grid so they can all be looked at.
 *
 * Rooms are found through the structure template manager, so a new template added anywhere under the mod's
 * structure folder shows up the next time the gallery is built (first join, or /backrooms gallery).
 * Each folder (level_0, later level_1 ...) gets its own block of rows.
 */
public class RoomGallery {
	public static final Identifier DIMENSION_TYPE = Backrooms263.id("gallery");
	public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

	private static final int ROOM_GAP = 4;
	private static final int GROUP_GAP = 12;
	private static final int UPDATE_CLIENTS = 2;

	public static boolean isGallery(ServerLevel level) {
		return level.dimensionTypeRegistration().is(DIMENSION_TYPE);
	}

	public static boolean isBuilt(ServerLevel level) {
		return level.getBlockState(ORIGIN.below()).is(ModBlocks.MOIST_CARPET);
	}

	/** Places every room template on a grid. Returns how many were placed. */
	public static int build(ServerLevel level) {
		StructureTemplateManager manager = level.getServer().getStructureTemplateManager();

		Map<String, List<Identifier>> groups = new TreeMap<>();
		manager.listTemplates()
				.filter(id -> id.getNamespace().equals(Backrooms263.MOD_ID))
				.sorted(Comparator.comparingInt((Identifier id) -> id.getPath().length()).thenComparing(Identifier::getPath))
				.forEach(id -> groups.computeIfAbsent(folder(id), key -> new ArrayList<>()).add(id));

		int placed = 0;
		int groupZ = ORIGIN.getZ();
		for (List<Identifier> group : groups.values()) {
			List<StructureTemplate> templates = new ArrayList<>();
			int pitch = 0;
			for (Identifier id : group) {
				StructureTemplate template = manager.get(id).orElse(null);
				if (template == null) {
					continue;
				}

				Vec3i size = template.getSize();
				pitch = Math.max(pitch, Math.max(size.getX(), size.getZ()) + ROOM_GAP);
				templates.add(template);
			}

			int columns = Math.max(1, (int) Math.ceil(Math.sqrt(templates.size())));
			for (int i = 0; i < templates.size(); i++) {
				StructureTemplate template = templates.get(i);
				BlockPos pos = new BlockPos(ORIGIN.getX() + (i % columns) * pitch, ORIGIN.getY(), groupZ + (i / columns) * pitch);
				placeRoom(level, template, pos);
				placed++;
			}

			int rows = (templates.size() + columns - 1) / columns;
			groupZ += rows * pitch + GROUP_GAP;
		}

		return placed;
	}

	private static void placeRoom(ServerLevel level, StructureTemplate template, BlockPos pos) {
		Vec3i size = template.getSize();
		for (int x = 0; x < size.getX(); x++) {
			for (int z = 0; z < size.getZ(); z++) {
				level.setBlock(pos.offset(x, -1, z), ModBlocks.MOIST_CARPET.defaultBlockState(), UPDATE_CLIENTS);
			}
		}

		template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), RandomSource.create(), UPDATE_CLIENTS);
	}

	private static String folder(Identifier id) {
		int slash = id.getPath().lastIndexOf('/');
		return slash < 0 ? "" : id.getPath().substring(0, slash);
	}

	/** Builds the gallery on first use and keeps players from sitting in the void (first join, respawn). */
	public static void welcome(ServerPlayer player) {
		ServerLevel level = player.level();
		if (!isGallery(level)) {
			return;
		}

		if (!isBuilt(level)) {
			build(level);
		}

		if (player.getY() < ORIGIN.getY() - 4) {
			Vec3 view = new Vec3(ORIGIN.getX() + 8.5, ORIGIN.getY() + 8, ORIGIN.getZ() + 8.5);
			player.teleport(new TeleportTransition(level, view, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
		}
	}
}
