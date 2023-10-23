package com.saicone.pixelbuy.module.data.client;

import com.google.gson.Gson;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.api.store.StoreUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileDatabase implements DataClient {

    private final PixelBuy plugin = PixelBuy.get();
    private final File dataFolder = new File(plugin.getDataFolder() + File.separator + "playerdata");

    @Override
    public @NotNull String getType() {
        return "JSON";
    }

    @Override
    public boolean setup() {
        if (dataFolder.mkdir()) {
            PixelBuy.log(3, "playerdata folder was created because it didn't exist");
        }
        return true;
    }

    @Override
    public void saveData(@NotNull StoreUser data) {
        final String player = data.getName().toLowerCase();
        if (dataFolder.mkdir()) {
            PixelBuy.log(3, "playerdata folder was created because it didn't exist");
        }
        final File dataFile = new File(dataFolder + File.separator + player + ".json");
        if (dataFile.delete()) {
            PixelBuy.log(4, player + " data file was deleted");
        }

        try {
            if (dataFile.createNewFile()) {
                PixelBuy.log(4, player + " data file was created");
            }
        } catch (IOException ignored) { }

        try (FileWriter writer = new FileWriter(dataFile)) {
            final String dataString = new Gson().toJson(data);
            writer.write(dataString);
            writer.flush();
        } catch (IOException e) {
            plugin.getDatabase().addCachedData(data);
            e.printStackTrace();
        }
        //try {
        //    Gson gson = new Gson();
        //    Writer writer = Files.newBufferedWriter(Paths.get(dataFolder + File.separator + player + ".json"));
        //    gson.toJson(data, writer);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    @Override
    public @Nullable StoreUser getData(@NotNull String player) {
        StoreUser data = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(dataFolder + File.separator + player + ".json"))) {
            final Gson gson = new Gson();
            if (!reader.toString().isEmpty()) {
                data = gson.fromJson(reader, StoreUser.class);
            }
        } catch (IOException ignored) { }
        return data;
    }

    @Override
    public @NotNull List<StoreUser> getAllData() {
        final List<StoreUser> datas = new ArrayList<>();
        if (dataFolder.exists()) {
            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                final String name = file.getName().toLowerCase();
                if (name.endsWith(".json")) {
                    datas.add(getData(name.replace(".json", "")));
                }
            }
        }
        return datas;
    }

    @Override
    public void deleteData(@NotNull String player) {
        final File dataFile = new File(dataFolder, player + ".json");
        dataFile.delete();
    }
}
