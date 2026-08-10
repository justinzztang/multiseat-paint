package model;

import model.helpers.ActionPointTracker;
import model.paintActions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

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
    private ArrayList<Action> timeline = new ArrayList<>();

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<Integer>> actionPointTracker = new HashMap<>();

    /** ArrayList tracking previous canvas states for undoing/redoing */
    private ArrayList<Canvas> canvasList = new ArrayList<>();

    public StateTracker(int id, Canvas canvas){
        this.id = id;
        this.canvas = canvas;
    }




    //receive a concrete action that affects the canvas
    public void receiveAction(Action action){

        //create if not existant
        if(!actionPointTracker.containsKey(action.getUserID())){
            actionPointTracker.put(action.getUserID(), new ActionPointTracker<>());
        }

        //store it in the timeline
        timeline.add(action);
        int timelineIndex = timeline.size() - 1;
        //if undoable, track the undo stuff
        if(action instanceof Undoable undoable){
            ActionPointTracker<Integer> apt = actionPointTracker.get(action.getUserID());
            //need more robust way to check if something is an undopoint or redopoint
            if(undoable instanceof BeginStroke){
                apt.addUndo(timelineIndex);
            }
            else if(undoable instanceof EndStroke){
                apt.addRedo(timelineIndex);
            }
        }
        //apply the action
        action.apply(canvas);
    }


}
