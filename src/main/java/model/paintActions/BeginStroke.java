package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user has begun a stroke, by clicking down with the paint tool
 */
public class BeginStroke implements PaintAction, Undoable {
    private int x;
    private int y;
    private int id;
    private UndoStatus status = UndoStatus.DONE;

    public BeginStroke(int x, int y, int id){
        this.x = x;
        this.y = y;
        this.id = id;
    }


    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
        canvas.setPixel(x,y,0,0,0,255); //TODO temporary values
        System.out.println("applied BeginStroke");
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
