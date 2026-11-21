package com.example.backrooms26_3.world;

import com.example.backrooms26_3.Backrooms263;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BackroomsDimensions {
	public static final ResourceKey<Level> BACKROOMS = ResourceKey.create(Registries.DIMENSION, Backrooms263.id("backrooms"));
}
