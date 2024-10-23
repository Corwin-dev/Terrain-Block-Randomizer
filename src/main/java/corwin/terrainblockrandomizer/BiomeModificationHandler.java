package com.example.terrainblockrandomizer.world;

import com.example.terrainblockrandomizer.config.Config;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceRuleData;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
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

        Optional<Block> sourceBlockOpt = Registry.BLOCK.get(new Identifier(parts[0]));
        Optional<Block> targetBlockOpt = Registry.BLOCK.get(new Identifier(parts[1]));

        if (sourceBlockOpt.isEmpty() || targetBlockOpt.isEmpty()) {
            return; // Invalid blocks, do nothing
        }

        Block sourceBlock = sourceBlockOpt.get();
        Block targetBlock = targetBlockOpt.get();

        // Use BiomeModification API to apply changes to all biomes
        BiomeModification.create(new Identifier("terrainblockrandomizer", "modify_biomes"))
                .add(BiomeSelectors.all(), (context) -> {
                    context.getGenerationSettings().surfaceRule().ifPresent(surfaceRule -> {
                        MaterialRules.RuleSource modifiedRule = SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBlock(sourceBlock),
                                        SurfaceRules.block(targetBlock.getDefaultState())
                                ),
                                surfaceRule
                        );
                        context.getGenerationSettings().setSurfaceRule(modifiedRule);
                    });
                });
    }
}
