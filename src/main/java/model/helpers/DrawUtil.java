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

    public static void floodFill(Color initialColor, int x, int y, Canvas canvas, ArrayList<Point> accumulator){

        boolean[][] visited = new boolean[canvas.getHeight()][canvas.getWidth()];

        Queue<Point> pointQueue = new LinkedList<>();
        pointQueue.add(new Point(x,y));
        visited[y][x] = true;

        while(!pointQueue.isEmpty()){
            Point p = pointQueue.poll();
            accumulator.add(p);

            int[] dxs = {0,0,1,-1};
            int[] dys = {-1,1,0,0};

            for(int i=0;i<4;i++){
                if(p.x+dxs[i] < 0 || p.x+dxs[i] >= canvas.getWidth() || p.y+dys[i] < 0 || p.y+dys[i] >= canvas.getHeight()) continue;
                if(!canvas.getColor(p.x+dxs[i],p.y+dys[i]).equals(initialColor)) continue;
                if(visited[p.y+dys[i]][p.x+dxs[i]]) continue;
                visited[p.y+dys[i]][p.x+dxs[i]] = true;
                pointQueue.add(new Point(p.x+dxs[i],p.y+dys[i]));
            }
        }
    }

}
