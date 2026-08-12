package model.paintActions;

import model.Canvas;
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
    private int id;
    private UndoStatus status = UndoStatus.DONE;

    public Draw(int prevX, int prevY, int x, int y, int id){
        this.prevX = prevX;
        this.prevY = prevY;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    @Override
    public int getUserID() {
        return id;
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        System.out.println("applied Draw");
        Point[] markedPoints = DrawUtil.bresenhamLine(prevX, prevY, x, y);
        for(Point p : markedPoints){
            canvas.setPixel(p.x,p.y,0,0,0,255); //TODO temporary values
        }
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
