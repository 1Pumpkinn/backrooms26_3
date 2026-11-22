package com.example.backrooms26_3.command;

import com.example.backrooms26_3.world.BackroomsDimensions;
import com.example.backrooms26_3.world.BackroomsSpawn;
import com.example.backrooms26_3.world.RoomGallery;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class ModCommands {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
				dispatcher.register(Commands.literal("backrooms")
						.then(Commands.literal("enter").executes(ModCommands::enter))
						.then(Commands.literal("leave").executes(ModCommands::leave))
						.then(Commands.literal("gallery").executes(ModCommands::gallery))));
	}

	private static int enter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel backrooms = source.getServer().getLevel(BackroomsDimensions.BACKROOMS);
		if (backrooms == null) {
			source.sendFailure(Component.translatable("message.backrooms26_3.dimension_missing"));
			return 0;
		}

		player.teleport(new TeleportTransition(backrooms, BackroomsSpawn.find(backrooms), Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
		source.sendSuccess(() -> Component.translatable("message.backrooms26_3.entered"), false);
		return 1;
	}

	private static int leave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return 0;
		}

		Vec3 target = Vec3.atBottomCenterOf(player.adjustSpawnLocation(overworld, overworld.getRespawnData().pos()));
		player.teleport(new TeleportTransition(overworld, target, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
		source.sendSuccess(() -> Component.translatable("message.backrooms26_3.left"), false);
		return 1;
	}

	private static int gallery(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		if (!RoomGallery.isGallery(level)) {
			source.sendFailure(Component.translatable("message.backrooms26_3.not_gallery"));
			return 0;
		}

		int placed = RoomGallery.build(level);
		source.sendSuccess(() -> Component.translatable("message.backrooms26_3.gallery_built", placed), true);
		return placed;
	}
}
