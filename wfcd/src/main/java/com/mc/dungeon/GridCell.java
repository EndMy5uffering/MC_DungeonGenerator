package com.mc.dungeon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.WorldEditException;

public class GridCell {

    public int x, y, z; // cords in tilegrid
    public float tileSize;
    public Tile tile;
    public Grid grid;
    public int tileIdx = -1;
    public boolean collapsed;
    public int entropy;
    public int[] values;

    public int _FRONT= 0, _LEFT = 1, _RIGHT = 2, _BACK = 3, _TOP = 4, _BOTTOM = 5;

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
        List<List<Integer>> neighbours = new ArrayList<>();
        for(GridCell e : c){
            List<Integer> n = this.getValidNeighboursList(e);
            if(n != null) neighbours.add(n);
        }


        List<Integer> validTiles = new ArrayList<Integer>();
        for(int j = 0; j < grid.tiles.length; ++j){
            boolean jvalid = true;
            for(int i = 0; i < neighbours.size(); ++i){
                jvalid &= neighbours.get(i).contains(j);
            }
            if(jvalid) validTiles.add(j);
        }

        this.values = validTiles.stream().mapToInt(Integer::intValue).toArray();
        this.entropy = validTiles.size();

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
        if(Other.collapsed && Other.x > this.x) return Other.tile.validNeighbours.get(_LEFT);
        if(Other.collapsed && Other.x < this.x) return Other.tile.validNeighbours.get(_RIGHT);
        if(Other.collapsed && Other.y > this.y) return Other.tile.validNeighbours.get(_BOTTOM);
        if(Other.collapsed && Other.y < this.y) return Other.tile.validNeighbours.get(_TOP);
        if(Other.collapsed && Other.z > this.z) return Other.tile.validNeighbours.get(_BACK);
        if(Other.collapsed && Other.z < this.z) return Other.tile.validNeighbours.get(_FRONT);
        return null;
    }

}
