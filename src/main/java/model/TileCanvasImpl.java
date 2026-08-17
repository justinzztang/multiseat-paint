package model;

import model.constants.CanvasConstants;
import model.helpers.DrawUtil;

import java.awt.*;

public class TileCanvasImpl implements TiledCanvas{

    private int id;

    private int width;
    private int height;

    private CanvasTile[][] canvasTiles;

    private void initCanvas(int id, int w, int h){
        this.id = id;
        this.width = w;
        this.height = h;
        CanvasTile[][] temp =
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
                temp[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE] = new CanvasTile(tx/CanvasConstants.TILE_SIDE, ty/CanvasConstants.TILE_SIDE, tileWidth, tileHeight, tileColors);
            }
        }
        canvasTiles = temp;
    }

    /**
     * Creates a MAX_WIDTH x MAX_HEIGHT white canvas
     */
    public TileCanvasImpl(int id){
        this.initCanvas(id, CanvasConstants.MAX_WIDTH, CanvasConstants.MAX_HEIGHT);
    }

    /**
     * Creates a white canvas with the specified width and height
     */
    public TileCanvasImpl(int id, int w, int h){
        this.initCanvas(id, w, h);
    }

    @Override
    public CanvasTile getTile(int tileX, int tileY) {
        return canvasTiles[tileY][tileX];
    }

    @Override
    public void setTile(int tileX, int tileY, CanvasTile tile){
        canvasTiles[tileY][tileX] = tile;
    }

    @Override
    public int getId() {
        return this.id;
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
        int tileX = x/CanvasConstants.TILE_SIDE;
        int tileY = y/CanvasConstants.TILE_SIDE;
        return canvasTiles[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE];
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
        if(a==255){
            canvasTiles[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE] = new Color(r,g,b,a);
        }else{
            Color nc = DrawUtil.compositeOver(tileColor, new Color(r,g,b,a));
            canvasTiles[tileY][tileX].colorArray[y % CanvasConstants.TILE_SIDE][x % CanvasConstants.TILE_SIDE] = nc;
        }
    }

    @Override
    public TileCanvasImpl copy() {
        TileCanvasImpl temp = new TileCanvasImpl(this.id, this.width, this.height);
        for(int y = 0; y < canvasTiles.length; y++){
            for(int x = 0; x < canvasTiles[0].length; x++){
                temp.setTile(x, y, canvasTiles[y][x]);
            }
        }
        return temp;
    }

    @Override
    public Color[][] toColorArray(){

        Color[][] tileColors = new Color[height][width];

        for(int ty=0; ty<height; ty+=CanvasConstants.TILE_SIDE){
            for(int tx=0; tx<width; tx+=CanvasConstants.TILE_SIDE){
                int tileWidth = Math.min(width-tx, CanvasConstants.TILE_SIDE);
                int tileHeight = Math.min(height-ty, CanvasConstants.TILE_SIDE);

                for(int y=0; y< tileHeight; y++){
                    for(int x=0; x<tileWidth;x++){
                        tileColors[ty + y][tx + x] = canvasTiles[ty/CanvasConstants.TILE_SIDE][tx/CanvasConstants.TILE_SIDE].colorArray[y][x];
                    }
                }
            }
        }

        return tileColors;

    }
}
