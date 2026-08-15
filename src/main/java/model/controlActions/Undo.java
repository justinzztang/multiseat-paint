package model.controlActions;

import model.COWTileCanvas;
import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
import model.helpers.ActionTrackerDLL;
import model.helpers.ActionTrackerDLLNode;
import model.paintActions.PaintAction;
import model.paintActions.Undoable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Undo implements ControlAction{
    private int userID;

    public Undo(int id){
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
            ActionTrackerDLLNode jumpNode = apt.getLatestUndoPoint();
            apt.undoUpdate();
            ActionTrackerDLLNode redoNode = apt.getEarliestRedoPoint(); //if undoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "undone"

            for(ActionTrackerDLLNode node : jumpNode){
                if (node.paintAction.getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted theoretically
                    ((Undoable)node).setUndoStatus(Undoable.UndoStatus.UNDONE);
                }
                if(node == redoNode) break;
            }

            //resimulate the canvas
            //canvas.setLayer(LAST COMMON CANVAS)
            canvas.setLayer(canvas.getLayerCopy(pointToCanvasLayer.get(lcc))); //do we need locking here? //TODO we need locking here yeah

            for(ActionTrackerDLLNode node : lcc){
                node.paintAction.apply(canvas);
            }

            return true; //successfully undid

        } catch(Exception e){
            //theres no more undos, somehow, so do nothing
            return false; //did not successfuly undo
        }
    }
}
