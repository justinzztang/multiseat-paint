package model.controlActions;

import model.COWTileCanvas;
import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
import model.helpers.ActionTrackerDLL;
import model.helpers.ActionTrackerDLLNode;
import model.paintActions.PaintAction;
import model.paintActions.Undoable;

import java.util.ArrayList;
import java.util.HashMap;

public class Redo implements ControlAction{
    private int userID;

    public Redo(int id){
        userID = id;
    }

    @Override
    public int getUserID(){
        return this.userID;
    }

    @Override
    public boolean runAction(COWTileCanvas canvas, HashMap<ActionTrackerDLLNode,Integer> pointToCanvasLayer,
                             ActionTrackerDLL timeline, ActionPointTracker<ActionTrackerDLLNode> apt, ActionTrackerDLLNode lcc) {
        try{
            ActionTrackerDLLNode jumpNode = apt.getEarliestRedoPoint();
            apt.redoUpdate();
            ActionTrackerDLLNode undoNode = apt.getLatestUndoPoint(); //if redoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "done"

            for(ActionTrackerDLLNode node : undoNode){
                if (node.paintAction.getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted theoretically
                    ((Undoable)node).setUndoStatus(Undoable.UndoStatus.UNDONE);
                }
                if(node == jumpNode) break;
            }

            //update canvas
            canvas.setLayer(canvas.getLayerCopy(pointToCanvasLayer.get(lcc))); //do we need locking here? //TODO we need locking here yeah

            for(ActionTrackerDLLNode node : lcc){
                node.paintAction.apply(canvas);
            }


            return true; //successful
        } catch(Exception e){
            //theres no more redos, so do nothing
            return false;
        }
    }
}
