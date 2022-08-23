package com.mc.dungeon;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

public class Util {
    
    public class Pair<T,K>{
        T first;
        K secound;
        
        public Pair(T t, K k){
            this.first = t;
            this.secound = k;
        }

        public T getFirst(){
            return first;
        }
        
        public void setFirst(T t){
            this.first = t;
        }

        public K getSecound(){
            return secound;
        }

        public void setSecound(K k){
            this.secound = k;
        }
    }

    public static Util.Pair<List<Path>,List<Path>> getDirsAndFiles(Path parent){

        List<Path> dirs = new ArrayList<>();
        List<Path> files = new ArrayList<>();

        for(File f : parent.toFile().listFiles()){
            if(f.isFile()){
                files.add(f.toPath());
            }else if(f.isDirectory()){
                Pair<List<Path>, List<Path>> contains = getDirsAndFiles(f.toPath());
                for(Path p : contains.getFirst()) dirs.add(p);
                for(Path p : contains.getSecound()) files.add(p);
                dirs.add(f.toPath());
            }
        }

        return (new Util()).new Pair<List<Path>,List<Path>>(dirs, files);
    }

    public static boolean DeleteFolder(Path folder){
        Pair<List<Path>, List<Path>> paths = getDirsAndFiles(folder);

        for(Path file : paths.getSecound())
            if(!file.toFile().delete()) return false;

        for(Path dir : paths.getFirst())
            if(!dir.toFile().delete()) return false;

        return folder.toFile().delete();
    }

    public static <T> T safeCast(Object o, Class<T> clazz) {
		if(clazz == null)
			throw new ClassCastException("Can not cast object to nullpointer!");
	    return clazz.isInstance(o) ? clazz.cast(o) : null;
	}

    public static boolean saveTileSet(Player player, TileSet tileSet){
        HashMap<String, Object> config = new HashMap<>();
            config.put("name", tileSet.getName());
            config.put("size", tileSet.getTileSize());
            
            List<HashMap<String, Object>> tilesConfig = new ArrayList<>();
            for(Tile t : tileSet.getTileList().values()){
                HashMap<String, Object> singleTileConfig = new HashMap<>();
                try{
                    if(t.isNewTile()){
                        File file = new File(WFC.getInstatic().getDataFolder().toPath().resolve("TileSets").resolve(tileSet.getName()).resolve(t.getName() + ".schem").toUri());
                        We7Wrapper.SaveSchematic(file, t.getPrefab());
                        t.isNewTile(false);
                    }
                    singleTileConfig.put("name", t.getName());
                    singleTileConfig.put("sockets", t.getSockets());
                    singleTileConfig.put("rotation", t.getRotation());
                    tilesConfig.add(singleTileConfig);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Ther was an error while saving tiles of tileset " + tileSet.getName());
                    return false;
                }
            }
            config.put("tiles", tilesConfig.toArray());

            try {
                Writer writer = Files.newBufferedWriter(WFC.getInstatic().getDataFolder().toPath().resolve("TileSets").resolve(tileSet.getName()).resolve("Config.json"));
                Gson gson = new Gson();
                gson.toJson(config, writer);
                writer.close();
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Ther was an error while writing the config");
                e.printStackTrace();
                return false;
            }
            return true;
    }

}
