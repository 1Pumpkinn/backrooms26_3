package com.example.backrooms26_3.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A flat ceiling light panel. It is a thin slab (4 of 16 pixels) that sits flush with the bottom of
 * its block space, so it reads as a panel set into the ceiling instead of a full cube.
 */
public class FluorescentLightBlock extends Block {
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 4, 16);

	public FluorescentLightBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
