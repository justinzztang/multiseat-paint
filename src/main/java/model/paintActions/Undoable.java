package model.paintActions;

/**
 * Interface that represents an undoable (and redoable) user action
 */
public interface Undoable {

    enum PointType{
        UNDOPOINT,
        REDOPOINT,
        INBETWEEN
    }

    enum UndoStatus{
        DONE,
        UNDONE,
        OVERWRITTEN
    }

    PointType getPointType();

    void setUndoStatus(UndoStatus status);

    UndoStatus getUndoStatus();

}
