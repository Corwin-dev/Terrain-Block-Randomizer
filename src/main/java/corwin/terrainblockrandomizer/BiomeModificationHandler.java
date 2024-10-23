package com.example.terrainblockrandomizer.world;

import com.example.terrainblockrandomizer.config.Config;
import com.example.terrainblockrandomizer.TerrainBlockRandomizer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import org.slf4j.Logger;

import java.util.Optional;

public class BiomeModificationHandler {
    private static final Logger LOGGER = TerrainBlockRandomizer.LOGGER;

    public static void register() {
        LOGGER.info("Registering biome modifications for {}", TerrainBlockRandomizer.MOD_ID);

        // Register a listener to modify biomes when the server starts or when the world loads
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Modifying biomes on server start.");
            modifyBiomes();
        });
        ServerWorldEvents.LOAD.register((server, world) -> {
            LOGGER.info("Modifying biomes when world {} loads.", world.getRegistryKey().getValue());
            modifyBiomes();
        });
    }

    private static void modifyBiomes() {
        // Access the config to get the block replacement setting
        String replacementRule = (String) Config.getInstance().getWorldSetting("block_replacement");
        String[] parts = replacementRule.split("->");
        if (parts.length != 2) {
            LOGGER.warn("Invalid config format for block replacement.");
            return; // Invalid config format, do nothing
        }

        Optional<Block> sourceBlockOpt = Registries.BLOCK.getOrEmpty(new Identifier(parts[0]));
        Optional<Block> targetBlockOpt = Registries.BLOCK.getOrEmpty(new Identifier(parts[1]));

        if (sourceBlockOpt.isEmpty() || targetBlockOpt.isEmpty()) {
            LOGGER.warn("Invalid blocks specified in config: {}", replacementRule);
            return; // Invalid blocks, do nothing
        }

        Block sourceBlock = sourceBlockOpt.get();
        Block targetBlock = targetBlockOpt.get();

        // Use BiomeModifications to modify the biomes
        BiomeModifications.add(
            ModificationPhase.ADDITIONS,
            BiomeSelectors.all(),
            (selectionContext, modificationContext) -> {
                LOGGER.info("Modifying biome: {}", selectionContext.getBiomeKey().getValue());

                // Example of modifying the surface builder of the biome
                modificationContext.getGenerationSettings().surfaceBuilder().ifPresent(surfaceBuilder -> {
                    // Add your logic for modifying the surface builder or other biome properties
                    LOGGER.info("Surface builder modified for biome: {}", selectionContext.getBiomeKey().getValue());
                });
            }
        );
    }
}
