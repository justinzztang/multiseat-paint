package model.controlActions;

import model.Canvas;
import model.helpers.ActionPointTracker;
import model.paintActions.PaintAction;
import model.paintActions.Undoable;

import java.util.ArrayList;

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
    public boolean runAction(Canvas canvas, ArrayList<PaintAction> timeline, ActionPointTracker<Integer> apt) {
        try{
            int jumpIndex = apt.getEarliestRedoPoint();
            apt.redoUpdate();
            int undoIndex = apt.getLatestUndoPoint(); //if redoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "done"
            for(int i=undoIndex; i<=jumpIndex; i++){
                if(timeline.get(i).getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted theoretically
                    ((Undoable)timeline.get(i)).setUndoStatus(Undoable.UndoStatus.DONE);
                }
            }
            //update canvas
            return true; //successful
        } catch(Exception e){
            //theres no more redos, so do nothing
            return false;
        }
    }
}
