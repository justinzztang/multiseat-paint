package model.paintActions;

import model.Canvas;
import model.helpers.BoundingBox;
import model.helpers.DrawUtil;

import java.awt.*;

/**
 * Action that indicates a user has begun a stroke, by clicking down with the paint tool
 */
public class BeginStroke implements PaintAction, Undoable {
    protected int x;
    protected int y;
    protected int thickness;
    protected int r;
    protected int g;
    protected int b;
    protected int a;
    protected int id;
    protected UndoStatus status = UndoStatus.DONE;

    public BeginStroke(int x, int y, int t, int r, int g, int b, int a, int id){
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
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getHeight()) return;

        Point[] points = DrawUtil.filledCircle(x,y,thickness);
        for(Point p : points){
            if( p.x < 0 || p.x > canvas.getWidth() || p.y < 0 || p.y > canvas.getHeight()) continue;
            canvas.compositePixel(p.x,p.y,r,g,b,a);
        }
        //System.out.println("applied BeginStroke");
    }

    @Override
    public BoundingBox getBoundingBox() {
        int minX = x - thickness/2;
        int minY = y - thickness/2;
        int maxX = x + (thickness-1)/2;
        int maxY = y + (thickness-1)/2;

        return new BoundingBox(minX, minY, maxX, maxY);
    }

    @Override
    public PointType getPointType() {
        return PointType.UNDOPOINT;
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
        return (o instanceof BeginStroke);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof BeginStroke bs){
            return (bs.canEqual(this)) && (x==bs.x
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
