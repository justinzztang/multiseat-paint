package model.controlActions;

import model.COWTileCanvas;
import model.Canvas;
import model.MemorySmartCanvas;
import model.helpers.ActionPointTracker;
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
    public boolean runAction(COWTileCanvas canvas, HashMap<Integer,Integer> pointToCanvasLayer,
                             ArrayList<PaintAction> timeline, ActionPointTracker<Integer> apt, int lcc) {
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
            //canvas.setLayer(LAST COMMON CANVAS)

            canvas.setLayer(canvas.getLayerCopy(pointToCanvasLayer.get(lcc)));
            for(int i=lcc; i<timeline.size();i++){
                timeline.get(i).apply(canvas);
            }

            return true; //successfully undid

        } catch(Exception e){
            //theres no more undos, somehow, so do nothing
            return false; //did not successfuly undo
        }
    }
}
