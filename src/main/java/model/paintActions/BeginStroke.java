package model.paintActions;

import model.Canvas;
import model.constants.CanvasConstants;
import model.helpers.BoundingBox;

import java.awt.*;

/**
 * Action that indicates a user has begun a stroke, by clicking down with the paint tool
 */
public class BeginStroke implements PaintAction, Undoable {
    private int x;
    private int y;
    private int thickness;
    private int id;
    private UndoStatus status = UndoStatus.DONE;

    public BeginStroke(int x, int y, int t, int id){
        this.x = x;
        this.y = y;
        this.thickness = t;
        this.id = id;
    }


    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        if( x < 0 || x > canvas.getWidth() || y < 0 || y > canvas.getWidth()) return;
        canvas.setPixel(x,y,0,0,0,255); //TODO temporary values
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
}
