package model.controlActions;

import model.COWTileCanvas;
import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
import model.helpers.IndexTrackerDLLNode;
import model.paintActions.PaintAction;

import java.util.ArrayList;
import java.util.HashMap;

public interface ControlAction {
    int getUserID();

    boolean runAction(COWTileCanvas canvas,
                      HashMap<Integer,Integer> pointToCanvasLayer,
                      ArrayList<PaintAction> timeline,
                      ActionPointTracker<IndexTrackerDLLNode> apt,
                      int lcc);
}
