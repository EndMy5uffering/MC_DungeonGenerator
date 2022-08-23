package com.mc.dungeon.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.easycommands.CMDListener;
import com.easycommands.commands.CMDArgs;
import com.easycommands.commands.CMDCommand;
import com.easycommands.commands.Permission;

import net.md_5.bungee.api.ChatColor;

public class FrameGeneration implements CMDListener{
    
    public static String commandForPlayerOnly = ChatColor.RED + "Command only for players";

    @CMDCommand(cmd = "/wave frame generate <d>")
    @Permission(Permissions = {"wave.command.genframe"})
    public boolean GenerateTileFrame(CMDArgs args){

        if(args.getSender() instanceof Player player){
            String dim = args.getWildCard("d");

            if(dim != null && dim.length() > 0) {
                try {
                    int dimentions = Integer.parseInt(dim);

                    if(dimentions < 5) {
                        player.sendMessage(ChatColor.RED + "Tile dimenstions have to be at least 5");
                        return true;
                    }

                    Location ploc = player.getLocation();
                    int px = ploc.getBlockX();
                    int py = ploc.getBlockY();
                    int pz = ploc.getBlockZ();

                    for(int i = 0; i <= dimentions+1; ++i){
                        player.getWorld().getBlockAt(px+i, py, pz).setType(Material.STONE);
                        player.getWorld().getBlockAt(px, py+i, pz).setType(Material.STONE);
                        player.getWorld().getBlockAt(px, py, pz+i).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+i, py+dimentions+1, pz+dimentions+1).setType(Material.STONE);
                        player.getWorld().getBlockAt(px, py+dimentions+1-i, pz+dimentions+1).setType(Material.STONE);
                        player.getWorld().getBlockAt(px, py+dimentions+1, pz+dimentions+1-i).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1-i, py, pz+dimentions+1).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1, py, pz+dimentions+1-i).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1, py+i, pz+dimentions+1).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1-i, py+dimentions+1, pz).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1, py+dimentions+1-i, pz).setType(Material.STONE);
                        player.getWorld().getBlockAt(px+dimentions+1, py+dimentions+1, pz+i).setType(Material.STONE);
                    }

                    player.getWorld().getBlockAt(px, py, pz).setType(Material.BLACK_CONCRETE);
                    player.getWorld().getBlockAt(px, py+dimentions+1, pz).setType(Material.CYAN_CONCRETE);
                    for(int i = 1; i <= dimentions/2; ++i){
                        player.getWorld().getBlockAt(px+i, py, pz).setType(Material.RED_CONCRETE);
                        player.getWorld().getBlockAt(px, py+i, pz).setType(Material.GREEN_CONCRETE);
                        player.getWorld().getBlockAt(px, py, pz+i).setType(Material.BLUE_CONCRETE);
                    }

                    for(int i = 1; i < dimentions-1; ++i){
                        player.getWorld().getBlockAt(px+1, py+dimentions+1, pz+i+1).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+i+1, py+dimentions+1, pz+1).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+dimentions-i, py+dimentions+1, pz+dimentions).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+dimentions, py+dimentions+1, pz+dimentions-i).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+1, py, pz+i+1).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+i+1, py, pz+1).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+dimentions-i, py, pz+dimentions).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+dimentions, py, pz+dimentions-i).setType(Material.WHITE_WOOL);
                    }

                    for(int i = 1; i <= dimentions; ++i){
                        player.getWorld().getBlockAt(px+i, py+dimentions, pz).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+i, py+dimentions, pz+dimentions+1).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px, py+dimentions, pz+i).setType(Material.WHITE_WOOL);
                        player.getWorld().getBlockAt(px+dimentions+1, py+dimentions, pz+i).setType(Material.WHITE_WOOL);
                    }

                } catch (NumberFormatException e) {
                    player.sendMessage(commandForPlayerOnly);
                }
            }else{
                
            }
        }else{
            args.getSender().sendMessage("Command only for players");
        }

        return true;
    }

}
