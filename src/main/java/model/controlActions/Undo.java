package model.controlActions;

import model.Canvas;
import model.helpers.ActionPointTracker;
import model.paintActions.PaintAction;
import model.paintActions.Undoable;

import java.util.ArrayList;

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
    public boolean runAction(Canvas canvas, ArrayList<PaintAction> timeline, ActionPointTracker<Integer> apt) {
        try{
            int jumpIndex = apt.getLatestUndoPoint();
            apt.undoUpdate();
            int redoIndex = apt.getEarliestRedoPoint(); //if undoUpdate shot off correctly, this should always work
            //mark everything done by this ID as "undone"
            for(int i=jumpIndex; i<=redoIndex; i++){
                if(timeline.get(i).getUserID() == userID){
                    //should be safe, undoable actions come in groups uninterrupted theoretically
                    ((Undoable)timeline.get(i)).setUndoStatus(Undoable.UndoStatus.UNDONE);
                }
            }

            //resimulate the canvas

            return true; //successfully undid

        } catch(Exception e){
            //theres no more undos, somehow, so do nothing
            return false; //did not successfuly undo
        }
    }
}
