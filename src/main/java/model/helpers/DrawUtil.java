package model.helpers;

import java.awt.*;
import java.util.ArrayList;

public class DrawUtil {
    //https://en.wikipedia.org/wiki/Bresenham's_line_algorithm
    public static Point[] bresenhamLine(int x0, int y0, int x1, int y1){

        ArrayList<Point> points = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx - dy;

        while (true) {

            points.add(new Point(x0,y0));

            int e2 = 2 * error;

            if (x0 == x1 && y0 == y1) {
                break;
            }

            if (e2 > -dy) {
                error -= dy;
                x0 += sx;
            }

            if (e2 < dx) {
                error += dx;
                y0 += sy;
            }
        }

        return points.toArray(new Point[0]);
    }

    //https://stackoverflow.com/questions/7438263/alpha-compositing-algorithm-blend-modes
    public static Color compositeOver(Color bg, Color fg){

        int newAlpha = (bg.getAlpha()*255 + fg.getAlpha()*255 - bg.getAlpha() * fg.getAlpha())/255;

        int bgR = bg.getRed() * bg.getAlpha();
        int fgR = fg.getRed() * fg.getAlpha();
        int newR = fgR*255 + bgR * (255 - fg.getAlpha());
        newR = newR / 255 / newAlpha;

        int bgG = bg.getGreen() * bg.getAlpha();
        int fgG = fg.getGreen() * fg.getAlpha();
        int newG = fgG*255 + bgG * (255 - fg.getAlpha());
        newG = newG / 255 / newAlpha;

        int bgB = bg.getBlue() * bg.getAlpha();
        int fgB = fg.getBlue() * fg.getAlpha();
        int newB = fgB*255 + bgB * (255 - fg.getAlpha());
        newB = newB / 255 / newAlpha;

        return new Color(newR, newG, newB, newAlpha);
    }
}
