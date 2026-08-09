package model;

import java.awt.Color;
import java.util.Arrays;

public class CanvasImpl implements Canvas{

    private int id;
    private Color[][] colorArray;

    /**
     * Creates a 1000x1000 white canvas
     */
    public CanvasImpl(int id){
        this.id = id;

        Color[] row = new Color[1000];
        Arrays.fill(row, Color.white);
        Color[][] temp = new Color[1000][1000];
        Arrays.fill(temp,row);
        this.colorArray = temp;
    }

    /**
     * Creates a white canvas with the specified width and height
     */
    public CanvasImpl(int id, int x, int y){
        this.id = id;

        Color[] row = new Color[x];
        Arrays.fill(row, Color.white);
        Color[][] temp = new Color[y][x];
        Arrays.fill(temp,row);
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
    public int getRed(int x, int y) {
        return colorArray[y][x].getRed();
    }

    @Override
    public int getGreen(int x, int y) {
        return colorArray[y][x].getGreen();
    }

    @Override
    public int getBlue(int x, int y) {
        return colorArray[y][x].getBlue();
    }

    @Override
    public int getAlpha(int x, int y) {
        return colorArray[y][x].getAlpha();
    }

    @Override
    public Color getColor(int x, int y) {
        return colorArray[y][x];
    }

    @Override
    public void setPixel(int x, int y, int r, int g, int b, int a) {
        colorArray[y][x] = new Color(r,g,b,a);
    }
}
