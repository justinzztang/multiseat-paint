package model.paintActions;

import model.Canvas;

/**
 * Interface that represents a user action
 */
public interface Action {

    /** @return the ID of the user associated with this action */
    int getUserID();

    /** Apply this action to the provided canvas */
    void apply(Canvas canvas);
}
