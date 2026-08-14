package model;

import java.awt.*;

public interface Canvas {

    /** @return the id of the canvas*/
    int getId();

    /** @return the width of the canvas, in pixels*/
    int getWidth();

    /** @return the height of the canvas, in pixels*/
    int getHeight();

    /** @return the color at the given pixel*/
    Color getColor(int x, int y);

    /** set the color of a pixel*/
    void setPixel(int x, int y, int r, int g, int b, int a);
}
