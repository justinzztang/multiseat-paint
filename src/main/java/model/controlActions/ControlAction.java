package model.controlActions;

import model.Canvas;
import model.helpers.ActionPointTracker;
import model.paintActions.PaintAction;

import java.util.ArrayList;

public interface ControlAction {
    int getUserID();

    boolean runAction(Canvas canvas, ArrayList<PaintAction> timeline, ActionPointTracker<Integer> apt);
}
