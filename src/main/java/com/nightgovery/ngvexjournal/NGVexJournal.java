package com.nightgovery.ngvexjournal;

import com.google.common.collect.Lists;
import lk.vexview.api.VexViewAPI;
import lk.vexview.event.ButtonClickEvent;
import lk.vexview.gui.VexGui;
import lk.vexview.gui.components.VexButton;
import lk.vexview.gui.components.VexText;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Pointer;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.database.PlayerData;

import java.text.SimpleDateFormat;
import java.util.*;

public class NGVexJournal extends JavaPlugin implements Listener {

    private String guiImg;
    private String buttonImg;
    private String buttonHoverImg;
    private int guiX;
    private int guiY;
    private int guiW;
    private int guiH;
    private int guiXS;
    private int guiYS;
    private int lineLimit;
    private int pageLine;
    private int start_x;
    private int start_w;
    private int start_h;
    private  int start_id;
    private String start_string;
    private int stopgps_x;
    private int stopgps_y;
    private int stopgps_w;
    private int stopgps_h;
    private  int stopgps_id;
    private String stopgps_string;
    private String stop_command;
    private int prev_x;
    private int prev_y;
    private int prev_w;
    private int prev_h;
    private  int prev_id;
    private String prev_string;
    private int next_x;
    private int next_y;
    private int next_w;
    private int next_h;
    private  int next_id;
    private String next_string;

    private Map<Player, Map<Integer, String>> buttonMap = new HashMap<>();
    private Map<Player, Integer> playerPage = new HashMap<>();

    private boolean isGPSEnabled = false;
    private boolean isPAPIEnabled = false;

    @Override
    public void onEnable() {
        loadConfig();
        Bukkit.getConsoleSender().sendMessage("§c原创作: §ewillz §c原名:§e[VexJournal]");
        Bukkit.getConsoleSender().sendMessage("§c一接: §eHellmessage");
        Bukkit.getConsoleSender().sendMessage("§c现作者: §e夜政");
        Bukkit.getConsoleSender().sendMessage("§c版本: §eV1.2.1");
        Bukkit.getConsoleSender().sendMessage("§cVV支持: §eV2.1.3之后");
        Bukkit.getConsoleSender().sendMessage("§cQQ: §e208989395");
        getServer().getPluginManager().registerEvents(this, this);
        isGPSEnabled = getServer().getPluginManager().isPluginEnabled("GPS");
        isPAPIEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        FileConfiguration config = getConfig();
        guiImg = config.getString("gui", "[local]guiImg.png");
        buttonImg = config.getString("button", "[local]button.png");
        buttonHoverImg = config.getString("button_hover", "[local]button.png");
        guiX = config.getInt("gui-x", -1);
        guiY = config.getInt("gui-y", -1);
        guiW = config.getInt("gui-w", 200);
        guiH = config.getInt("gui-h", 200);
        guiXS = config.getInt("gui-xs", 200);
        guiYS = config.getInt("gui-ys", 200);
        lineLimit = config.getInt("line-limit", 20);
        pageLine = config.getInt("page-line", 16);
        start_x = config.getInt("start-x", -25);
        start_w = config.getInt("start-w", 22);
        start_h = config.getInt("start-h", 16);
        start_id = config.getInt("start-id", 2020040499);
        start_string = config.getString("start-string", "导航");
        stopgps_x = config.getInt("stopgps-x", 203);
        stopgps_y = config.getInt("stopgps-y", 10);
        stopgps_w = config.getInt("stopgps-w", 42);
        stopgps_h = config.getInt("stopgps-h", 16);
        stopgps_id = config.getInt("stopgps-id", 2020040498);
        stopgps_string = config.getString("stopgps-string", "停止导航");
        stop_command = config.getString("stopgps-command", "gps stop");
        prev_x = config.getInt("prev-x", 203);
        prev_y = config.getInt("prev-y", 35);
        prev_w = config.getInt("prev-w", 42);
        prev_h = config.getInt("prev-h", 16);
        prev_id = config.getInt("prev-id", 2020040497);
        prev_string = config.getString("prev-string", "上一页");
        next_x = config.getInt("next-x", 203);
        next_y = config.getInt("next-y", 60);
        next_w = config.getInt("next-w", 42);
        next_h = config.getInt("next-h", 16);
        next_id = config.getInt("next-id", 2020040496);
        next_string = config.getString("next-string", "下一页");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && sender.isOp() && args[0].equals("reload")) {
            loadConfig();
            sender.sendMessage("VexJournal重载配置！");
            return true;
        }

        if (sender instanceof Player) {
            int page = 0;
            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (Exception ignored) { }
            }

            Player player = (Player) sender;
            playerPage.put(player, page);

            //int realHeight = VexViewAPI.getPlayerClientWindowHeight(player);
            //int realWidth = VexViewAPI.getPlayerClientWindowWidth(player);

            VexGui vg = new VexGui(guiImg, guiX, guiY, guiW, guiH, guiXS, guiYS);
            PlayerData pd = BetonQuest.getInstance().getPlayerData(player.getUniqueId().toString());
            List<Pointer> pointers = Lists.reverse(pd.getJournal().getPointers());
            int line = 0;
            int buttonId = start_id;
            Map<Integer, String> bm = new HashMap<>();
            int currentPage = 0;
            boolean hasNext = false;
            Iterator<Pointer> it = pointers.iterator();
            while (it.hasNext()) {
                Pointer p = it.next();
                String datePrefix = "";
                String pointerName;
                if (Config.getString("config.journal.hide_date").equalsIgnoreCase("false")) {
                    String parts = (new SimpleDateFormat(Config.getString("config.date_format"))).format(p.getTimestamp());
                    String[] packName = parts.split(" ");
                    String pack = "§" + Config.getString("config.journal_colors.date.day") + packName[0];
                    pointerName = "";
                    if (packName.length > 1) {
                        pointerName = "§" + Config.getString("config.journal_colors.date.hour") + packName[1];
                    }
                    datePrefix = pack + " " + pointerName;
                }
                String[] parts1 = p.getPointer().split("\\.");
                String packName1 = parts1[0];
                ConfigPackage pack1 = Config.getPackages().get(packName1);
                if (pack1 != null && parts1.length >= 2) {
                    pointerName = parts1[1];
                    String text;
                    if (pack1.getJournal().getConfig().isConfigurationSection(pointerName)) {
                        text = pack1.getString("journal." + pointerName + ".cn");
                        if (text == null) {
                            text = pack1.getString("journal." + pointerName + ".cn");
                        }
                    } else {
                        text = pack1.getString("journal." + pointerName);
                    }
                    if (text == null) {
                        continue;
                    }
                    List<String> record = new ArrayList<>();
                    if(isPAPIEnabled) {
                        text = PlaceholderAPI.setPlaceholders(player, text);
                    }
                    text = text.replaceAll("&", "§");
                    int ty = 10 + line * 10;
                    int curLoopLine = 1;
                    record.add(datePrefix + "§" + Config.getString("config.journal_colors.text"));

                    String[] lines = text.split("\n");
                    for (String sline : lines) {
                        curLoopLine++;
                        text = sline;
                        while (text.length() > lineLimit) {
                            String cut = text.substring(0, lineLimit);
                            text = text.substring(lineLimit);
                            record.add(cut);
                            curLoopLine++;
                        }
                        record.add(text);
                    }

                    if (line + curLoopLine > pageLine) {
                        currentPage++;
                        if (currentPage > page) {
                            break;
                        }
                        ty = 10;
                        line = curLoopLine;
                    } else {
                        line += curLoopLine;
                    }

                    if(currentPage == page) {
                        vg.addComponent(new VexText(10, ty, record));
                        String gps = pack1.getString("journal." + pointerName + ".command.");
                        bm.put(buttonId, gps);
                        if (gps != null && isGPSEnabled) {
                            vg.addComponent(new VexButton(buttonId++, start_string, buttonImg, buttonHoverImg, start_x, ty, start_w, start_h));
                        }
                    }
                }

                hasNext = it.hasNext();
                if (currentPage > page || !hasNext) {
                    break;
                }
            }

            buttonMap.put(player, bm);
            if(isGPSEnabled){
                vg.addComponent(new VexButton(stopgps_id, stopgps_string, buttonImg, buttonHoverImg, stopgps_x, stopgps_y, stopgps_w, stopgps_h));
            }
            if(page > 0) {
                vg.addComponent(new VexButton(prev_id, prev_string, buttonImg, buttonHoverImg, prev_x, prev_y, prev_w, prev_h));
            }
            if(hasNext) {
                vg.addComponent(new VexButton(next_id, next_string, buttonImg, buttonHoverImg, next_x, next_y, next_w, next_h));
            }
            VexViewAPI.openGui(player, vg);
        }
        return true;
    }


    @EventHandler
    public void onButton(ButtonClickEvent event) {
        Object temp = event.getButtonID();
        if (NumberUtils.isNumber(temp.toString())) {
            int id = (int) event.getButtonID();
            if (id >= start_id) {
                Map<Integer, String> bm = buttonMap.getOrDefault(event.getPlayer(), null);
                if (bm != null) {
                    String gps = bm.getOrDefault(id, "unknown");
                    event.getPlayer().performCommand(gps);
                    event.getPlayer().closeInventory();
                }
            } else {
                int page;
                if(id == stopgps_id){
                    event.getPlayer().performCommand(stop_command);
                    event.getPlayer().closeInventory();
                }else if(id == prev_id){
                    page = playerPage.getOrDefault(event.getPlayer(), 0) - 1;
                    event.getPlayer().performCommand("vj " + page);
                }else if(id == next_id){
                    page = playerPage.getOrDefault(event.getPlayer(), 0) + 1;
                    event.getPlayer().performCommand("vj " + page);
                }
            }
        }
    }


}
