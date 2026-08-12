package model.paintActions;

import model.Canvas;
import model.helpers.BoundingBox;

import java.awt.*;

/**
 * Interface that represents a user action
 */
public interface PaintAction {

    /** @return the ID of the user associated with this action */
    int getUserID();

    /** Apply this action to the provided canvas */
    void apply(Canvas canvas);

    /** Get the bounding box that encapsulates this action */
    BoundingBox getBoundingBox();
}
