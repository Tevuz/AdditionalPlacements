package com.firemerald.additionalplacements.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import com.firemerald.additionalplacements.common.TagMismatchChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;

public class CommandExportTags
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final String PACK_FOLDER_NAME = "additional_placements_generated_tags";
	public static final ResourceLocation PACK_META_LOC = new ResourceLocation(AdditionalPlacementsMod.MOD_ID, "generated_datapack_meta.mcmeta");

	public static void optionalMakeDirectory(Path path) throws IOException
	{
        if (!Files.exists(path)) Files.createDirectory(path);
        else if (!Files.isDirectory(path))
        {
        	Files.delete(path);
        	Files.createDirectory(path);
        }
	}

	public static void optionalMakeDirectories(Path path) throws IOException
	{
        if (!Files.exists(path)) Files.createDirectories(path);
        else if (!Files.isDirectory(path))
        {
        	Files.delete(path);
        	Files.createDirectory(path);
        }
	}

	public static void emptyDirectory(Path path) throws IOException
	{
		Iterator<Path> it = Files.list(path).iterator();
		while (it.hasNext())
		{
			Path file = it.next();
			if (Files.isDirectory(file)) emptyDirectory(file);
			Files.delete(file);
		}
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatch)
	{
		dispatch.register(Commands.literal("ap_tags_export").requires(TagMismatchChecker::canGenerateTags).executes(context -> {
			CommandSourceStack source = context.getSource();
			MinecraftServer server = source.getServer();
			Path packPath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACK_FOLDER_NAME);
			try
			{
				optionalMakeDirectory(packPath);
				try
				{
					Files.copy(CommandExportTags.class.getResource("/assets/" + PACK_META_LOC.getNamespace() + "/" + PACK_META_LOC.getPath()).openStream(), packPath.resolve("pack.mcmeta"), StandardCopyOption.REPLACE_EXISTING);
				}
				catch (IOException e)
				{
					AdditionalPlacementsMod.LOGGER.error("Error generating datapack: failed to copy pack definition", e);
					source.sendFailure(Component.translatable("msg.additionalplacements.generate.failure.definition"));
				}
				Path dataPath = packPath.resolve("data");
				if (Files.exists(dataPath))
				{
					if (Files.isDirectory(dataPath)) emptyDirectory(dataPath);
					else
					{
						Files.delete(dataPath);
						Files.createDirectory(dataPath);
					}
				}
				else Files.createDirectory(dataPath);
				Map<TagKey<Block>, List<ResourceLocation>> tagMap = new HashMap<>();
				BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
					Block block = entry.getValue();
					if (block instanceof AdditionalPlacementBlock)
					{
						Set<TagKey<Block>> tags = ((AdditionalPlacementBlock<?>) block).getDesiredTags();
						tags.forEach(tag -> tagMap.computeIfAbsent(tag, key -> new LinkedList<>()).add(entry.getKey().location()));
					}
				});
				tagMap.forEach((tag, blocks) -> {
					try
					{
						Path tagPath = dataPath.resolve(tag.location().getNamespace() + "/tags/blocks/" + tag.location().getPath() + ".json");
						optionalMakeDirectories(tagPath.getParent());
						JsonObject obj = new JsonObject();
						obj.addProperty("replace", false);
						JsonArray array = new JsonArray();
						blocks.forEach(block -> array.add(block.toString()));
						obj.add("values", array);
						Files.writeString(tagPath, GSON.toJson(obj), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					}
					catch (IOException e)
					{
						AdditionalPlacementsMod.LOGGER.error("Error generating datapack: failed to save tag " + tag.location().toString(), e);
						source.sendFailure(Component.translatable("msg.additionalplacements.generate.failure.tag", tag.location().toString()));
					}
				});
				AdditionalPlacementsMod.LOGGER.info("Finished exporting tags");
				source.sendSuccess(() -> Component.translatable("msg.additionalplacements.generate.success"), true);
			}
			catch (IOException e)
			{
				AdditionalPlacementsMod.LOGGER.error("Error generating datapack: failed to initialize datapack", e);
				source.sendFailure(Component.translatable("msg.additionalplacements.generate.failure.initialization"));
			}
			return 0;
		}));
	}
}