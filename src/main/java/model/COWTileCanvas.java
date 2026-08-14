package model;

import model.constants.CanvasConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import model.Pair;
import model.CanvasTile;

public class COWTileCanvas implements Canvas{

    private int id;

    private int width;
    private int height;

    //original tile check, tiles
    private ArrayList<Pair<boolean[][],CanvasTile[][]>> tileLayers;

    //private ArrayList<Color[][]> colorLayers; //most recent layer is the visible layer


    private void initCanvas(int id, int w, int h){
        this.id = id;
        this.width = w;
        this.height = h;
        this.tileLayers = new ArrayList<>();

        boolean[][] boolLayer = new boolean[(h + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE][(w + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE];
        CanvasTile[][] tileLayer =
                new CanvasTile[(h + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE][(w + CanvasConstants.TILE_SIDE - 1)/CanvasConstants.TILE_SIDE];

        for(int ty=0; ty<h; ty+=CanvasConstants.TILE_SIDE){
            for(int tx=0; tx<w; tx+=CanvasConstants.TILE_SIDE){
                int tileWidth = Math.min(w-tx, CanvasConstants.TILE_SIDE);
                int tileHeight = Math.min(h-ty, CanvasConstants.TILE_SIDE);
                Color[][] tileColors = new Color[tileHeight][tileWidth];
                for(int y=0; y< tileHeight; y++){
                    for(int x=0; x<tileWidth;x++){
                        tileColors[y][x] = Color.white;
                    }
                }
                boolLayer[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE] = true;
                tileLayer[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE] = new CanvasTile(tileWidth, tileHeight, tileColors);
            }
        }

        this.tileLayers.add(new Pair<>(boolLayer,tileLayer));
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

    private int tileCoord(int c){
        return c/CanvasConstants.TILE_SIDE;
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
        int tileX = tileCoord(x);
        int tileY = tileCoord(y);
        return tileLayers.getLast().second()[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE];
    }

    @Override
    public void setPixel(int x, int y, int r, int g, int b, int a) {

        //oob check
        if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()){
            return;
        }

        int tileX = tileCoord(x);
        int tileY = tileCoord(y);

        Color tileColor = getColor(x,y);
        if(!tileColor.equals(new Color(r,g,b,a))){
            //if its not an original copy, make a new copy
            if(!tileLayers.getLast().first()[tileY][tileX]){
                //create its own array
                tileLayers.getLast().first()[tileY][tileX] = true;
                CanvasTile oldTile = tileLayers.getLast().second()[tileY][tileX];
                tileLayers.getLast().second()[tileY][tileX] = new CanvasTile(oldTile.width, oldTile.height, CanvasTile.copyColors(oldTile.colorArray));
            }
            tileLayers.getLast().second()[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE] = new Color(r,g,b,a);
        }

    }

    public int getNumLayers() {
        return tileLayers.size();
    }


    private Color[][] convertTileSetToColorArray(CanvasTile[][] tileSet){

        Color[][] tileColors = new Color[height][width];

        for(int ty=0; ty<height; ty+=CanvasConstants.TILE_SIDE){
            for(int tx=0; tx<width; tx+=CanvasConstants.TILE_SIDE){
                int tileWidth = Math.min(width-tx, CanvasConstants.TILE_SIDE);
                int tileHeight = Math.min(height-ty, CanvasConstants.TILE_SIDE);

                for(int y=0; y< tileHeight; y++){
                    for(int x=0; x<tileWidth;x++){
                        tileColors[ty + y][tx + x] = tileSet[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE].colorArray[y][x];
                    }
                }
            }
        }

        return tileColors;
    }

    public Color[][] getLayer(int layer){ return convertTileSetToColorArray(tileLayers.get(layer).second()); }

    public Color[][] getTop(){ return getLayer(tileLayers.size()-1); }


    public Pair<boolean[][],CanvasTile[][]> getLayerCopy(int layer){


        boolean[][] boolCopy = new boolean[tileLayers.get(layer).first().length][tileLayers.get(layer).first()[0].length];
        for(int i=0;i<tileLayers.get(layer).first().length;i++){
            for(int j=0;j<tileLayers.get(layer).first()[0].length;j++){
                boolCopy[i][j] = false;
            }
        }
        CanvasTile[][] canvasCopy = new CanvasTile[tileLayers.get(layer).second().length][];
        for(int i=0;i<tileLayers.get(layer).second().length;i++){
            canvasCopy[i] = Arrays.copyOf(tileLayers.get(layer).second()[i] , tileLayers.get(layer).second()[i].length);
        }

        return new Pair<>(boolCopy, canvasCopy);

    }

    public void copyTopLayer(){
        tileLayers.add(getLayerCopy(tileLayers.size()-1));
    }

    public void addLayer(Pair<boolean[][],CanvasTile[][]> layer){
        tileLayers.add(layer);
    }

    public void setLayer(Pair<boolean[][],CanvasTile[][]> layer){
        tileLayers.set(tileLayers.size()-1, layer);
    }



    public Color getColorLayer(int x, int y, int layer){
        int tileX = tileCoord(x);
        int tileY = tileCoord(y);
        return tileLayers.get(layer).second()[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE];
    }

    @Override
    public String toString(){
        StringBuilder canvasString = new StringBuilder();
        for(Color[] row : getTop()){
            for(Color pixel : row){
                String hex = String.format("#%02x%02x%02x", pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                canvasString.append("[").append(hex).append("]");
            }
            canvasString.append("\n");
        }
        return canvasString.toString();
    }

    //for debugging
    public ArrayList<Pair<boolean[][], CanvasTile[][]>> getTileLayers() {
        return tileLayers;
    }
}

