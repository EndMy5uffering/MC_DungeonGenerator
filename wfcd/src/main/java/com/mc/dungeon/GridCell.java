package com.mc.dungeon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.google.common.collect.Sets;

public class GridCell {

    public int x, y, z; // cords in tilegrid
    public float tileSize;
    public Tile tile;
    public Grid grid;
    public int tileIdx = -1;
    public boolean collapsed;
    public int entropy;
    public int[] values;

    public GridCell(Grid grid, int x, int y, int z, float tileSize, Tile tile, int tileIdx, boolean collapsed, int init_entropy) {
        this.tile = tile;
        this.grid = grid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.collapsed = collapsed;
        this.tileSize = tileSize;
        this.entropy = init_entropy;
        this.tileIdx = tileIdx;
    }

    public void Collapse(Tile tile){
        this.collapsed = true;
        this.entropy = -1;
        this.tile = tile;
    }

    public void Reset(){
        this.collapsed = false;
        recalcEntropy();
        this.tile = null;
        tileIdx = -1;
    }

    public void recalcEntropy(){
        if(this.collapsed) return;

        List<GridCell> c = getNeighbours();
        Set<Set<Integer>> neighbours = new HashSet<>();
        for(GridCell e : c){
            List<Integer> n = this.getValidNeighboursList(e);
            if(n != null) {
                neighbours.add(Sets.newHashSet(n));
            }

        }

        List<Integer> validTiles = new ArrayList<Integer>();
        for(int j = 0; j < grid.tiles.length; ++j){
            boolean jvalid = true;
            for(Set<Integer> s : neighbours){
                jvalid &= s.contains(j);
            }
            if(jvalid) validTiles.add(j);
        }

        this.values = validTiles.stream().mapToInt(Integer::intValue).toArray();
        this.entropy = validTiles.size();

        /*if(!this.collapsed){
            Bukkit.getLogger().log(Level.INFO, "##############################################");
            Bukkit.getLogger().log(Level.INFO, "TILES: \n" + String.join("\n", List.of(grid.tiles).stream().map(x -> x.toString()).toList()));
            Bukkit.getLogger().log(Level.INFO, "-------------------\n Values: " + String.join(",", validTiles.stream().map(x -> x+"").toList()));
            Bukkit.getLogger().log(Level.INFO, "-------------------\n NList: [" + neighbours.stream().map(x -> "[" + x.stream().map(y -> y+"").reduce("", (s, e) -> s+", "+e) + "]").reduce("", (s,e) -> s + ", " + e)  + "]");
        }*/

    }

    public List<GridCell> getNeighbours(){
        List<GridCell> result = new ArrayList<GridCell>();
        if(x+1 < this.grid.width && this.grid.cells[x+1][y][z] != null) result.add(this.grid.cells[x+1][y][z]);
        if(x > 0 && this.grid.cells[x-1][y][z] != null) result.add(this.grid.cells[x-1][y][z]);
        if(y+1 < this.grid.height && this.grid.cells[x][y+1][z] != null) result.add(this.grid.cells[x][y+1][z]);
        if(y > 0 && this.grid.cells[x][y-1][z] != null) result.add(this.grid.cells[x][y-1][z]);
        if(z+1 < this.grid.depth && this.grid.cells[x][y][z+1] != null) result.add(this.grid.cells[x][y][z+1]);
        if(z > 0 && this.grid.cells[x][y][z-1] != null) result.add(this.grid.cells[x][y][z-1]);
        return result;
    }

    public List<Integer> getValidNeighboursList(GridCell Other){
        if(Other.collapsed && Other.x > this.x) return Other.tile.validNeighbours.get(Tile.RIGHT);
        if(Other.collapsed && Other.x < this.x) return Other.tile.validNeighbours.get(Tile.LEFT);
        if(Other.collapsed && Other.y > this.y) return Other.tile.validNeighbours.get(Tile.BOTTOM);
        if(Other.collapsed && Other.y < this.y) return Other.tile.validNeighbours.get(Tile.TOP);
        if(Other.collapsed && Other.z > this.z) return Other.tile.validNeighbours.get(Tile.BACK);
        if(Other.collapsed && Other.z < this.z) return Other.tile.validNeighbours.get(Tile.FRONT);
        return null;
    }

}
