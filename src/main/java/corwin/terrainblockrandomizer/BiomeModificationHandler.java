package com.example.terrainblockrandomizer.world;

import com.example.terrainblockrandomizer.config.Config;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import java.util.Optional;

public class BiomeModificationHandler {

    public static void register() {
        // Register a listener to modify biomes when the server starts or when the world loads
        ServerLifecycleEvents.SERVER_STARTED.register(server -> modifyBiomes());
        ServerWorldEvents.LOAD.register((server, world) -> modifyBiomes());
    }

    private static void modifyBiomes() {
        // Access the config to get the block replacement setting
        String replacementRule = (String) Config.getInstance().getWorldSetting("block_replacement");
        String[] parts = replacementRule.split("->");
        if (parts.length != 2) {
            return; // Invalid config format, do nothing
        }

        Optional<Block> sourceBlockOpt = Registries.BLOCK.getOrEmpty(new Identifier(parts[0]));
        Optional<Block> targetBlockOpt = Registries.BLOCK.getOrEmpty(new Identifier(parts[1]));

        if (sourceBlockOpt.isEmpty() || targetBlockOpt.isEmpty()) {
            return; // Invalid blocks, do nothing
        }

        Block sourceBlock = sourceBlockOpt.get();
        Block targetBlock = targetBlockOpt.get();

        // Use BiomeModification API to apply changes to all biomes
        BiomeModification.INSTANCE.add(BiomeSelectors.all(), (context) -> {
            context.getGenerationSettings().getSurfaceBuilder().ifPresent(surfaceBuilder -> {
                // Logic to modify surface block generation here
            });
        });
    }
}
