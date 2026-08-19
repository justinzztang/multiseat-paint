package model.paintActions;

import model.COWTileCanvas;
import model.Canvas;
import model.helpers.BoundingBox;
import model.helpers.DrawUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Fill implements PaintAction, Undoable{

    protected int x;
    protected int y;
    protected int r;
    protected int g;
    protected int b;
    protected int a;
    protected int id;
    protected UndoStatus status = UndoStatus.DONE;

    protected int minX=x;
    protected int minY=y;
    protected int maxX=x;
    protected int maxY=y;

    public Fill(int x, int y, int r, int g, int b, int a, int id){
        this.x = x;
        this.y = y;
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

        ArrayList<Point> points = new ArrayList<>();
        DrawUtil.floodFill(canvas.getColor(x,y), x,y,canvas,points);

        for(Point p : points){
            minX = Math.min(minX,p.x);
            minY = Math.min(minY,p.y);
            maxX = Math.max(maxX,p.x);
            maxY = Math.max(maxY,p.y);
            canvas.setPixel(p.x,p.y,r,g,b,a);
        }

    }

    @Override
    public BoundingBox getBoundingBox() {
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
        return (o instanceof Fill);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Fill f){
            return (f.canEqual(this)) && (x==f.x
                    && y==f.y
                    && r==f.r
                    && g==f.g
                    && b==f.b
                    && a==f.a
                    && id==f.id
                    && status == f.status);
        }
        return false;
    }

}
