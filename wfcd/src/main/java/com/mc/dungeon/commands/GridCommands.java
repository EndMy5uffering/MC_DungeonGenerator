package com.mc.dungeon.commands;

import org.bukkit.entity.Player;

import com.easycommands.CMDListener;
import com.easycommands.commands.CMDArgs;
import com.easycommands.commands.CMDCommand;
import com.mc.dungeon.Grid;
import com.mc.dungeon.TileSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;

import net.md_5.bungee.api.ChatColor;

public class GridCommands implements CMDListener{
    
    @CMDCommand(cmd = "wave grid generate <tileset> <width> <height> <depth>")
    public boolean generateGrid(CMDArgs args){

        if(args.getSender() instanceof Player player){
            TileSet tset = TileSet.get(args.getWildCard("tileset"));
            int width = args.getWildCardInt("width");
            int height = args.getWildCardInt("height");
            int depth = args.getWildCardInt("depth");

            if(tset == null){
                player.sendMessage(ChatColor.RED + "Tile set not found!");
                return true;
            }
            Grid g = null;
            try {
                g = new Grid(tset.toArray(), width, height, depth, tset.getTileSize());
            } catch (Exception e) {
                e.printStackTrace();   
            }
            player.sendMessage("Has grid: " + (g != null));
            Thread t = new Thread(g.getRunnable(player));
            t.start();
        }

        return true;
    }

    @CMDCommand(cmd = "wave grid spawn")
    public boolean spawnGrid(CMDArgs args){

        if(args.getSender() instanceof Player player){

            if(Grid.playerToGeneratedGrids.get(player) == null){
                player.sendMessage(ChatColor.RED+ "You have to generate a grid first!");
                return true;
            }

            Grid g = Grid.playerToGeneratedGrids.get(player);

            g.location = player.getLocation();
            for (int x = 0; x < g.width; ++x) {
                for (int y = 0; y < g.height; ++y) {
                    for (int z = 0; z < g.depth; ++z) {
                        try {
                            player.sendMessage("Spawning tile: " + g.cells[x][y][z].tile.getName());
                            g.cells[x][y][z].tile.spawn(g, g.cells[x][y][z], null).close();
                        } catch (WorldEditException e) {
                            player.sendMessage(e.getMessage());
                            return true;
                        }
                    }
                }
            }

        }

        return true;
    }

}
