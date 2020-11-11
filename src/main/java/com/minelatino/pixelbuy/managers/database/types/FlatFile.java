package com.minelatino.pixelbuy.managers.database.types;

import com.google.gson.Gson;
import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.DatabaseType;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlatFile implements DatabaseType {

    private final PixelBuy pl = PixelBuy.get();
    private final File dataFolder = new File(pl.getDataFolder() + File.separator + "playerdata");

    private boolean debug = false;

    public String getType() {
        return "JSON";
    }

    public boolean setup() {
        debug = pl.configBoolean("Database.Debug");
        if (dataFolder.mkdir() && debug) Utils.info(pl.langString("Debug.FlatFile.Folder"));
        return true;
    }

    public void saveData(PlayerData data) {
        String player = data.getPlayer().toLowerCase();
        PlayerData oldData = getData(player);
        if (oldData != null) data.addOrders(oldData.getOrders());
        if (dataFolder.mkdir() && debug) Utils.info(pl.langString("Debug.FlatFile.Folder"));
        File dataFile = new File(dataFolder + File.separator + player + ".json");
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile() && debug) Utils.info(pl.langString("Debug.FlatFile.File").replace("%player%", player));
            } catch (IOException ignored) { }
        }
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

    public PlayerData getData(String player) {
        PlayerData data = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(dataFolder + File.separator + player + ".json"));
            Gson gson = new Gson();
            if (!reader.toString().isEmpty()) data = gson.fromJson(reader, PlayerData.class);
            reader.close();
        } catch (IOException ignored) { }
        return data;
    }

    public List<PlayerData> getAllData() {
        List<PlayerData> datas = new ArrayList<>();
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
