package com.mc.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

public class Tile {
    
    private static int FRONT= 0;
    private static int RIGHT = 1;
    private static int BACK = 2;
    private static int LEFT = 3;
    private static int TOP = 4;
    private static int BOTTOM = 5;

    private String[][] sockets; //[0][0-3] = TOP | [1][0-3] = MIDDLE | [2][0-3] = BOTTOM
    private Clipboard prefab;
    private int rotation = 0;
    private int tileSize = 0;
    private String fileName;
    private boolean isNewTile = false;
    private boolean existsAsSchematic = false;

    private boolean canBeRotated = true;
    private boolean canBeSpawned = true;

    public List<List<Integer>> validNeighbours = new ArrayList<>();


    public Tile(String[] socketsTop, String[] socketsMiddle, String[] socketsBottom, Clipboard prefab, String fileName, int rotation, int tileSize, boolean isNewTile, boolean existsAsSchematic) {
        this.sockets = new String[][]{socketsTop, socketsMiddle, socketsBottom};
        this.prefab = prefab;
        this.rotation = rotation;
        this.tileSize = tileSize;
        this.fileName = fileName;
        this.isNewTile = isNewTile;
        this.existsAsSchematic = existsAsSchematic;
    }

    private String getReverse(String in){
        return new StringBuilder(in).reverse().toString();
    }

    public void checkValidNeighbour(Tile[] tiles){
        for(int i = 0; i < 6; ++i){
            this.validNeighbours.add(new ArrayList<>());
        }

        for(int i = 0; i < tiles.length; ++i)
        {
            if(this.sockets[0][0].equals(getReverse(tiles[i].sockets[0][0])) &&
            this.sockets[0][1].equals(getReverse(tiles[i].sockets[0][1])) &&
            this.sockets[0][2].equals(getReverse(tiles[i].sockets[0][2])) &&
            this.sockets[0][3].equals(getReverse(tiles[i].sockets[0][3]))){
                this.validNeighbours.get(TOP).add(i);
            }
            if(this.sockets[2][0].equals(getReverse(tiles[i].sockets[2][0])) &&
            this.sockets[2][1].equals(getReverse(tiles[i].sockets[2][1])) &&
            this.sockets[2][2].equals(getReverse(tiles[i].sockets[2][2])) &&
            this.sockets[2][3].equals(getReverse(tiles[i].sockets[2][3]))){
                this.validNeighbours.get(BOTTOM).add(i);
            }
            if(this.sockets[1][0].equals(getReverse(tiles[i].sockets[1][0]))){
                this.validNeighbours.get(FRONT).add(i);
            }
            if(this.sockets[1][1].equals(getReverse(tiles[i].sockets[1][1]))){
                this.validNeighbours.get(RIGHT).add(i);
            }
            if(this.sockets[1][2].equals(getReverse(tiles[i].sockets[1][2]))){
                this.validNeighbours.get(BACK).add(i);
            }
            if(this.sockets[1][3].equals(getReverse(tiles[i].sockets[1][3]))){
                this.validNeighbours.get(LEFT).add(i);
            }
        }
    }

    public EditSession spawn(Grid g, GridCell gc, EditSession session) throws WorldEditException{

        BlockVector3 offset;

        switch (this.rotation) {
            case 0:
                offset = BlockVector3.ZERO;
                break;
            case 1:
                offset = BlockVector3.UNIT_X.multiply(this.tileSize);
                break;
            case 2:
                offset = BlockVector3.UNIT_X.add(BlockVector3.UNIT_Z).multiply(this.tileSize);
                break;
            case 3:
                offset = BlockVector3.UNIT_Z.multiply(this.tileSize);
                break;
            default:
                offset = BlockVector3.ZERO;
                break;
        }
        EditSession editSession = session;
        if(session == null) editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(g.location.getWorld()));

        ClipboardHolder holder = new ClipboardHolder(this.prefab);
                
        holder.setTransform(new AffineTransform().rotateY(90*rotation));
        Operation o = holder
        .createPaste(editSession)
        .to(BlockVector3
        .at(g.location.getX() + offset.getX() + (gc.x*gc.tileSize), g.location.getY() + offset.getY() + (gc.y*gc.tileSize), g.location.getZ() + offset.getZ() + (gc.z*gc.tileSize)))
        .build();
        Operations.complete(o);
        return editSession;
    }


    public String[][] getSockets() {
        return this.sockets;
    }

    public Clipboard getPrefab() {
        return this.prefab;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getTileSize() {
        return this.tileSize;
    }

    public String getName() {
        return this.fileName;
    }

    public List<List<Integer>> getValidNeighbours() {
        return this.validNeighbours;
    }

    public boolean isNewTile(){
        return this.isNewTile;
    }

    public void isNewTile(boolean isNew){
        this.isNewTile = isNew;
    }

    public boolean existsAsSchematic(){
        return this.existsAsSchematic;
    }

    public boolean canBeRotated(){
        return canBeRotated;
    }

    public void canBeRotated(boolean canRotate){
        this.canBeRotated = canRotate;
    }

    public boolean canBeSpawned(){
        return this.canBeSpawned;
    }

    public void canBeSpawned(boolean canSpawn){
        this.canBeSpawned = canSpawn;
    }

}
