package model.controlActions;

import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
import model.paintActions.PaintAction;

import java.util.ArrayList;
import java.util.HashMap;

public interface ControlAction {
    int getUserID();

    boolean runAction(MemorySmartCanvas canvas,
                      HashMap<Integer,Integer> pointToCanvasLayer,
                      ArrayList<PaintAction> timeline,
                      ActionPointTracker<Integer> apt,
                      int lcc);
}
