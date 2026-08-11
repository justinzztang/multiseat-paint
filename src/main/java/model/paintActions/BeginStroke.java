package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user has begun a stroke, by clicking down with the paint tool
 */
public class BeginStroke implements PaintAction, Undoable {
    private int x;
    private int y;
    private UndoStatus status = UndoStatus.DONE;

    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {
        if(status != UndoStatus.DONE) return;
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
