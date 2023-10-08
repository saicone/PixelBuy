package com.saicone.pixelbuy.module.data.client;

import com.google.gson.Gson;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.module.data.DataClient;
import com.saicone.pixelbuy.api.object.StoreUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileDatabase implements DataClient {

    private final PixelBuy pl = PixelBuy.get();
    private final File dataFolder = new File(pl.getDataFolder() + File.separator + "playerdata");

    public String getType() {
        return "JSON";
    }

    public boolean setup() {
        if (dataFolder.mkdir()) {
            PixelBuy.log(3, "playerdata folder was created because it didn't exist");
        }
        return true;
    }

    public void saveData(StoreUser data) {
        String player = data.getPlayer().toLowerCase();
        if (dataFolder.mkdir()) {
            PixelBuy.log(3, "playerdata folder was created because it didn't exist");
        }
        File dataFile = new File(dataFolder + File.separator + player + ".json");
        if (dataFile.delete()) {
            PixelBuy.log(4, player + " data file was deleted");
        }

        try {
            if (dataFile.createNewFile()) {
                PixelBuy.log(4, player + " data file was created");
            }
        } catch (IOException ignored) { }

        try {
            FileWriter writer = new FileWriter(dataFile);
            String dataString = new Gson().toJson(data);
            writer.write(dataString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            pl.getDatabase().addCachedData(data);
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

    public StoreUser getData(String player) {
        StoreUser data = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(dataFolder + File.separator + player + ".json"));
            Gson gson = new Gson();
            if (!reader.toString().isEmpty()) data = gson.fromJson(reader, StoreUser.class);
            reader.close();
        } catch (IOException ignored) { }
        return data;
    }

    public List<StoreUser> getAllData() {
        List<StoreUser> datas = new ArrayList<>();
        if (dataFolder.exists()) {
            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".json")) datas.add(getData(name.replace(".json", "")));
            }
        }
        return datas;
    }

    public void deleteData(String player) {
        File dataFile = new File(dataFolder, player + ".json");
        dataFile.delete();
    }
}
