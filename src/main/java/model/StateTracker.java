package model;

import model.controlActions.ControlAction;
import model.helpers.ActionPointTracker;
import model.paintActions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Tracks and handles actions and canvas states, allowing undoing/redoing functionality
 * The core of the program
 */
public class StateTracker {

    private int id;
    private MemorySmartCanvas canvas;
    private HashMap<Integer,Integer> pointToCanvasLayer = new HashMap<>();
    private int lcc = 0;

    //timeline:some data structure that allows ordered storage and O(1) lookups
    //undopointtracker:some data structure that allows ordered storage and O(1) addition and removal of latest element



    /** ArrayList containing user actions in sequence */
    private ArrayList<PaintAction> timeline = new ArrayList<>();

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<Integer>> actionPointTracker = new HashMap<>();

    public StateTracker(int id, MemorySmartCanvas canvas){
        this.id = id;
        this.canvas = canvas;
    }

    public int getLCC(){
        ArrayList<Integer> indList = new ArrayList<>();
        actionPointTracker.forEach((Integer id,ActionPointTracker<Integer> apt) -> {
            try{
                indList.add(apt.getLatestUndoPoint()); //technically not the latest, but is safe
            }
            catch (Exception _){

            }
        });
        if(indList.isEmpty()){ return 0; }
        return Collections.min(indList);
    }

    //receive action that isnt something you apply to the canvas, like undos
    public void receiveControlAction(ControlAction controlAction){

        //create if not initialized
        if(!actionPointTracker.containsKey(controlAction.getUserID())){
            actionPointTracker.put(controlAction.getUserID(), new ActionPointTracker<>());
        }
        ActionPointTracker<Integer> apt = actionPointTracker.get(controlAction.getUserID());

        this.lcc = getLCC();

        controlAction.runAction(canvas, pointToCanvasLayer, timeline, apt, lcc);
    }

    //receive a concrete action that affects the canvas
    public void receivePaintAction(PaintAction paintAction){

        //create if not initialized
        if(!actionPointTracker.containsKey(paintAction.getUserID())){
            actionPointTracker.put(paintAction.getUserID(), new ActionPointTracker<>());
        }

        //store it in the timeline
        timeline.add(paintAction);
        int timelineIndex = timeline.size() - 1;
        //if undoable, track the undo stuff
        if(paintAction instanceof Undoable undoable){
            ActionPointTracker<Integer> apt = actionPointTracker.get(paintAction.getUserID());

            if(undoable.getPointType().equals(Undoable.PointType.UNDOPOINT)){

                //need to overwrite everything after this
                if(!apt.availableRedosEmpty() || !apt.unavailableUndosEmpty()){
                    try{
                        int startingIndex = apt.getEarliestUnavailableUndoPoint();
                        for(int i=startingIndex; i<timeline.size()-1;i++){ //-1 so the new one doesnt get touched
                            if(timeline.get(i).getUserID() == paintAction.getUserID()){
                                if(timeline.get(i) instanceof Undoable overwrite){
                                    overwrite.setUndoStatus(Undoable.UndoStatus.OVERWRITTEN);
                                }
                            }
                        }
                    } catch (Exception _){
                        //should never reach here with the if statement check
                    }
                }

                apt.addUndo(timelineIndex);
                //need to save a canvas snapshot
                pointToCanvasLayer.put(timelineIndex, canvas.getNumLayers()-1); //mark the current canvas layer as UNDOPOINT_timelineindex
                canvas.copyLayer(); //creates a new layer on which everything will be applied, preserving the previous (before this copy) layer


            }
            else if(undoable.getPointType().equals(Undoable.PointType.REDOPOINT)){
                apt.addRedo(timelineIndex);
                //need to save a canvas snapshot, but AFTER application
                paintAction.apply(canvas);
                pointToCanvasLayer.put(timelineIndex, canvas.getNumLayers()-1); //mark the current canvas layer as REDOPOINT_timelineindex
                canvas.copyLayer(); //creates a new layer on which everything after will be applied, preserving the previous (before this copy) layer
                return; //TODO this might need to change in the future
            }

        }
        //apply the action
        paintAction.apply(canvas);
    }


}
