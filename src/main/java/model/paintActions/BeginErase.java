package model.paintActions;

import model.Canvas;
import model.helpers.DrawUtil;

import java.awt.*;

public class BeginErase extends BeginStroke{

    public BeginErase(int x, int y, int t, int id) {
        super(x, y, t, 0, 0, 0, 0, id);
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getHeight()) return;

        Point[] points = DrawUtil.filledCircle(x,y,thickness);
        for(Point p : points){
            if( p.x < 0 || p.x > canvas.getWidth() || p.y < 0 || p.y > canvas.getHeight()) continue;
            canvas.setPixel(p.x,p.y,0,0,0,0);
        }
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof BeginErase);
    }
}
