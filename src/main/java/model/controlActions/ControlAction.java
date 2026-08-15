package model.controlActions;

import model.COWTileCanvas;
import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
import model.helpers.ActionTrackerDLL;
import model.helpers.ActionTrackerDLLNode;
import model.paintActions.PaintAction;

import java.util.ArrayList;
import java.util.HashMap;

public interface ControlAction {
    int getUserID();

    boolean runAction(COWTileCanvas canvas,
                      HashMap<ActionTrackerDLLNode,Integer> pointToCanvasLayer,
                      ActionTrackerDLL timeline,
                      ActionPointTracker<ActionTrackerDLLNode> apt,
                      ActionTrackerDLLNode lcc);
}
