package com.mc.dungeon.commands;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.easycommands.CMDListener;
import com.easycommands.commands.CMDArgs;
import com.easycommands.commands.CMDCommand;
import com.easycommands.commands.CMDEventText;
import com.google.gson.Gson;
import com.mc.dungeon.Tile;
import com.mc.dungeon.TileSet;
import com.mc.dungeon.Util;
import com.mc.dungeon.WFC;
import com.mc.dungeon.We7Wrapper;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TileSetCommands implements CMDListener {
    
    public static Map<Material, String> woolToCharMap = new HashMap<>();
    static {
        woolToCharMap.put(Material.WHITE_WOOL, "-");
        woolToCharMap.put(Material.ORANGE_WOOL, "0");
        woolToCharMap.put(Material.MAGENTA_WOOL, "1");
        woolToCharMap.put(Material.LIGHT_BLUE_WOOL, "2");
        woolToCharMap.put(Material.YELLOW_WOOL, "3");
        woolToCharMap.put(Material.LIME_WOOL, "4");
        woolToCharMap.put(Material.PINK_WOOL, "5");
        woolToCharMap.put(Material.GRAY_WOOL, "6");
        woolToCharMap.put(Material.LIGHT_GRAY_WOOL, "7");
        woolToCharMap.put(Material.CYAN_WOOL, "8");
        woolToCharMap.put(Material.PURPLE_WOOL, "9");
        woolToCharMap.put(Material.BLUE_WOOL, "A");
        woolToCharMap.put(Material.BROWN_WOOL, "B");
        woolToCharMap.put(Material.BROWN_WOOL, "C");
        woolToCharMap.put(Material.GREEN_WOOL, "D");
        woolToCharMap.put(Material.RED_WOOL, "E");
        woolToCharMap.put(Material.BLACK_WOOL, "F");

    }

    @CMDCommand(cmd = "/wave tileset create <name> <size>")
    public boolean createTileSet(CMDArgs args) {

        String name = args.getWildCard("name");
        String size = args.getWildCard("size");

        if(name == null || name.equals("")) {
            args.getSender().sendMessage(ChatColor.RED + "Tile set name can not be empty!");
            args.getSender().sendMessage(ChatColor.RED + "/wave tileset create <name> <size>");
            return true;
        }
        if(size == null || size.equals("")) {
            args.getSender().sendMessage(ChatColor.RED + "Tile size can not be empty!");
            args.getSender().sendMessage(ChatColor.RED + "/wave tileset create <name> <size>");
            return true;
        }

        int tileSize;
        try {
            tileSize = Integer.parseInt(size);
        } catch (Exception e) {
            args.getSender().sendMessage(ChatColor.RED + "Tile size has to be a number!");
            return true;
        }

        if(TileSet.SetNameToTileSet.get(name) != null){
            args.getSender().sendMessage(ChatColor.RED + "A tileset with the given name already exists!");
            return true;
        }

        TileSet.SetNameToTileSet.put(name, new TileSet(name, tileSize));

        File tileSetFolder = new File(WFC.getInstatic().getDataFolder().toPath().resolve("TileSets").resolve(name).toUri());
        if(!tileSetFolder.exists())
            try {
                tileSetFolder.mkdirs();
                File configFile = new File(tileSetFolder.toPath().resolve("Config.json").toUri());
                configFile.createNewFile();
        
                Map<String, Object> tilesetInfo = new HashMap<>();
                tilesetInfo.put("name", name);
                tilesetInfo.put("size", tileSize);

                Gson gson = new Gson();
                Writer writer = Files.newBufferedWriter(configFile.toPath());
                gson.toJson(tilesetInfo, writer);
                writer.close();
            } catch (IOException e) {
                args.getSender().sendMessage(ChatColor.RED + "Could not create config file for tileset: " + name);
                return true;
            }
        args.getSender().sendMessage(ChatColor.GREEN + "Tileset (" + name + ", " + tileSize + ") created");
        return true;
    }

    @CMDCommand(cmd="/wave tileset <tileset> add <name>")
    public boolean addTileToTileset(CMDArgs args) {
        if(args.getSender() instanceof Player player){

            if(TileSet.has(args.getWildCard("tileset"))) {
                player.sendMessage(ChatColor.RED + "You have to specify for what tileset the tile should be saved!");
                return true;
            }

            if(!args.hasWildCard("name")) {
                player.sendMessage(ChatColor.RED + "You have to provide a name for the tile schematic!");
                return true;
            }

            try {
                World w = player.getLocation().getWorld();
                Clipboard clip = We7Wrapper.copyCurrentSelection(WFC.worldEditPlugin, player);
                BlockVector3 dim = clip.getDimensions();
                BlockVector3 min = clip.getMinimumPoint();
                int l = dim.getX();
                if(l != dim.getY() || l != dim.getZ()) {
                    player.sendMessage(ChatColor.RED + "The selection has to be a cube!");
                    return true;
                }
                l -= 1; //The dimension is 1 to large
                String[] UpperRingSockets = new String[] { getSocketString(min.add(l-2, l, l-1), BlockVector3.UNIT_MINUS_X, w, l-3),
                                                                                   getSocketString(min.add(1, l, l-2), BlockVector3.UNIT_MINUS_Z, w, l-3),
                                                                                   getSocketString(min.add(2, l, 1), BlockVector3.UNIT_X, w, l-3),
                                                                                   getSocketString(min.add(l-1, l, 2), BlockVector3.UNIT_Z, w, l-3) };
                
                String[] MiddleRingSockets = new String[]{ getSocketString(min.add(l-1, l-1, l), BlockVector3.UNIT_MINUS_X, w, l-1),
                                                                                   getSocketString(min.add(0, l-1, l-1), BlockVector3.UNIT_MINUS_Z, w, l-1),
                                                                                   getSocketString(min.add(1, l-1, 0), BlockVector3.UNIT_X, w, l-1),
                                                                                   getSocketString(min.add(l, l-1, 1), BlockVector3.UNIT_Z, w, l-1)};
                
                String[] LowerRingSockets = new String[]{ getSocketString(min.add(l-2, 0, l-1), BlockVector3.UNIT_MINUS_X, w, l-3),
                                                                                   getSocketString(min.add(1, 0, l-2), BlockVector3.UNIT_MINUS_Z, w, l-3),
                                                                                   getSocketString(min.add(2, 0, 1), BlockVector3.UNIT_X, w, l-3),
                                                                                   getSocketString(min.add(l-1, 0, 2), BlockVector3.UNIT_Z, w, l-3) };

                if(TileSet.SetNameToTileSet.get(args.getWildCard("tileset")).add(new Tile(UpperRingSockets, MiddleRingSockets, LowerRingSockets, clip, args.getWildCard("name"), 0, l-1, true, true))){
                    player.sendMessage(ChatColor.GREEN + "Tile (" + args.getWildCard("name") + ") was added to the tileset.");
                }else{
                    player.sendMessage(ChatColor.RED + "Unable to add tile to tileset.");
                }
            } catch (Exception e) {
                player.sendMessage(e.getMessage());
            }
        }
        return true;
    }

    @CMDCommand(cmd="/wave tileset <tileset> save")
    public boolean positionToSave(CMDArgs args) {
        if(args.getSender() instanceof Player player){

            if(!args.hasWildCard("tileset")) {
                player.sendMessage(ChatColor.RED + "You have to specify for what tileset the tile should be saved!");
                return true;
            }

            TileSet tSet = TileSet.SetNameToTileSet.get(args.getWildCard("tileset"));

            if(Util.saveTileSet(player, tSet))
                player.sendMessage(ChatColor.GREEN + "Tileset was saved!");    
        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset list")
    public boolean listTileSets(CMDArgs args){
        if(args.getSender() instanceof Player player){
            for(String key : TileSet.SetNameToTileSet.keySet()){
                TextComponent t0 = CMDEventText.getTextComponent(ChatColor.GOLD + "[" + key + ":" + TileSet.SetNameToTileSet.get(key).getTileSize() + "]");
                TextComponent t1 = CMDEventText.getInteractComponent(ChatColor.GREEN + " [Add]", "/wave tileset " + key + " add <name> ", Action.SUGGEST_COMMAND);
                TextComponent t2 = CMDEventText.getInteractComponent(ChatColor.AQUA + " [Info]", "/wave tileset " + key + " info", Action.RUN_COMMAND);
                TextComponent t3 = CMDEventText.getInteractComponent(ChatColor.LIGHT_PURPLE + " [Book]", "/wave tileset " + key + " book open", Action.RUN_COMMAND);
                TextComponent t4 = CMDEventText.getInteractComponent(ChatColor.BLUE + " [Save]", "/wave tileset " + key + " save", Action.RUN_COMMAND);
                TextComponent t5 = CMDEventText.getInteractComponent(ChatColor.RED + " [Delete]", "/wave tileset " + key + " delete", Action.RUN_COMMAND);
                CMDEventText.sendEventMessage(player, t0, t1, t2, t3, t4, t5);
            }
        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> info")
    public boolean tilesetInfo(CMDArgs args){
        if(args.getSender() instanceof Player player){

            String tileset = args.getWildCard("tileset");
            if(!args.hasWildCard("tileset")) {
                args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name");
                return true;
            }
            
            TileSet tset = TileSet.SetNameToTileSet.get(tileset);
            if(tset == null){
                args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + tileset);
                return true;
            }

            String topText = "-------[Tileset(" + tileset + ") info]-------";
            args.getSender().sendMessage(ChatColor.GOLD + topText);
            for(Tile tile : tset.getTileList().values()){
                TextComponent t0 = CMDEventText.getTextComponent(ChatColor.GOLD + "[" + tile.getName() + "]");
                TextComponent t1 = CMDEventText.getInteractComponent(ChatColor.BLUE + " [Sockets] ", "/wave tileset " + tileset + " info " + tile.getName() + " sockets", Action.RUN_COMMAND);
                TextComponent t2 = CMDEventText.getInteractComponent(ChatColor.LIGHT_PURPLE + " [Settings] ", "/wave tileset " + tileset + " info " + tile.getName() + " settings", Action.RUN_COMMAND);
                TextComponent t3 = CMDEventText.getInteractComponent(ChatColor.RED + "[Delete]", "/wave tileset " + tileset + " info " + tile.getName() + " delete", Action.RUN_COMMAND);
                CMDEventText.sendEventMessage(player, t0, t1, t2, t3);
            }
            args.getSender().sendMessage(ChatColor.GOLD + "-".repeat(topText.length()));
            

        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> info <tilename> sockets")
    public boolean tilesetInfoSockets(CMDArgs args){
        if(args.getSender() instanceof Player player){

            String tileset = args.getWildCard("tileset");
            if(tileset == null || tileset == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name!");
            
            String tilename = args.getWildCard("tilename");
            if(tilename == null || tilename == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide the name of a tile in the tileset!");
            
            TileSet tset = TileSet.SetNameToTileSet.get(tileset);
            if(tset == null){
                args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + tileset);
                return true;
            }

            Tile tile = tset.getTile(tilename);

            if(tile == null) {
                args.getSender().sendMessage(ChatColor.RED + "There is no tile with the given name (" + tilename + ") in the tileset " + tileset);
                return true;
            }

            String[][] sockets = tile.getSockets();
            String topText = "-------[Sockets (" + tilename + ")]-------";
            args.getSender().sendMessage(ChatColor.GOLD + topText);
            args.getSender().sendMessage(ChatColor.GOLD + "Top sockets: " + ChatColor.AQUA + "[" +  String.join(", ", sockets[0]) + "]");
            args.getSender().sendMessage(ChatColor.GOLD + "Mid sockets: " + ChatColor.AQUA + "[" +  String.join(", ", sockets[1]) + "]");
            args.getSender().sendMessage(ChatColor.GOLD + "Bot sockets: " + ChatColor.AQUA + "[" +  String.join(", ", sockets[2]) + "]");
            args.getSender().sendMessage(ChatColor.GOLD + "-".repeat(topText.length()));

        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> info <tilename> settings")
    public boolean tilesetInfoSettings(CMDArgs args){
        if(args.getSender() instanceof Player player){

            if(!args.hasWildCard("tileset") || !args.hasWildCard("tilename")){
                args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset and a tilename!");
                return true;
            }

            TileSet tset = TileSet.get(args.getWildCard("tileset"));
            if(tset == null){
                args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + args.getWildCard("tileset"));
                return true;
            }

            Tile tile = tset.getTile(args.getWildCard("tilename"));

            if(tile == null) {
                args.getSender().sendMessage(ChatColor.RED + "There is no tile with the given name (" + args.getWildCard("tilename") + ") in the tileset " + args.getWildCard("tileset"));
                return true;
            }

            String topText = "-------[Settings (" + tile.getName() + ")]-------";
            args.getSender().sendMessage(ChatColor.GOLD + topText);
            List<BaseComponent> b = getTileSettingsText(tset, tile);
            TextComponent[] settings = new TextComponent[b.size()];
            CMDEventText.sendEventMessage(player, b.toArray(settings));

        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> info <tilename> delete")
    public boolean tilesetInfoDelete(CMDArgs args){
        if(args.getSender() instanceof Player player){
            String tileset = args.getWildCard("tileset");
            if(tileset == null || tileset == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name!");
            
            String tilename = args.getWildCard("tilename");
            if(tilename == null || tilename == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide the name of a tile in the tileset!");
            
            TileSet tset = TileSet.SetNameToTileSet.get(tileset);
            if(tset == null){
                args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + tileset);
                return true;
            }

            TextComponent t0 = CMDEventText.getTextComponent(ChatColor.GOLD + "Are you sure you want to delete the tile (" + args.getWildCard("tilename") + ") from the tileset " + args.getWildCard("tileset"));
            TextComponent t1 = CMDEventText.getInteractComponent(ChatColor.GREEN + " [Confirm] ", "/wave tileset " + tileset + " info " + tilename + " delete confirm", Action.RUN_COMMAND);
            CMDEventText.sendEventMessage(player, t0, t1);

        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> info <tilename> delete confirm")
    public boolean tilesetInfoDeleteConfirm(CMDArgs args){
        if(args.getSender() instanceof Player player){

            String tileset = args.getWildCard("tileset");
            if(tileset == null || tileset == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name!");
            
            String tilename = args.getWildCard("tilename");
            if(tilename == null || tilename == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide the name of a tile in the tileset!");
            
            TileSet tset = TileSet.SetNameToTileSet.get(tileset);
            if(tset == null){
                args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + tileset);
                return true;
            }

            Tile tile = tset.getTile(tilename);

            if(tile == null) {
                args.getSender().sendMessage(ChatColor.RED + "There is no tile with the given name (" + tilename + ") in the tileset " + tileset);
                return true;
            }

            File schematic = WFC.getInstatic().getDataFolder().toPath().resolve("TileSets").resolve(tset.getName()).resolve(tile.getName() + ".schem").toFile();
            if(schematic.exists()){
                if(schematic.delete() && tset.getTileList().remove(tilename) != null && Util.saveTileSet(player, tset)){
                    args.getSender().sendMessage(ChatColor.GREEN + "Tile was deleted.");
                }else{
                    args.getSender().sendMessage(ChatColor.RED + "Unable to delete file.");
                }
            }else{
                args.getSender().sendMessage(ChatColor.RED + "There is no schematic file with the given name " + tilename);
            }

        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> delete confirm")
    public boolean deleteTilesetConfrim(CMDArgs args){
        String tileset = args.getWildCard("tileset");
        if(tileset == null || tileset == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name");
        
        TileSet tset = TileSet.SetNameToTileSet.get(tileset);
        if(tset == null){
            args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name: " + tileset);
            return true;
        }
        Path tilesetFolder = WFC.getInstatic().getDataFolder().toPath().resolve("TileSets").resolve(tset.getName());

        if(tilesetFolder.toFile().exists()){
            if(Util.DeleteFolder(tilesetFolder)){
                TileSet.SetNameToTileSet.remove(tileset);
                args.getSender().sendMessage(ChatColor.GREEN + "Deleted tileset " + tileset);
            }else{
                args.getSender().sendMessage(ChatColor.RED + "Could not delte tileset directory!");
            }
            
        }else{
            args.getSender().sendMessage(ChatColor.RED + "Could not find tileset folder!");
        }


        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> delete")
    public boolean deletetileset(CMDArgs args){
        if(args.getSender() instanceof Player player){
            String tileset = args.getWildCard("tileset");
            if(tileset == null || tileset == "") args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset name");
        
            TextComponent t0 = CMDEventText.getTextComponent(ChatColor.GOLD + "Are you sure you want to delete the tileset " + ChatColor.BLUE + args.getWildCard("tileset"));
            TextComponent t1 = CMDEventText.getInteractComponent(ChatColor.GREEN + " [CONFIRM]", "/wave tileset " + tileset + " delete confirm", Action.RUN_COMMAND);
            CMDEventText.sendEventMessage(player, t0, t1);
        }
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> tilesettings <tilename> canrotate <value>")
    public boolean tileSettingsCanRotate(CMDArgs args){
        if(!args.hasWildCard("tileset") || !args.hasWildCard("tilename") || !args.hasWildCard("value")){
            args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset and a tilename and a value!");
            return true;
        }

        if(!args.isWildCardBoolean("value")){
            args.getSender().sendMessage(ChatColor.RED + "The value has to be a boolean (ture, false)");
            return true;
        }

        TileSet tileSet = TileSet.get(args.getWildCard("tileset"));
        if(tileSet == null){
            args.getSender().sendMessage(ChatColor.RED + "There is no tileset " + args.getWildCard("tileset"));
            return true;
        }

        Tile t = tileSet.getTile(args.getWildCard("tilename"));
        if(t != null) {
            t.canBeRotated(args.getWildCardBoolean("value"));
            args.getSender().sendMessage(ChatColor.GREEN + "Tile setting 'Can Rotate' was set to " + ChatColor.GOLD + args.getWildCardBoolean("value") + ChatColor.GREEN + " for " + ChatColor.GOLD + t.getName());
        }else{
            args.getSender().sendMessage(ChatColor.RED + "There is no tile " + args.getWildCard("tilename"));
        }
        
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> tilesettings <tilename> canspawn <value>")
    public boolean tileSettingsCanSpawn(CMDArgs args){
        if(!args.hasWildCard("tileset") || !args.hasWildCard("tilename") || !args.hasWildCard("value")){
            args.getSender().sendMessage(ChatColor.RED + "You have to provide a tileset and a tilename and a value!");
            return true;
        }

        if(!args.isWildCardBoolean("value")){
            args.getSender().sendMessage(ChatColor.RED + "The value has to be a boolean (ture, false)");
            return true;
        }

        TileSet tileSet = TileSet.get(args.getWildCard("tileset"));
        if(tileSet == null){
            args.getSender().sendMessage(ChatColor.RED + "There is no tileset " + args.getWildCard("tileset"));
            return true;
        }

        Tile t = tileSet.getTile(args.getWildCard("tilename"));
        if(t != null) {
            t.canBeSpawned(args.getWildCardBoolean("value"));
            args.getSender().sendMessage(ChatColor.GREEN + "Tile setting 'Can be Spawned' was set to " + ChatColor.GOLD + args.getWildCardBoolean("value") + ChatColor.GREEN + " for " + ChatColor.GOLD + t.getName());
        }else{
            args.getSender().sendMessage(ChatColor.RED + "There is no tile " + args.getWildCard("tilename"));
        }
        
        return true;
    }

    @CMDCommand(cmd = "/wave tileset <tileset> book open")
    public boolean tilesetBook(CMDArgs args){
        try {
            if(args.getSender() instanceof Player player){
                String tilesetName = args.getWildCard("tileset");
                TileSet tileSet = TileSet.SetNameToTileSet.get(tilesetName);
                if(tileSet == null){
                    args.getSender().sendMessage(ChatColor.RED + "There is no tileset with the name " + tilesetName);
                    return true;
                }
                player.openBook(getBook(tileSet));
            }
        } catch (Exception e) {
            args.getSender().sendMessage(e.getMessage());
        }

        return true;
    }

    private String getSocketString(BlockVector3 startingPos, BlockVector3 direction, World world, int length){
        String result = "";
        for(int i = 0; i < length; ++i){
            BlockVector3 v = startingPos.add(direction.multiply(i));
            Block bs = world.getBlockAt(v.getX(), v.getY(), v.getZ());
            if(woolToCharMap.containsKey(bs.getType())){
                result += woolToCharMap.get(bs.getType());
            }
        }
        return result;
    }

    private List<BaseComponent> getTileSettingsText(TileSet tileSet, Tile tile){
        List<BaseComponent> result = new ArrayList<>();
        TextComponent canRotateText = CMDEventText.getTextComponent(ChatColor.GOLD + "Can Rotate ");
        canRotateText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Description...")));
        result.add(canRotateText);
        if(tile.canBeRotated()){
            result.add(CMDEventText.getInteractComponent(ChatColor.GREEN+"[True]\n", "/wave tileset " + tileSet.getName() + " tilesettings " + tile.getName() + " canrotate false", Action.RUN_COMMAND));
        }else{
            result.add(CMDEventText.getInteractComponent(ChatColor.RED+"[False]\n", "/wave tileset " + tileSet.getName() + " tilesettings " + tile.getName() + " canrotate true", Action.RUN_COMMAND));
        }
        TextComponent canSpawnText = CMDEventText.getTextComponent(ChatColor.GOLD + "Can Spawn ");
        canSpawnText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Description...")));
        result.add(canSpawnText);
        if(tile.canBeSpawned()){
            result.add(CMDEventText.getInteractComponent(ChatColor.GREEN+"[True]\n", "/wave tileset " + tileSet.getName() + " tilesettings " + tile.getName() + " canspawn false", Action.RUN_COMMAND));
        }else{
            result.add(CMDEventText.getInteractComponent(ChatColor.RED+"[False]\n", "/wave tileset " + tileSet.getName() + " tilesettings " + tile.getName() + " canspawn true", Action.RUN_COMMAND));
        }

        return result;
    }

    private ItemStack getBook(TileSet tileSet){

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        bookMeta.addPage("test");
        bookMeta.setTitle("Tileset book");
        bookMeta.setAuthor("Server");
        
        String topText = "---[" + tileSet.getName() + " info]---";
        List<BaseComponent[]> pages = new ArrayList<>();
        List<BaseComponent> page = new ArrayList<>();
        pages.add(new BaseComponent[]{
            CMDEventText.getTextComponent(ChatColor.GOLD + topText + "\n"),
            CMDEventText.getTextComponent(ChatColor.BLACK + "Size: " + tileSet.getTileSize() + "\n"),
            CMDEventText.getTextComponent(ChatColor.BLACK + "Tiles: " + tileSet.getTileList().size() + "\n"),
            CMDEventText.getInteractComponent(ChatColor.GREEN + "[Add]", "/wave tileset " + tileSet.getName() + " add <name> ", Action.SUGGEST_COMMAND),
            CMDEventText.getTextComponent(ChatColor.BLACK + "|"),
            CMDEventText.getInteractComponent(ChatColor.BLUE + "[Save]", "/wave tileset " + tileSet.getName() + " save", Action.RUN_COMMAND),
            CMDEventText.getTextComponent(ChatColor.BLACK + "|"),
            CMDEventText.getInteractComponent(ChatColor.RED + "[Del]\n", "/wave tileset " + tileSet.getName() + " delete", Action.RUN_COMMAND),
            CMDEventText.getInteractComponent(ChatColor.AQUA + "[Info(Chat)]", "/wave tileset " + tileSet.getName() + " info", Action.RUN_COMMAND)
        
        });
        for(Tile tile : tileSet.getTileList().values()){
            page = new ArrayList<>();

            TextComponent tileName = CMDEventText.getTextComponent(ChatColor.BLACK + (tile.getName().length() > 10 ? tile.getName().substring(0, 9) : tile.getName()) + ":\n\n");
            tileName.setUnderlined(true);
            page.add(tileName);
            
            for(BaseComponent c : getTileSettingsText(tileSet, tile)) page.add(c);

            page.add(CMDEventText.getTextComponent("\n"));
            page.add(CMDEventText.getInteractComponent(ChatColor.BLUE + "[Sockets]", "/wave tileset " + tileSet.getName() + " info " + tile.getName() + " sockets", Action.RUN_COMMAND));
            page.add(CMDEventText.getTextComponent(" "));
            page.add(CMDEventText.getInteractComponent(ChatColor.RED + "[Del]\n", "/wave tileset " + tileSet.getName() + " info " + tile.getName() + " delete", Action.RUN_COMMAND));

            pages.add(page.toArray(new BaseComponent[page.size()]));
        }

        bookMeta.spigot().setPages(pages);
        
        book.setItemMeta(bookMeta);
        return book;
    }

}
