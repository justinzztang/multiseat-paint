package model;

import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.ActionPointTracker;
import model.paintActions.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tracks and handles actions and canvas states, allowing undoing/redoing functionality
 * The core of the program
 */
public class StateTracker {

    private int id;
    private Canvas canvas;

    //timeline:some data structure that allows ordered storage and O(1) lookups
    //undopointtracker:some data structure that allows ordered storage and O(1) addition and removal of latest element



    /** ArrayList containing user actions in sequence */
    private ArrayList<PaintAction> timeline = new ArrayList<>();

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<Integer>> actionPointTracker = new HashMap<>();

    public StateTracker(int id, Canvas canvas){
        this.id = id;
        this.canvas = canvas;
    }

    //receive action that isnt something you apply to the canvas, like undos
    public void receiveControlAction(ControlAction controlAction){

        //create if not initialized
        if(!actionPointTracker.containsKey(controlAction.getUserID())){
            actionPointTracker.put(controlAction.getUserID(), new ActionPointTracker<>());
        }
        ActionPointTracker<Integer> apt = actionPointTracker.get(controlAction.getUserID());

        controlAction.runAction(canvas, timeline, apt);

    }

    //receive a concrete action that affects the canvas
    public void receiveAction(PaintAction paintAction){

        //create if not existant
        if(!actionPointTracker.containsKey(paintAction.getUserID())){
            actionPointTracker.put(paintAction.getUserID(), new ActionPointTracker<>());
        }

        //store it in the timeline
        timeline.add(paintAction);
        int timelineIndex = timeline.size() - 1;
        //if undoable, track the undo stuff
        if(paintAction instanceof Undoable undoable){
            ActionPointTracker<Integer> apt = actionPointTracker.get(paintAction.getUserID());
            //need more robust way to check if something is an undopoint or redopoint
            if(undoable instanceof BeginStroke){
                apt.addUndo(timelineIndex);
            }
            else if(undoable instanceof EndStroke){
                apt.addRedo(timelineIndex);
            }
        }
        //apply the action
        paintAction.apply(canvas);
    }


}
