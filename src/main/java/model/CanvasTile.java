package model;

import java.awt.*;

public class CanvasTile{
    public int tileX;
    public int tileY;
    public int width;
    public int height;
    public int[][] colorArray;
    CanvasTile(int tileX, int tileY, int w, int h, int[][] colorArray){
        this.tileX = tileX;
        this.tileY = tileY;
        this.width = w;
        this.height = h;
        assert(colorArray.length == h && colorArray[0].length == w);
        int[][] temp = new int[h][w];
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                temp[y][x] = colorArray[y][x];
            }
        }
        this.colorArray = temp;
    }

    static int[][] copyColors(int[][] ca){
        int[][] temp = new int[ca.length][ca[0].length];
        for(int y=0;y<ca.length;y++){
            for(int x=0;x<ca[0].length;x++){
                temp[y][x] = ca[y][x];
            }
        }
        return temp;
    }
}
