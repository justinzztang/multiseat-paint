package model.paintActions;

import model.Canvas;
import model.helpers.BoundingBox;
import model.helpers.DrawUtil;

import java.awt.*;

/**
 * Action that indicates a user is drawing on the canvas,
 */
public class Draw implements PaintAction, Undoable {
    protected int prevX;
    protected int prevY;
    protected int x;
    protected int y;
    protected int thickness;
    protected int r;
    protected int g;
    protected int b;
    protected int a;
    protected int id;
    protected UndoStatus status = UndoStatus.DONE;

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
        if( prevX < 0 || prevX > canvas.getWidth() || prevY < 0 || prevY > canvas.getHeight()) return;
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getHeight()) return;
        //System.out.println("applied Draw");
        Point[] markedPoints = DrawUtil.bresenhamLine(prevX, prevY, x, y);
        for(Point p : markedPoints){
            Point[] points = DrawUtil.filledCircle(p.x,p.y,thickness);
            for(Point pp : points){
                if( pp.x < 0 || pp.x > canvas.getWidth() || pp.y < 0 || pp.y > canvas.getHeight()) continue;
                canvas.compositePixel(pp.x,pp.y,r,g,b,a);
            }
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

    public boolean canEqual(Object o){
        return (o instanceof Draw);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Draw bs){
            return bs.canEqual(this) && (prevX==bs.prevX
                    && prevY==bs.prevY
                    && x==bs.x
                    && y==bs.y
                    && thickness==bs.thickness
                    && r==bs.r
                    && g==bs.g
                    && b==bs.b
                    && a==bs.a
                    && id==bs.id
                    && status == bs.status);
        }
        return false;
    }
}
