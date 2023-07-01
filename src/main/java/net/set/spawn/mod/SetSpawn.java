package net.set.spawn.mod;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Objects;

public class SetSpawn implements ClientModInitializer {
    public static final String MOD_ID = "setspawnmod";
    public static final String subDir = SetSpawn.MOD_ID + "_global";
    public static Logger LOGGER = LogManager.getLogger();
    public static boolean shouldModifySpawn;
    public static boolean shouldSendErrorMessage;
    public static String errorMessage;
    public static File localConfigFile;
    public static File globalConfigFile;
    public static Config config;

    private static void createIfNonExistent(File file) {
        try {
            if (file.createNewFile()) {
                writeDefaultProperties(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() throws IOException, NumberFormatException, JsonSyntaxException {
        Gson gson = new Gson();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(localConfigFile));
        config = gson.fromJson(bufferedReader, Config.class);
        if (config.isUseGlobalConfig()) {
            bufferedReader = new BufferedReader(new FileReader(globalConfigFile));
            config = gson.fromJson(bufferedReader, Config.class);
        }
    }

    private static void writeDefaultProperties(File file) throws IOException {
        Seed[] seedsToWrite = new Seed[]{};
        Config config = new Config(true, false, seedsToWrite);

        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Seed findSeedObjectFromLong(long seedLong) {
        String seed = String.valueOf(seedLong);
        Seed[] seedObjects = config.getSeeds();
        for (Seed seedObject : seedObjects) {
            if (Objects.equals(seedObject.getSeed(), seed)) {
                return seedObject;
            }
        }
        return null;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing");
        File globalDir = new File(System.getProperty("user.home").replace("\\", "/"), subDir);
        globalDir.mkdirs();
        globalConfigFile = new File(globalDir, "setspawn.json");
        localConfigFile = FabricLoader.getInstance().getConfigDir().resolve("setspawn.json").toFile();
        createIfNonExistent(globalConfigFile);
        createIfNonExistent(localConfigFile);
        try {
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}