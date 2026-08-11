package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user is drawing on the canvas,
 */
public class Draw implements PaintAction, Undoable {
    private int x;
    private int y;
    private UndoStatus status = UndoStatus.DONE;

    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {
        System.out.println("applied Draw");
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
