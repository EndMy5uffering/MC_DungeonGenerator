package com.mc.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.easycommands.commands.CMDEventText;
import com.mc.dungeon.commands.GridCommands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class Grid {
    
    public enum Reason{
        WFC_No_Cell_Found,
        WFC_No_Fitting_Tile,
        WFC_Null_Entropy,
        WFC_OK
    }

    class WFCResult{
        private final Reason reason;
        private final GridCell cell;

        public WFCResult(Reason reason, GridCell cell){
            this.cell = cell;
            this.reason = reason;
        }

        public Reason getReason(){
            return this.reason;
        }

        public GridCell getCell(){
            return this.cell;
        }
    }

    public static HashMap<Player, Grid> playerToGeneratedGrids = new HashMap<Player, Grid>();

    public Location location;
    public GridCell[][][] cells;

    public Tile[] tiles;

    public int width, height, depth;
    public int tileSize = 0;
    private boolean isDone = false;
    private boolean interrupted = false;

    public Grid(Tile[] tiles, int width, int height, int depth, int tileSize){
        this.tiles = tiles;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.tileSize = tileSize;

        List<Tile> tempTiles = new ArrayList<Tile>();

        for(Tile t : tiles) {
            tempTiles.add(t);
            String[][] socks = t.getSockets();
            for (int i = 0; i < 3; ++i) 
            {
                tempTiles.add(new Tile(rotateSubArray(socks[0], 6-i+1), rotateSubArray(socks[1], 6-i+1), rotateSubArray(socks[2], 6-i+1), t.getPrefab(), t.getName(), i+1, t.getTileSize(), false, false));
            }
        }

        this.tiles = tempTiles.toArray(new Tile[0]);

        for(Tile t : this.tiles){
            Bukkit.getLogger().log(Level.INFO, t.toString());
            t.checkValidNeighbour(this.tiles);
        }

        this.cells = new GridCell[width][height][depth];

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    cells[x][y][z] = new GridCell(this, x, y, z, tileSize, null, -1, false, tiles.length);
                }
            }
        }

        recalcEntropyUncollapsed();

    }

    private String[] rotateSubArray(String[] a, int offset) {
        String[] result = new String[a.length];
        for(int i = 0; i < a.length; ++i){
            result[(i+offset)%a.length] = a[i];
        }
        return result;
    }

    public List<GridCell> getCellsWithLowestEntropy() {
        int en = this.tiles.length;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    if(this.cells[x][y][z].entropy < en && !this.cells[x][y][z].collapsed){
                        en = this.cells[x][y][z].entropy;
                    }
                }
            }
        }

        List<GridCell> result = new ArrayList<GridCell>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    if(this.cells[x][y][z].entropy == en && !this.cells[x][y][z].collapsed){
                        result.add(this.cells[x][y][z]);
                    }
                }
            }
        }
        return result;
    }

    private void recalcEntropyUncollapsed(){
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    if (!this.cells[x][y][z].collapsed) this.cells[x][y][z].recalcEntropy();
                }
            }
        }
    }

    public Runnable getRunnable(Player player){
        return () -> {
            player.sendMessage("Starting generation of dungeron. This could take a bit.");

            Stack<GridCell> stack = new Stack<>();

            GridCell nextTry = null;
            List<Integer> toExclude = new ArrayList<Integer>();
            while(!isDone() && !interrupted){
                WFCResult c = WFC(toExclude, nextTry);
                GridCell p;
                switch (c.reason) {
                    case WFC_OK:
                        stack.push(c.cell);
                        nextTry = null;
                        toExclude = new ArrayList<Integer>();
                        Bukkit.getLogger().log(Level.INFO, "OK: " + c.cell.tile.getName());
                        break;
                    case WFC_No_Fitting_Tile:
                            if(stack.isEmpty()) break;
                            if(!stack.isEmpty()) stack.pop().Reset();
                            recalcEntropyUncollapsed();
                            Bukkit.getLogger().log(Level.INFO, "WFC_No_Fitting_Tile");

                            break;
                    case WFC_Null_Entropy:
                        if(stack.isEmpty()) break;
                            Bukkit.getLogger().log(Level.INFO, "WFC_Null_Entropy");
                    case WFC_No_Cell_Found:
                        if(stack.isEmpty()) break;
                        if(!stack.isEmpty()) stack.pop().Reset();
                        if(!stack.isEmpty()) stack.pop().Reset();
                        recalcEntropyUncollapsed();
                        Bukkit.getLogger().log(Level.INFO, "WFC_No_Cell_Found");
                        break;
                    default:
                        Bukkit.getLogger().log(Level.INFO, "default " + c.reason);
                        interrupted = true;
                        break;
                }
            }
            if(!interrupted){
                Grid.playerToGeneratedGrids.put(player, this);
                CMDEventText.sendEventMessage(player, CMDEventText.getTextComponent(ChatColor.GREEN + "Dungeon was generated: "), CMDEventText.getInteractComponent(ChatColor.GOLD + "[SPAWN]", "/wave grid spawn", Action.RUN_COMMAND));
            }
            player.sendMessage("Task was interruped");
            GridCommands.playerToThreads.remove(player);
        };
    }

    public WFCResult WFC(List<Integer> exclude, GridCell nextTry) {
        GridCell pick = null;

        if (nextTry == null) {
            List<GridCell> lowest = getCellsWithLowestEntropy();
            int r_idx = new Random().nextInt(lowest.size());
            pick = lowest.get(r_idx);
        } else {
            pick = nextTry;        
        }

        if (pick == null || pick.collapsed || pick.entropy <= 0) {
            return new WFCResult(Reason.WFC_No_Cell_Found, pick);
        }
        
        int[] pickableValues = pick.values;

        if (exclude != null && exclude.size() > 0) {
            List<Integer> npickable = new ArrayList<Integer>();

            for (int i : pickableValues) {
                if (!exclude.contains(i)) npickable.add(i);
            }

            pickableValues = npickable.stream().mapToInt(Integer::intValue).toArray();
            if (npickable.size() == 0) return new WFCResult(Reason.WFC_No_Fitting_Tile, null);
        }
        int r_idx_tile = new Random().nextInt(pickableValues.length);
        
        Tile tile = this.tiles[pickableValues[r_idx_tile]];
        
        pick.Collapse(tile);

        recalcEntropyUncollapsed();

        List<GridCell> newLowest = getCellsWithLowestEntropy();
        if (newLowest != null && newLowest.size() > 0 && newLowest.get(0).entropy <= 0) {
            pick.Reset();
            recalcEntropyUncollapsed();
            return new WFCResult(Reason.WFC_Null_Entropy, null);
        }
        return new WFCResult(Reason.WFC_OK, pick);

    }

    public boolean isDone() {
        boolean done = true;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    done &= this.cells[x][y][z].collapsed;
                    if(!done) return done;
                }
            }
        }
        return done;
    }

    public void cancel(){
        this.interrupted = true;
    }

}
