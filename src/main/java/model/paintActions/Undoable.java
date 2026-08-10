package model.paintActions;

/**
 * Interface that represents an undoable (and redoable) user action
 */
public interface Undoable {

    enum UndoStatus{
        DONE,
        UNDONE
    }

    void setUndoStatus(UndoStatus status);

    UndoStatus getUndoStatus();

}
