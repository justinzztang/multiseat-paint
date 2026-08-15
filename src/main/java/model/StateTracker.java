package model;

import model.constants.CanvasConstants;
import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.ActionPointTracker;
import model.helpers.ActionTrackerDLLNode;
import model.helpers.BoundingBox;
import model.helpers.ActionTrackerDLL;
import model.paintActions.*;
import web.PaintServer;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Tracks and handles actions and canvas states, allowing undoing/redoing functionality
 * The core of the program
 */
public class StateTracker {

    private int id;
    public int uniqueUsers = 0;

    int nodeCounter = 0;

    //this stuff needs to be locked when copying
    public ActionTrackerDLL timeline = new ActionTrackerDLL();

    private COWTileCanvas canvas;
    //index -> layer number
    private LinkedHashMap<ActionTrackerDLLNode,Integer> pointerToCanvasLayer = new LinkedHashMap<>();
    private ActionTrackerDLLNode lcc = ActionTrackerDLLNode.emptyNode;
    /** ArrayList containing user actions in sequence */
    //private ArrayList<PaintAction> timeline = new ArrayList<>();

    public ActionTrackerDLLNode lastSyncNode = ActionTrackerDLLNode.emptyNode;

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<ActionTrackerDLLNode>> actionPointTracker = new HashMap<>();

    private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();


    public void updateLSI(){
        stateLock.writeLock().lock();
        try{
            if(timeline.isEmpty()){
                lastSyncNode = ActionTrackerDLLNode.emptyNode;
                return;
            }
            lastSyncNode = timeline.getLast();
        }
        finally{
            stateLock.writeLock().unlock();
        }
    }

    //TODO replace with tiles
    public BoundingBox affectedAreaBoundingBox(boolean shouldUpdate){
        if(timeline.isEmpty() || lastSyncNode == timeline.getLast() || lastSyncNode==ActionTrackerDLLNode.emptyNode){
            return new BoundingBox(0,0,0,0);
        }
        BoundingBox aabb = lastSyncNode.paintAction.getBoundingBox();

        for(ActionTrackerDLLNode node : lastSyncNode){
            aabb = BoundingBox.combine(aabb, node.paintAction.getBoundingBox());
        }
        if(shouldUpdate){
            updateLSI(); //TODO idk if this actually works, but the idea is that it updates right after all this so the lsi never drifts too far ahead of the timeline size
        }
        return aabb;
    }

    public StateTracker(int id, COWTileCanvas canvas){
        this.id = id;
        this.canvas = canvas;
    }

    public ActionTrackerDLLNode getLCC(){
        ArrayList<ActionTrackerDLLNode> indList = new ArrayList<>();
        actionPointTracker.forEach((Integer id,ActionPointTracker<ActionTrackerDLLNode> apt) -> {
            try{
                indList.add(apt.getLatestUndoPoint()); //technically not the latest, but is safe
            }
            catch (Exception _){

            }
            try{
                indList.add(apt.getEarliestUnavailableUndoPoint()); //technically not the latest, but is safe
            }
            catch (Exception _){

            }
        });
        if(indList.isEmpty()){ return timeline.getFirst(); } //not a single undo
        return Collections.min(indList);
    }

    //receive action that isnt something you apply to the canvas, like undos
    //synchronized because every request must be processed in order
    public synchronized void receiveControlAction(ControlAction controlAction){

        stateLock.writeLock().lock();
        try {
            //create if not initialized
            if (!actionPointTracker.containsKey(controlAction.getUserID())) {
                actionPointTracker.put(controlAction.getUserID(), new ActionPointTracker<>());
            }
            ActionPointTracker<ActionTrackerDLLNode> apt = actionPointTracker.get(controlAction.getUserID());

            this.lcc = getLCC();
            //if you undo, we want to set lsi to this
            //if you redo, we want to not set lsi to this
            //if(controlAction instanceof Undo || Redo){
            lastSyncNode = lcc;
            //}


            //if(controlAction instanceof Redo){debugBreakpoint();}

            controlAction.runAction(canvas, pointerToCanvasLayer, timeline, apt, lcc);
        }
        finally{
            stateLock.writeLock().unlock();
        }

        //System.out.println(canvas.printCanvas());


    }

    //receive a concrete action that affects the canvas
    //synchronized because every request must be processed in order
    //private int actiontracker = 0;
    public synchronized void receivePaintAction(PaintAction paintAction){

        stateLock.writeLock().lock();
        try {
            //create if not initialized
            if (!actionPointTracker.containsKey(paintAction.getUserID())) {
                actionPointTracker.put(paintAction.getUserID(), new ActionPointTracker<>());
            }

            //store it in the timeline
            ActionTrackerDLLNode action = new ActionTrackerDLLNode(paintAction,nodeCounter);
            timeline.addLast(action);


            //if undoable, track the undo stuff
            if (paintAction instanceof Undoable undoable) {
                ActionPointTracker<ActionTrackerDLLNode> apt = actionPointTracker.get(paintAction.getUserID());

                if (undoable.getPointType().equals(Undoable.PointType.UNDOPOINT)) {

                    //need to overwrite everything after this
                    if (!apt.availableRedosEmpty() || !apt.unavailableUndosEmpty()) {
                        try {

                            ActionTrackerDLLNode startingNode = apt.getEarliestUnavailableUndoPoint();

                            for(ActionTrackerDLLNode node : startingNode){
                                if(node == action) break; //stop at the new one
                                if(node.paintAction.getUserID() == paintAction.getUserID()){
                                    if(node.paintAction instanceof Undoable){
                                        timeline.removeUsingNodeReference(node);
                                    }
                                }
                            }
                        } catch (Exception _) {
                            //should never reach here with the if statement check
                        }
                    }
                    apt.addUndo(action);
                    //need to save a canvas snapshot
                    pointerToCanvasLayer.put(action, canvas.getNumLayers() - 1); //mark the current canvas layer as UNDOPOINT_timelineindex
                    canvas.copyTopLayer(); //creates a new layer on which everything will be applied, preserving the previous (before this copy) layer


                } else if (undoable.getPointType().equals(Undoable.PointType.REDOPOINT)) {
                    apt.addRedo(action);
                    //need to save a canvas snapshot, but AFTER application
                    //paintAction.apply(canvas);
                    //pointToCanvasLayer.put(timelineIndex, canvas.getNumLayers()-1); //mark the current canvas layer as REDOPOINT_timelineindex
                    //canvas.copyTopLayer(); //creates a new layer on which everything after will be applied, preserving the previous (before this copy) layer
                    //return; //TODO this might need to change in the future
                }

            }
            //apply the action
            //System.out.println("applied action #" + actiontracker);
            //actiontracker++;
            paintAction.apply(canvas);
            //indexTrackerEnd = temp;
            nodeCounter++;
        }
        finally {
            stateLock.writeLock().unlock();
        }
        //System.out.println(canvas.printCanvas());

    }

    //clean up timeline, getting rid of unreachable canvases (past the undo limit), removing overwritten commands, and changing the tracker stuff
    //locks the state and prevents writing (but not reading)
    //filtering the timeline and updating numbers is linear in the number of elements in the timeline
    //very "conservatively": a single stroke is about 200 commands, there will be 16 users, and an average of 300 strokes stored for each one (cause some people havent drawn in a long time
    //thats less than 1 million elements in timeline, linear time should work perfectly fine
    //runs periodically, but not frequently
    public void cleanCanvas(){
        //no one else can write
        stateLock.writeLock().lock();

        try{

            ActionTrackerDLLNode earliestUndoLimitNode;
            ArrayList<ActionTrackerDLLNode> indList = new ArrayList<>();
            actionPointTracker.forEach((Integer id,ActionPointTracker<ActionTrackerDLLNode> apt) -> {
                try{
                    indList.add(apt.earliestUndo());
                }
                catch (Exception _){

                }
            });
            if(indList.isEmpty()){ earliestUndoLimitNode = timeline.getFirst(); } //no one has ANY undos? probably no ones drawn yet
            else{ earliestUndoLimitNode = Collections.min(indList); }

            //now flatten canvas, update PTCL (which is in order, as in smaller points have smaller indices),

            //find the first layer that doesnt need to be cleaned
            int firstSafeLayer=-1;
            for(Map.Entry<ActionTrackerDLLNode, Integer> entry : pointerToCanvasLayer.entrySet()){
                if(entry.getKey().compareTo(earliestUndoLimitNode) >=0 && firstSafeLayer==-1){
                    firstSafeLayer = entry.getValue();
                }
                if(firstSafeLayer >=0 ) pointerToCanvasLayer.put(entry.getKey(), entry.getValue()-firstSafeLayer); //update the canvas layers
            }

            //delete the first few layers of the canvas
            if(firstSafeLayer >=0 ) canvas.getTileLayers().subList(0,firstSafeLayer);

        }
        finally{
            stateLock.writeLock().unlock();
        }

    }

    public void debugBreakpoint(){
        return;
    }

    public ActionTrackerDLL getTimeline() {
        return timeline;
    }

    public HashMap<Integer, ActionPointTracker<ActionTrackerDLLNode>> getActionPointTracker() {
        return actionPointTracker;
    }

    public HashMap<ActionTrackerDLLNode, Integer> getPointerToCanvasLayer() {
        return pointerToCanvasLayer;
    }
}
