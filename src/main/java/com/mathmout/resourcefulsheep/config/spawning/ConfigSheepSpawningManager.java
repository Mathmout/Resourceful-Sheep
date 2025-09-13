package com.mathmout.resourcefulsheep.config.spawning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigSheepSpawningManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_spawning");
    private static final List<SheepSpawningData> SHEEP_SPAWNING = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSheepSpawningManager.class);

}
