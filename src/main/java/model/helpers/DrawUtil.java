package model.helpers;

import java.awt.Point;
import java.awt.Color;
import java.util.*;

import model.Canvas;

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

    //https://en.wikipedia.org/wiki/Midpoint_circle_algorithm
    public static Point[] midpointCircle(int x, int y, int r){

        Set<Point> points = new HashSet<>();

        int t1 = r/16;
        int x1 = r;
        int y1 = 0;
        while(x1 >= y1){

            points.add(new Point(x1,y1));
            points.add(new Point(-x1,y1));
            points.add(new Point(x1,-y1));
            points.add(new Point(-x1,-y1));
            points.add(new Point(y1,x1));
            points.add(new Point(-y1,x1));
            points.add(new Point(y1,-x1));
            points.add(new Point(-y1,-x1));

            y1++;
            t1 += y1;
            int t2 = t1 - x1;
            if(t2>=0){
                t1 = t2;
                x1--;
            }

        }
        for (Point point : points) {
            point.x += x;
            point.y += y;
        }
        return points.toArray(new Point[0]);
    }

    //https://stackoverflow.com/questions/35801952/drawing-concentric-tiling-circles-with-even-diameter
    public static Point[] evenDiameterCircle(int x, int y, int r){

        Set<Point> points = new HashSet<>();

        int x1 = 1;
        int y1 = r;
        while(y1 >= x1){
            points.add(new Point(x1,y1));
            points.add(new Point(x1,-y1 + 1));
            points.add(new Point(-x1 + 1,y1));
            points.add(new Point(-x1 + 1,-y1 + 1));
            points.add(new Point(y1,x1));
            points.add(new Point(y1,-x1 + 1));
            points.add(new Point(-y1 + 1,x1));
            points.add(new Point(-y1 + 1,-x1 + 1));

            boolean test1 = (r*2 - 1)*(r*2 - 1) < (x1+1)*(x1+1)*4 + y1*y1*4 && (x1+1)*(x1+1)*4 + y1*y1*4 < (r*2 + 1)*(r*2 + 1);
            boolean test2 = (r*2 - 1)*(r*2 - 1) < (y1-1)*(y1-1)*4 + x1*x1*4 && (y1-1)*(y1-1)*4 + x1*x1*4 < (r*2 + 1)*(r*2 + 1);
            if(test1){
                x1++;
            }
            else if(test2){
                y1--;
            }
            else{
                x1++;
                y1--;
            }

        }
        for (Point point : points) {
            point.x += x-1; //ms paint style offset where your stroke moves up and to the left a bit
            point.y += y-1;
        }
        return points.toArray(new Point[0]);
    }

    public static Point[] filledCircle(int x, int y, int diameter){
        Set<Point> newPoints = new HashSet<>();
        Set<Point> points = diameter%2==1 ? new HashSet<>(Arrays.asList(midpointCircle(x,y,diameter/2))) : new HashSet<>(Arrays.asList(evenDiameterCircle(x,y,diameter/2)));
        for(Point p : points){
            int x0 = p.x;
            if(x0 > x){
                while(true){
                    x0--;
                    if(points.contains(new Point(x0,p.y))) break;
                    newPoints.add(new Point(x0,p.y));
                }
            }
            else if(x0 < x){
                while(true){
                    x0++;
                    if(points.contains(new Point(x0,p.y))) break;
                    newPoints.add(new Point(x0,p.y));
                }
            }
        }
        newPoints.addAll(points);
        return newPoints.toArray(new Point[0]);
    }

    public static Point[] thickCircle(int x, int y, int diameter){
        Set<Point> newPoints = new HashSet<>();
        Set<Point> points = diameter%2==1 ? new HashSet<>(Arrays.asList(midpointCircle(x,y,diameter/2))) : new HashSet<>(Arrays.asList(evenDiameterCircle(x,y,diameter/2)));
        for(Point p : points){
            int x0 = p.x;
            if(x0 > x){
                x0--;
                newPoints.add(new Point(x0,p.y));
            }
            else if(x0 < x){
                x0++;
                newPoints.add(new Point(x0,p.y));
            }
        }
        newPoints.addAll(points);
        return newPoints.toArray(new Point[0]);
    }

    //https://stackoverflow.com/questions/7438263/alpha-compositing-algorithm-blend-modes
    public static int compositeOver(int bg, int fg){

        int newAlpha = (EfficientColor.getAlpha(bg)*255 + EfficientColor.getAlpha(fg)*255 - EfficientColor.getAlpha(bg) * EfficientColor.getAlpha(fg))/255;

        int bgR = EfficientColor.getRed(bg) * EfficientColor.getAlpha(bg);
        int fgR = EfficientColor.getRed(fg) * EfficientColor.getAlpha(fg);
        int newR = fgR*255 + bgR * (255 - EfficientColor.getAlpha(fg));
        newR = newR / 255 / newAlpha;

        int bgG = EfficientColor.getGreen(bg) * EfficientColor.getAlpha(bg);
        int fgG = EfficientColor.getGreen(fg) * EfficientColor.getAlpha(fg);
        int newG = fgG*255 + bgG * (255 - EfficientColor.getAlpha(fg));
        newG = newG / 255 / newAlpha;

        int bgB = EfficientColor.getBlue(bg) * EfficientColor.getAlpha(bg);
        int fgB = EfficientColor.getBlue(fg) * EfficientColor.getAlpha(fg);
        int newB = fgB*255 + bgB * (255 - EfficientColor.getAlpha(fg));
        newB = newB / 255 / newAlpha;

        return EfficientColor.toColor(newR, newG, newB, newAlpha);
    }

    public static void floodFill(int initialColor, int x, int y, Canvas canvas, ArrayDeque<Integer> accumulator){

        //points are represented as ints, where x is the first 16 bits, and y is the last 16 bits

        boolean[][] visited = new boolean[canvas.getHeight()][canvas.getWidth()];

        Queue<Integer> pointQueue = new ArrayDeque<>();
        pointQueue.add( (x<<16) + y );
        visited[y][x] = true;

        while(!pointQueue.isEmpty()){
            int p = pointQueue.poll();
            accumulator.add(p);

            int[] dxs = {0,0,1,-1};
            int[] dys = {-1,1,0,0};

            int px = (p >>> 16) & 65535;
            int py = p & 65535;

            for(int i=0;i<4;i++){
                if(px+dxs[i] < 0 || px+dxs[i] >= canvas.getWidth() || py+dys[i] < 0 || py+dys[i] >= canvas.getHeight()) continue;
                if(canvas.getColor(px+dxs[i],py+dys[i]) != (initialColor)) continue;
                if(visited[py+dys[i]][px+dxs[i]]) continue;
                visited[py+dys[i]][px+dxs[i]] = true;
                pointQueue.add( ((px+dxs[i])<<16) + (py+dys[i]));
            }
        }
    }

    /** Utils for storing colors as 4 byte integers */
    public static class EfficientColor{

        /** First 8 bits are alpha, 2nd 8 are red, 3rd 8 are green, last 8 are blue, in accordance with awt.Color's format*/
        public static int toColor(int r, int g, int b, int a){
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public static int getAlpha(int color){
            return color >>> 24;
        }
        public static int getRed(int color){
            return (color >>> 16) & 0xff;
        }
        public static int getGreen(int color){
            return (color >>> 8) & 0xff;
        }
        public static int getBlue(int color){
            return color & 0xff;
        }
    }



}
