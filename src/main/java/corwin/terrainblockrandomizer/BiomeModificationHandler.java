package com.example.terrainblockrandomizer.world;

import com.example.terrainblockrandomizer.config.Config;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.registry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilderConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;

import java.util.Optional;

public class BiomeModificationHandler {

    public static void register() {
        // Register a listener to modify biomes when the server starts or when the world loads
        ServerLifecycleEvents.SERVER_STARTED.register(server -> modifyBiomes());
        ServerWorldEvents.LOAD.register((server, world) -> modifyBiomes());
    }

    private static void modifyBiomes() {
        // Access the config to get the block replacement setting
        String replacementRule = (String) Config.getInstance(null).getWorldSetting("block_replacement");
        String[] parts = replacementRule.split("->");
        if (parts.length != 2) {
            return; // Invalid config format, do nothing
        }

        Optional<Block> sourceBlockOpt = Registry.BLOCK.getOrEmpty(new net.minecraft.util.Identifier(parts[0]));
        Optional<Block> targetBlockOpt = Registry.BLOCK.getOrEmpty(new net.minecraft.util.Identifier(parts[1]));

        if (sourceBlockOpt.isEmpty() || targetBlockOpt.isEmpty()) {
            return; // Invalid blocks, do nothing
        }

        Block sourceBlock = sourceBlockOpt.get();
        Block targetBlock = targetBlockOpt.get();

        // Use BiomeModification API to apply changes to all biomes
        BiomeModification worldBiomeModification = BiomeModification.create(new net.minecraft.util.Identifier("terrainblockrandomizer", "modify_biomes"));
        worldBiomeModification.add(BiomeSelectors.all(), (context) -> {
            // Get the biome and modify its generation settings
            GenerationSettings.Builder generationSettings = context.getGenerationSettings();
            
            // Check if the biome has a SurfaceBuilder and replace its block
            context.getSurfaceBuilder().ifPresent(builder -> {
                if (builder instanceof SurfaceBuilderConfig surfaceConfig) {
                    // Check if the source block matches and replace it with the target block
                    if (surfaceConfig.getTopMaterial().isOf(sourceBlock)) {
                        SurfaceBuilderConfig newConfig = new SurfaceBuilderConfig(
                            targetBlock.getDefaultState(), 
                            surfaceConfig.getUnderMaterial(), 
                            surfaceConfig.getUnderwaterMaterial()
                        );
                        context.setSurfaceBuilder(() -> SurfaceBuilder.DEFAULT.configured(newConfig));
                    }
                }
            });
        });
    }
}
