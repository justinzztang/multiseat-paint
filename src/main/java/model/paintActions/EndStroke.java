package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user has ended a stroke, by ending their click while using the paint tool
 */
public class EndStroke implements Action {
    private int x;
    private int y;

    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {

    }
}
