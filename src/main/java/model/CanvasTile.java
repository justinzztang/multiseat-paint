package model;

import java.awt.*;

public class CanvasTile{
    int width;
    int height;
    Color[][] colorArray;
    CanvasTile(int w, int h, Color[][] colorArray){
        this.width = w;
        this.height = h;
        assert(colorArray.length == h && colorArray[0].length == w);
        Color[][] temp = new Color[h][w];
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                temp[y][x] = colorArray[y][x];
            }
        }
        this.colorArray = temp;
    }

    static Color[][] copyColors(Color[][] ca){
        Color[][] temp = new Color[ca.length][ca[0].length];
        for(int y=0;y<ca.length;y++){
            for(int x=0;x<ca[0].length;x++){
                temp[y][x] = ca[y][x];
            }
        }
        return temp;
    }
}
