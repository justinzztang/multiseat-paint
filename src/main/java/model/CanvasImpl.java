package model;

import model.constants.CanvasConstants;

import java.awt.Color;
import java.util.Arrays;

public class CanvasImpl implements Canvas{

    private int id;
    private Color[][] colorArray;

    /**
     * Creates a MAX_WIDTH x MAX_HEIGHT white canvas
     */
    public CanvasImpl(int id){
        this.id = id;

        Color[][] temp = new Color[CanvasConstants.MAX_HEIGHT][CanvasConstants.MAX_WIDTH];
        for(int y=0;y<CanvasConstants.MAX_HEIGHT;y++){
            for(int x=0;x<CanvasConstants.MAX_WIDTH;x++){
                temp[y][x] = Color.white;
            }
        }

        this.colorArray = temp;
    }

    /**
     * Creates a white canvas with the specified width and height
     */
    public CanvasImpl(int id, int x, int y){
        this.id = id;

        Color[][] temp = new Color[y][x];
        for(int yy=0;yy<y;yy++){
            for(int xx=0;xx<x;xx++){
                temp[yy][xx] = Color.white;
            }
        }
        this.colorArray = temp;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getWidth() {
        return colorArray[0].length;
    }

    @Override
    public int getHeight() {
        return colorArray.length;
    }

    @Override
    public Color getColor(int x, int y) {
        return colorArray[y][x];
    }

    @Override
    public void setPixel(int x, int y, int r, int g, int b, int a) {
        colorArray[y][x] = new Color(r,g,b,a);
    }

    public String printCanvas(){
        StringBuilder canvasString = new StringBuilder();
        for(Color[] row : colorArray){
            for(Color pixel : row){
                String hex = String.format("#%02x%02x%02x", pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                canvasString.append("[").append(hex).append("]");
            }
            canvasString.append("\n");
        }
        return canvasString.toString();
    }

}
