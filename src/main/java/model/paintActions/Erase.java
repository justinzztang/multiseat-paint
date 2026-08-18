package model.paintActions;

import model.Canvas;
import model.helpers.DrawUtil;

import java.awt.*;

public class Erase extends Draw{

    public Erase(int prevX, int prevY, int x, int y, int t, int id) {
        super(prevX, prevY, x, y, t, 0, 0, 0, 0, id);
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        if( prevX < 0 || prevX > canvas.getWidth() || prevY < 0 || prevY > canvas.getHeight()) return;
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getHeight()) return;
        //System.out.println("applied Draw");
        Point[] markedPoints = DrawUtil.bresenhamLine(prevX, prevY, x, y);
        for(Point p : markedPoints){
            Point[] points = DrawUtil.thickCircle(p.x,p.y,thickness);
            for(Point pp : points){
                if( pp.x < 0 || pp.x > canvas.getWidth() || pp.y < 0 || pp.y > canvas.getHeight()) continue;
                canvas.setPixel(pp.x,pp.y,0,0,0,0);
            }
        }

    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof Erase);
    }

}
