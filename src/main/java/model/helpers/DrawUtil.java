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
}
