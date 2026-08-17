package model;

import java.awt.*;

/**
 * A canvas storing color data that can be read and written to.
 */

public interface Canvas {

    /** @return the id of the canvas*/
    int getId();

    /** @return the width of the canvas, in pixels*/
    int getWidth();

    /** @return the height of the canvas, in pixels*/
    int getHeight();

    /** @return the color at the provided coordinates */
    Color getColor(int x, int y);

    /** Set the RGBA color of a pixel at the provided coordinates */
    void drawPixel(int x, int y, int r, int g, int b, int a);

    /** @return a shallow copy of the canvas object */
    Canvas copy();

    /** @return a 2d array of Colors stored by the canvas */
    Color[][] toColorArray();
}
