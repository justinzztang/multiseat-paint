package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user has ended a stroke, by ending their click while using the paint tool
 */
public class EndStroke implements PaintAction, Undoable {
    private int x;
    private int y;
    private UndoStatus status = UndoStatus.DONE;

    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {
        System.out.println("applied EndStroke");
    }

    @Override
    public PointType getPointType() {
        return PointType.REDOPOINT;
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
