package model;

import model.constants.CanvasConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class COWTileCanvas implements LayeredCanvas<TiledCanvas>{

    private int id;

    private int width;
    private int height;

    //original tile check, tiles
    private ArrayList<Pair<boolean[][],TiledCanvas>> tileLayers;

    private void initCanvas(int id, int w, int h){
        this.id = id;
        this.width = w;
        this.height = h;
        this.tileLayers = new ArrayList<>();
        boolean[][] boolLayer = new boolean[(h + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE][(w + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE];
        for(int ty=0; ty<h; ty+=CanvasConstants.TILE_SIDE){
            for(int tx=0; tx<w; tx+=CanvasConstants.TILE_SIDE){
                boolLayer[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE] = true;
            }
        }
        this.tileLayers.add(new Pair<>(boolLayer,new TileCanvasImpl(id,w,h)));
    }

    /**
     * Creates a MAX_WIDTH x MAX_HEIGHT white canvas
     */
    public COWTileCanvas(int id){
        this.initCanvas(id, CanvasConstants.MAX_WIDTH, CanvasConstants.MAX_HEIGHT);
    }

    /**
     * Creates a white canvas with the specified width and height
     */
    public COWTileCanvas(int id, int w, int h){
        this.initCanvas(id, w, h);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Color getColor(int x, int y) {
        return tileLayers.getLast().second().getColor(x, y);
    }

    @Override
    public void drawPixel(int x, int y, int r, int g, int b, int a) {
        //oob check
        if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()){
            return;
        }
        int tileX = x/CanvasConstants.TILE_SIDE;
        int tileY = y/CanvasConstants.TILE_SIDE;
        Color tileColor = getColor(x,y);
        if(!tileColor.equals(new Color(r,g,b,a)) || a!=255){ //caveat for transparency
            //if its not an original copy, make a new copy
            if(!tileLayers.getLast().first()[tileY][tileX]){
                //create its own array
                tileLayers.getLast().first()[tileY][tileX] = true;
                CanvasTile oldTile = tileLayers.getLast().second().getTile(tileX, tileY);
                CanvasTile newTile = new CanvasTile(oldTile.tileX, oldTile.tileY, oldTile.width, oldTile.height, CanvasTile.copyColors(oldTile.colorArray));
                tileLayers.getLast().second().setTile(tileX, tileY, newTile);
            }
            tileLayers.getLast().second().drawPixel(x,y,r,g,b,a);
        }
    }

    @Override
    public Canvas copy() {
        COWTileCanvas copy = new COWTileCanvas(id, width, height);
        copy.tileLayers.addAll(tileLayers);
        copy.deleteLayer(0); //get rid of the initial one
        return copy;
    }

    @Override
    public Color[][] toColorArray() {
        return getTop().toColorArray();
    }

    @Override
    public int getNumLayers() {
        return tileLayers.size();
    }

    @Override
    public void deleteLayer(int layer) {
        tileLayers.subList(layer,layer).clear();
    }

    @Override
    public void deleteLayers(int start, int end) {
        tileLayers.subList(start, end).clear();
    }


    @Override
    public TiledCanvas getLayer(int layer){ return tileLayers.get(layer).second(); }

    @Override
    public TiledCanvas getTop(){ return getLayer(tileLayers.size()-1); }

    @Override
    public void copyTopLayer(){
        boolean[][] boolCopy = new boolean[tileLayers.getLast().first().length][tileLayers.getLast().first()[0].length];
        tileLayers.add(new Pair<>(boolCopy, tileLayers.getLast().second().copy()));
    }

    @Override
    public void setTopLayer(TiledCanvas layer) {
        boolean[][] boolCopy = new boolean[tileLayers.getLast().first().length][tileLayers.getLast().first()[0].length];
        tileLayers.set(tileLayers.size()-1, new Pair<>(boolCopy,layer));
    }

    @Override
    public String toString(){
        StringBuilder canvasString = new StringBuilder();
        for(Color[] row : getTop().toColorArray()){
            for(Color pixel : row){
                String hex = String.format("#%02x%02x%02x", pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                canvasString.append("[").append(hex).append("]");
            }
            canvasString.append("\n");
        }
        return canvasString.toString();
    }

    //debug and testing method
    public ArrayList<Pair<boolean[][], TiledCanvas>> getTileLayers() {
        return tileLayers;
    }
}

