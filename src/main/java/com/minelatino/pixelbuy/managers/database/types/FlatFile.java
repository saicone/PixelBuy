package com.minelatino.pixelbuy.managers.database.types;

import com.google.gson.Gson;
import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.database.DatabaseType;
import com.minelatino.pixelbuy.managers.player.PlayerData;
import com.minelatino.pixelbuy.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlatFile implements DatabaseType {

    private final PixelBuy pl = PixelBuy.get();
    private final File dataFolder = new File(pl.getDataFolder() + File.separator + "playerdata");

    public String getType() {
        return "JSON";
    }

    public boolean setup() {
        if (dataFolder.mkdir()) Utils.info(pl.getFiles().getLang().getString(""));
        return true;
    }

    public void saveData(PlayerData data) {
        String player = data.getPlayer().toLowerCase();
        PlayerData oldData = getData(player);
        if (oldData != null) data.addCommands(oldData.getCommands());
        File dataFile = new File(dataFolder, player + ".json");
        try {
            Gson gson = new Gson();
            gson.toJson(data, new FileWriter(dataFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData getData(String player) {
        PlayerData data = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(dataFolder + File.separator + player + ".json"));
            Gson gson = new Gson();
            data = gson.fromJson(reader, PlayerData.class);
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
