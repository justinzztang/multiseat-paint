package model;

import java.awt.*;

public interface Canvas {

    /** @return the id of the canvas*/
    int getId();

    /** @return the width of the canvas, in pixels*/
    int getWidth();

    /** @return the height of the canvas, in pixels*/
    int getHeight();

    /** @return the red value of a pixel, between 0 and 255 inclusive*/
    int getRed(int x, int y);

    /** @return the green value of a pixel, between 0 and 255 inclusive*/
    int getGreen(int x, int y);

    /** @return the blue value of a pixel, between 0 and 255 inclusive*/
    int getBlue(int x, int y);

    /** @return the alpha value of a pixel, between 0 and 255 inclusive*/
    int getAlpha(int x, int y);

    Color getColor(int x, int y);

    /** set the color of a pixel*/
    void setPixel(int x, int y, int r, int g, int b, int a);
}
