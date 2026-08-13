package model.paintActions;

import model.Canvas;
import model.helpers.BoundingBox;
import model.helpers.DrawUtil;

import java.awt.*;

/**
 * Action that indicates a user is drawing on the canvas,
 */
public class Draw implements PaintAction, Undoable {
    private int prevX;
    private int prevY;
    private int x;
    private int y;
    private int thickness;
    private int r;
    private int g;
    private int b;
    private int a;
    private int id;
    private UndoStatus status = UndoStatus.DONE;

    public Draw(int prevX, int prevY, int x, int y, int t, int r, int g, int b, int a, int id){
        this.prevX = prevX;
        this.prevY = prevY;
        this.x = x;
        this.y = y;
        this.thickness = t;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.id = id;
    }

    @Override
    public int getUserID() {
        return id;
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        if( prevX < 0 || prevX > canvas.getWidth() || prevY < 0 || prevY > canvas.getWidth()) return;
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getWidth()) return;
        //System.out.println("applied Draw");
        Point[] markedPoints = DrawUtil.bresenhamLine(prevX, prevY, x, y);
        for(Point p : markedPoints){
            if( p.x < 0 || p.x > canvas.getWidth() || p.y < 0 || p.y > canvas.getWidth()) continue;
            canvas.setPixel(p.x,p.y,r,g,b,a);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        int minX = Math.min(x,prevX) - thickness/2;
        int minY = Math.min(y,prevY) - thickness/2;
        int maxX = Math.max(x,prevX) + (thickness-1)/2;
        int maxY = Math.max(y,prevY) + (thickness-1)/2;

        return new BoundingBox(minX, minY, maxX, maxY);
    }

    @Override
    public PointType getPointType() {
        return PointType.INBETWEEN;
    }

    @Override
    public void setUndoStatus(UndoStatus status) {
        this.status = status;
    }

    @Override
    public UndoStatus getUndoStatus() {
        return status;
    }
}
