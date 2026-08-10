package model.paintActions;

import model.Canvas;

/**
 * Action that indicates a user is drawing on the canvas,
 * tracking the start and end points to make the stroke smooth and connected
 */
public class LerpDraw implements Action {
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    @Override
    public int getUserID() {
        return 0;
    }

    @Override
    public void apply(Canvas canvas) {

    }
}
