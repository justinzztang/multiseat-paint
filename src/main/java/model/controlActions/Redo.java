package model.controlActions;

import model.*;
import model.helpers.ActionPointTracker;
import model.helpers.IndexTrackerDLLNode;
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
    public boolean runAction(LayeredCanvas<TiledCanvas> canvas, HashMap<IndexTrackerDLLNode,Integer> pointToCanvasLayer,
                             ArrayList<PaintAction> timeline, ActionPointTracker<IndexTrackerDLLNode> apt, IndexTrackerDLLNode lcc) {
        try{
            int jumpIndex = apt.getEarliestRedoPoint().indexNumber;
            apt.redoUpdate();
            int undoIndex = apt.getLatestUndoPoint().indexNumber; //if redoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "done"
            for(int i=undoIndex; i<=jumpIndex; i++){
                if(timeline.get(i).getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted
                    ((Undoable)timeline.get(i)).setUndoStatus(Undoable.UndoStatus.DONE);
                }
            }

            canvas.setTopLayer(canvas.getLayer(pointToCanvasLayer.get(lcc)).copy()); //TODO bug: this should be LCC
            for(int i=lcc.indexNumber; i<timeline.size();i++){
                timeline.get(i).apply(canvas);
            }



            return true;
        } catch(Exception e){
            //theres no more redos, so do nothing
            return false;
        }
    }
}
