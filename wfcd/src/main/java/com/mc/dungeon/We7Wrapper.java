package com.mc.dungeon;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.Closer;

public class We7Wrapper {
    
    public static void SaveSchematic(File file, Clipboard clip) throws IOException{
        if(!file.exists()) file.createNewFile();
        Closer closer = Closer.create();
        FileOutputStream fileOutputStream = closer.register(new FileOutputStream(file));
        BufferedOutputStream bufferedOutputStream = closer.register(new BufferedOutputStream(fileOutputStream));
        ClipboardWriter writer = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(bufferedOutputStream));
        writer.write(clip);
        writer.close();
        bufferedOutputStream.close();
        fileOutputStream.close();
        closer.close();
    }

    
    public static Clipboard LoadSchematic(Path path) throws FileNotFoundException, IOException{
        return LoadSchematic(new File(path.toUri()));
    }

    public static Clipboard LoadSchematic(String path) throws FileNotFoundException, IOException{
        return LoadSchematic(new File(path));
    }

    public static Clipboard LoadSchematic(File file) throws FileNotFoundException, IOException{
        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        ClipboardReader reader = format.getReader(new FileInputStream(file));
        clipboard = reader.read();
        return clipboard;
    }

    public static Clipboard copyCurrentSelection(WorldEditPlugin wep, Player player) throws WorldEditException{
        LocalSession session = wep.getSession(player);
        com.sk89q.worldedit.world.World SessionWorld = session.getSelectionWorld();
        Region selection = session.getSelection(SessionWorld);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(selection);
        ForwardExtentCopy copyRegion = new ForwardExtentCopy(SessionWorld, selection, clipboard, selection.getMinimumPoint());
        Operations.complete(copyRegion);
        return clipboard;
    }

}
