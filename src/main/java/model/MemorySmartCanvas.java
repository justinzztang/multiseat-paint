package model;

import model.constants.CanvasConstants;

import java.awt.*;
import java.util.ArrayList;

public class MemorySmartCanvas implements Canvas{

    private int id;
    private ArrayList<Color[][]> colorLayers; //most recent layer is the visible layer

    /**
     * Creates a MAX_WIDTH x MAX_HEIGHT white canvas
     */
    public MemorySmartCanvas(int id){
        this.id = id;
        this.colorLayers = new ArrayList<>();

        Color[][] temp = new Color[CanvasConstants.MAX_HEIGHT][CanvasConstants.MAX_WIDTH];
        for(int y=0;y<CanvasConstants.MAX_HEIGHT;y++){
            for(int x=0;x<CanvasConstants.MAX_WIDTH;x++){
                temp[y][x] = Color.white;
            }
        }

        this.colorLayers.add(temp);
    }

    /**
     * Creates a white canvas with the specified width and height
     */
    public MemorySmartCanvas(int id, int x, int y){
        this.id = id;
        this.colorLayers = new ArrayList<>();

        Color[][] temp = new Color[y][x];
        for(int yy=0;yy<y;yy++){
            for(int xx=0;xx<x;xx++){
                temp[yy][xx] = Color.white;
            }
        }
        this.colorLayers.add(temp);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getWidth() {
        return colorLayers.getFirst()[0].length;
    }

    @Override
    public int getHeight() {
        return colorLayers.getFirst().length;
    }

    public int getNumLayers() {
        return colorLayers.size();
    }

    @Override
    public Color getColor(int x, int y) {
        return colorLayers.getLast()[y][x];
    }

    @Override
    public void setPixel(int x, int y, int r, int g, int b, int a) {

        if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()){
            return;
        }

        //only set to the color if the color is different, otherwise keep it as the reference, this will save memory
        if(!colorLayers.getLast()[y][x].equals(new Color(r,g,b,a))){
            colorLayers.getLast()[y][x] = new Color(r,g,b,a);
        }
    }


    public Color[][] getLayer(int layer){ return colorLayers.get(layer); }

    public Color[][] getTop(){ return colorLayers.get(getNumLayers()-1); }


    public Color[][] getLayerCopy(int layer){
        Color[][] temp = new Color[getHeight()][getWidth()];
        for(int y=0;y<getHeight();y++){
            for(int x=0;x<getWidth();x++){
                temp[y][x] = colorLayers.get(layer)[y][x]; //layer copy only has references
            }
        }
        return temp;
    }

    public void copyLayer(){
        colorLayers.add(getLayerCopy(colorLayers.size()-1));
    }

    public void addLayer(Color[][] layer){
        colorLayers.add(layer);
    }

    public void setLayer(Color[][] layer){
        colorLayers.set(colorLayers.size()-1, layer);
    }



    public Color getColorLayer(int x, int y, int layer){
        return colorLayers.get(layer)[y][x];
    }

    public String printCanvas(){
        StringBuilder canvasString = new StringBuilder();
        for(Color[] row : colorLayers.getLast()){
            for(Color pixel : row){
                String hex = String.format("#%02x%02x%02x", pixel.getRed(), pixel.getGreen(), pixel.getBlue());
                canvasString.append("[").append(hex).append("]");
            }
            canvasString.append("\n");
        }
        return canvasString.toString();
    }

}
