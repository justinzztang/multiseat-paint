package model.controlActions;

import model.*;
import model.Canvas;
import model.helpers.ActionPointTracker;
import model.helpers.IndexTrackerDLLNode;
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
    public boolean runAction(LayeredCanvas<TiledCanvas> canvas, HashMap<IndexTrackerDLLNode,Integer> pointToCanvasLayer,
                             ArrayList<PaintAction> timeline, ActionPointTracker<IndexTrackerDLLNode> apt, IndexTrackerDLLNode lcc) {
        try{
            int jumpIndex = apt.getLatestUndoPoint().indexNumber;
            apt.undoUpdate();
            int redoIndex = apt.getEarliestRedoPoint().indexNumber; //if undoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "undone"
            for(int i=jumpIndex; i<=redoIndex; i++){
                if(timeline.get(i).getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted theoretically
                    ((Undoable)timeline.get(i)).setUndoStatus(Undoable.UndoStatus.UNDONE);
                }
            }

            //resimulate the canvas
            //canvas.setLayer(LAST COMMON CANVAS)
            canvas.setTopLayer(canvas.getLayer(pointToCanvasLayer.get(lcc)).copy()); //TODO bug: this should be LCC
            for(int i=lcc.indexNumber; i<timeline.size();i++){
                timeline.get(i).apply(canvas);
            }

            return true; //successfully undid

        } catch(Exception e){
            //theres no more undos, somehow, so do nothing
            return false; //did not successfuly undo
        }
    }
}
