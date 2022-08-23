package com.mc.dungeon;

import org.bukkit.Location;

public class Grid {
    
    public Location location;
    public GridCell[][][] cells;

    public Tile[] tiles;

    public int width, height, depth;
    public int tileSize = 0;

}
