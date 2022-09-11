package com.mc.dungeon;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.easycommands.commands.CMDManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mc.dungeon.commands.FrameGeneration;
import com.mc.dungeon.commands.GridCommands;
import com.mc.dungeon.commands.TileSetCommands;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;

/**
 * Hello world!
 *
 */
public class WFC extends JavaPlugin
{

    public CMDManager cmdManager = new CMDManager(this);

    public static WFC instance;
    public static Logger logger;


    public static WorldEditPlugin worldEditPlugin;

   @Override
    public void onEnable(){
        logger = this.getLogger();
        this.getServer().getLogger().log(Level.INFO, "Dungeon Generator enabled");

        worldEditPlugin = getWorldEdit();
        cmdManager.register(new FrameGeneration());
        cmdManager.register(new TileSetCommands());
        cmdManager.register(new GridCommands());
        instance = this;

        cmdManager.suggestWildcardNames(true);

        WFC.getInstatic().cmdManager.registerTabLookup("/wave tileset <tileset>", (args) -> { return List.copyOf(TileSet.SetNameToTileSet.keySet()); });
        WFC.getInstatic().cmdManager.registerTabLookup("/wave tileset <tileset> info <tilename>", (args) -> {
            TileSet ts = TileSet.SetNameToTileSet.get(args.getWildCard("tileset"));
            if(ts != null){
                return ts.getTileList().values().stream().map(e -> e.getName()).sorted().toList();
            }else{
                return List.of("<tilename>");
            }
        });

        WFC.getInstatic().cmdManager.registerTabLookup("/wave grid generate <tileset>", (args) -> {
            return List.copyOf(TileSet.SetNameToTileSet.keySet());
        });

        loadTilesets();
    }

    public WorldEditPlugin getWorldEdit(){
        Plugin p = this.getServer().getPluginManager().getPlugin("WorldEdit");
        if(p instanceof WorldEditPlugin we) return we;
        return null;
    }    

    public static WFC getInstatic(){
        return instance;
    }

    public void loadTilesets(){
        getLogger().log(Level.INFO, "Loading tilesets");
        File dataFolder = this.getDataFolder().toPath().resolve("TileSets").toFile();
        
        if(dataFolder.exists()) {
            for(File f : dataFolder.listFiles()){
                if(f.isDirectory()){
                    File config = f.toPath().resolve("Config.json").toFile();
                    
                    if(config.exists()){
                        try {
                            Reader reader = Files.newBufferedReader(config.toPath());
                            JsonObject jo = (JsonObject) JsonParser.parseReader(reader);
                            reader.close();
                            TileSet tileSet = new TileSet(jo.get("name").getAsString(), jo.get("size").getAsInt());

                            for(JsonElement jsonElem : jo.get("tiles").getAsJsonArray()){
                                String tileName = jsonElem.getAsJsonObject().get("name").getAsString();
                                Clipboard prefab = We7Wrapper.LoadSchematic(f.toPath().resolve(tileName + ".schem"));

                                String[][] sockets = new Gson().fromJson(jsonElem.getAsJsonObject().get("sockets"), new TypeToken<String[][]>(){}.getType());

                                Tile tile = new Tile(sockets[0], sockets[1], sockets[2],prefab,tileName, 0, tileSet.getTileSize(), false, true);
                                tileSet.add(tile);
                            }

                            TileSet.SetNameToTileSet.put(tileSet.getName(), tileSet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        getLogger().log(Level.INFO, "Tilesets loaded");
    }

}
