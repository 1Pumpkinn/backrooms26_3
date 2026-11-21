package com.example.backrooms26_3.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class BackroomsSpawn {
	public static final int FLOOR_Y = 2;
	private static final int SEARCH_RADIUS = 24;

	public static Vec3 find(ServerLevel level) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
						continue;
					}

					pos.set(dx, FLOOR_Y, dz);
					if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
						return Vec3.atBottomCenterOf(pos);
					}
				}
			}
		}

		return Vec3.atBottomCenterOf(new BlockPos(0, FLOOR_Y, 0));
	}
}
