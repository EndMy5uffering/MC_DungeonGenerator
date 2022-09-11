package com.mc.dungeon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TileSet {
    
    public static Map<String, TileSet> SetNameToTileSet = new HashMap<>();
    
    private Map<String, Tile> tileList = new HashMap<>();

    private String name;

    private int tileSize;

    public TileSet(String name, int tileSize){
        this.name = name;
        this.tileSize = tileSize;
    }

    public boolean add(Tile tile){
        if(tile.getTileSize() != this.tileSize || this.tileList.containsKey(tile.getName())) return false;
        this.tileList.put(tile.getName(), tile);
        return true;
    }

    public Map<String, Tile> getTileList() {
        return this.tileList;
    }

    public Tile getTile(String name){
        return this.tileList.get(name);
    }

    public String getName() {
        return this.name;
    }

    public int getTileSize() {
        return this.tileSize;
    }

    public static TileSet get(String tileSetName){
        return TileSet.SetNameToTileSet.get(tileSetName);
    }

    public static boolean has(String tileSetName){
        return get(tileSetName) != null;
    }

    public static void add(TileSet tileset){
        TileSet.SetNameToTileSet.put(tileset.name, tileset);
    }

    public static TileSet remove(String tilesetName){
        return TileSet.SetNameToTileSet.remove(tilesetName);
    }

    public Tile[] toArray(){
        Collection<Tile> ks = this.tileList.values();
        return ks.toArray(new Tile[ks.size()]);
    }

}
