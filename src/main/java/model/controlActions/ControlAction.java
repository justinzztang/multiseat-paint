package model.controlActions;

import model.*;
import model.helpers.ActionPointTracker;
import model.helpers.IndexTrackerDLLNode;
import model.paintActions.PaintAction;

import java.util.ArrayList;
import java.util.HashMap;

public interface ControlAction {
    int getUserID();

    boolean runAction(LayeredCanvas<TiledCanvas> canvas,
                      HashMap<IndexTrackerDLLNode,Integer> pointToCanvasLayer,
                      ArrayList<PaintAction> timeline,
                      ActionPointTracker<IndexTrackerDLLNode> apt,
                      IndexTrackerDLLNode lcc);
}
