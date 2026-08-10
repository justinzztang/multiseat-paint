package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user has begun a stroke, by clicking down with the paint tool
 */
public class BeginStroke implements Action {
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
