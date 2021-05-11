package com.minelatino.pixelbuy.module.locale;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.module.config.Settings;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class PixelLocale {

    final Settings file;
    int loglevel;

    public PixelLocale(Settings file) {
        this.file = file;
    }

    void load() { }

    public void reload() {
        String path = "lang/" + PixelBuy.SETTINGS.getString("Locale.Language") + ".yml";
        if (!file.getPath().equals(path)) {
            file.setPath(path);
            file.reload();
        }
        loglevel = PixelBuy.SETTINGS.getInt("Locale.LogLevel");
    }

    public void log(int level, String path, String... args) {
        if (level <= loglevel) {
            sendToConsole(path, args);
        }
    }

    public abstract void sendToConsole(String path, String... args);

    public abstract void sendTo(Object user, String path, String... args);

    public abstract void sendMessage(Object user, String text, String... args);

    public abstract void broadcast(String text, String... args);

    public abstract void broadcastPath(String path, String... args);

    public void sendTitle(Object user, String path, String... args) { }

    public void sendTitle(Object user, String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) { }

    public void broadcastTitle(String path, String... args) { }

    public void broadcastTile(String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) { }

    public void sendActionbar(Object user, String path, String... args) { }

    public void sendActionbar(Object user, String text, int pulses, String... args) { }

    public void broadcastActionbar(String path, String... args) { }

    public void broadcastActionbar(String text, int pulses, String... args) { }

    public List<String> color(List<String> list) {
        List<String> l = new ArrayList<>();
        list.forEach(s -> l.add(color(s)));
        return l;
    }

    public String color(String s) {
        return s;
    }

    public List<String> replaceArgs(List<String> list, String... args) {
        if (args.length < 1) return list;
        List<String> l0 = new ArrayList<>();
        list.forEach(s -> list.add(replaceArgs0(s, args)));
        return l0;
    }

    public String replaceArgs(String s, String... args) {
        if (args.length < 1) return s;
        return replaceArgs0(s, args);
    }

    private String replaceArgs0(String s, String... args) {
        return color(MessageFormat.format(s, (Object[]) args));
    }
}
