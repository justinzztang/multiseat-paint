package model;

import model.constants.CanvasConstants;
import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.ActionPointTracker;
import model.helpers.BoundingBox;
import model.helpers.IndexTrackerDLLNode;
import model.paintActions.*;
import web.PaintServer;

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
    public IndexTrackerDLLNode indexTrackerDLL = new IndexTrackerDLLNode(false,0, 0);
    public IndexTrackerDLLNode indexTrackerHead = indexTrackerDLL; //should always be 0 no?
    public IndexTrackerDLLNode indexTrackerEnd = indexTrackerDLL; //should always be timeline.size()-1 no?

    private COWTileCanvas canvas;
    //index -> layer number
    private LinkedHashMap<IndexTrackerDLLNode,Integer> pointToCanvasLayer = new LinkedHashMap<>();
    private IndexTrackerDLLNode lcc = indexTrackerHead;
    /** ArrayList containing user actions in sequence */
    private ArrayList<PaintAction> timeline = new ArrayList<>();

    public IndexTrackerDLLNode lastSyncIndex = indexTrackerEnd;

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> actionPointTracker = new HashMap<>();

    public ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock(true);


    public void updateLSI(){
        stateLock.writeLock().lock();
        try{
            if(timeline.isEmpty()){
                lastSyncIndex = indexTrackerHead;
                return;
            }
            lastSyncIndex = indexTrackerEnd;
        }
        finally{
            stateLock.writeLock().unlock();
        }
    }

    //TODO replace with tiles
    public BoundingBox affectedAreaBoundingBox(boolean shouldUpdate){
        if(timeline.isEmpty() || lastSyncIndex.indexNumber == timeline.size()-1){
            return new BoundingBox(0,0,0,0);
        }
        BoundingBox aabb = timeline.get(lastSyncIndex.indexNumber).getBoundingBox();
        for(int i=lastSyncIndex.indexNumber; i<timeline.size(); i++){
            aabb = BoundingBox.combine(aabb, timeline.get(i).getBoundingBox());
        }
        if(shouldUpdate){
            updateLSI(); //TODO idk if this actually works, but the idea is that it updates right after all this so the lsi never drifts too far ahead of the timeline size
        }
        return aabb;
    }

    public CanvasTile[] affectedAreaTiles(boolean shouldUpdate){
        if(timeline.isEmpty() || lastSyncIndex.indexNumber == timeline.size()-1){
            return new CanvasTile[]{}; //empty
        }
        Set<CanvasTile> affectedTiles = new HashSet<>();
        for(int i=lastSyncIndex.indexNumber+1; i<timeline.size(); i++){
            BoundingBox bb = timeline.get(i).getBoundingBox();

            int minXTile = bb.minX / CanvasConstants.TILE_SIDE;
            int maxXTile = bb.maxX / CanvasConstants.TILE_SIDE;
            int minYTile = bb.minY / CanvasConstants.TILE_SIDE;
            int maxYTile = bb.maxY / CanvasConstants.TILE_SIDE;

            for(int y = minYTile;y<=maxYTile;y++){
                for(int x = minXTile;x<=maxXTile;x++){
                    affectedTiles.add(canvas.getTileLayers().getLast().second()[y][x]);
                }
            }


        }
        if(shouldUpdate){
            updateLSI();
        }
        return affectedTiles.toArray(new CanvasTile[0]);
    }


    public StateTracker(int id, COWTileCanvas canvas){
        this.id = id;
        this.canvas = canvas;
    }

    public IndexTrackerDLLNode getLCC(){
        ArrayList<IndexTrackerDLLNode> indList = new ArrayList<>();
        actionPointTracker.forEach((Integer id,ActionPointTracker<IndexTrackerDLLNode> apt) -> {
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
        if(indList.isEmpty()){ return indexTrackerHead; }
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
            ActionPointTracker<IndexTrackerDLLNode> apt = actionPointTracker.get(controlAction.getUserID());

            this.lcc = getLCC();
            //if you undo, we want to set lsi to this
            //if you redo, we want to not set lsi to this
            //if(controlAction instanceof Undo || Redo){
            lastSyncIndex = lcc;
            //}
            System.out.println(lcc.indexNumber);

            //if(controlAction instanceof Redo){debugBreakpoint();}
            stateLock.readLock().lock();
            try {
                controlAction.runAction(canvas, pointToCanvasLayer, timeline, apt, lcc);
            }
            finally{
                stateLock.readLock().unlock();
            }
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
            timeline.add(paintAction);
            int timelineIndex = timeline.size() - 1;

            IndexTrackerDLLNode temp = new IndexTrackerDLLNode(false, timelineIndex, indexTrackerEnd,nodeCounter);
            if(timelineIndex==0){
                temp = indexTrackerEnd;
            }

            //if undoable, track the undo stuff
            if (paintAction instanceof Undoable undoable) {
                ActionPointTracker<IndexTrackerDLLNode> apt = actionPointTracker.get(paintAction.getUserID());

                if (undoable.getPointType().equals(Undoable.PointType.UNDOPOINT)) {

                    //need to overwrite everything after this
                    if (!apt.availableRedosEmpty() || !apt.unavailableUndosEmpty()) {
                        try {
                            int startingIndex = apt.getEarliestUnavailableUndoPoint().indexNumber;
                            for (int i = startingIndex; i < timeline.size() - 1; i++) { //-1 so the new one doesnt get touched
                                if (timeline.get(i).getUserID() == paintAction.getUserID()) {
                                    if (timeline.get(i) instanceof Undoable overwrite) {
                                        overwrite.setUndoStatus(Undoable.UndoStatus.OVERWRITTEN);
                                    }
                                }
                            }
                        } catch (Exception _) {
                            //should never reach here with the if statement check
                        }
                    }
                    temp.isIndex = true;
                    apt.addUndo(temp);
                    //need to save a canvas snapshot
                    pointToCanvasLayer.put(temp, canvas.getNumLayers() - 1); //mark the current canvas layer as UNDOPOINT_timelineindex
                    canvas.copyTopLayer(); //creates a new layer on which everything will be applied, preserving the previous (before this copy) layer


                } else if (undoable.getPointType().equals(Undoable.PointType.REDOPOINT)) {
                    temp.isIndex = true;
                    apt.addRedo(temp);
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
            indexTrackerEnd = temp;
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
    public void cleanTimeline(){
        //no one else can write
        stateLock.writeLock().lock();

        try{
            ArrayList<PaintAction> filteredTimeline = new ArrayList<>();

            int earliestUndoLimit;
            ArrayList<Integer> indList = new ArrayList<>();
            actionPointTracker.forEach((Integer id,ActionPointTracker<IndexTrackerDLLNode> apt) -> {
                try{
                    indList.add(apt.earliestUndo().indexNumber);
                }
                catch (Exception _){

                }
            });
            if(indList.isEmpty()){ earliestUndoLimit=0; } //no one has ANY undos? probably no ones drawn yet
            else{ earliestUndoLimit = Collections.min(indList); }

            int curIndex = 0;
            assert(indexTrackerEnd.indexNumber <= timeline.size());
            for(IndexTrackerDLLNode node : indexTrackerDLL){
                boolean add = true;
                if(curIndex < earliestUndoLimit){ //do not add
                    add = false;
                    //splice out the node
                    node.spliceOut();
                    if(node.prev==null){
                        indexTrackerHead = node.next;
                    }
                    if(node.next==null){
                        indexTrackerEnd = node.prev;
                    }
                    //if both happen, then we have an empty list, and both head and end are null
                }
                else if(timeline.get(curIndex) instanceof Undoable undoable){
                    if(undoable.getUndoStatus() == Undoable.UndoStatus.OVERWRITTEN){//do not add
                        add = false;
                        //splice out the node
                        node.spliceOut();
                        if(node.prev==null){
                            indexTrackerHead = node.next;
                        }
                        if(node.next==null){
                            indexTrackerEnd = node.prev;
                        }
                        //if both happen, then we have an empty list, and both head and end are null
                    }
                }
                if(add){
                    filteredTimeline.add(timeline.get(curIndex));
                }
                curIndex++;
            }

            curIndex = 0;
            for(IndexTrackerDLLNode node : indexTrackerHead){
                node.indexNumber = curIndex;
                curIndex++;
            }
            //timeline is filtered, indices are updated
            timeline = filteredTimeline;

            //now flatten canvas, update PTCL (which is in order, as in smaller points have smaller indices),

            //find the first layer that doesnt need to be cleaned
            int firstSafeLayer=-1;
            for(Map.Entry<IndexTrackerDLLNode, Integer> entry : pointToCanvasLayer.entrySet()){
                if(!entry.getKey().deleted && firstSafeLayer==-1){
                    firstSafeLayer = entry.getValue();
                }
                if(firstSafeLayer >=0 ) pointToCanvasLayer.put(entry.getKey(), entry.getValue()-firstSafeLayer); //update the canvas layers
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

    public ArrayList<PaintAction> getTimeline() {
        return timeline;
    }

    public HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> getActionPointTracker() {
        return actionPointTracker;
    }

    public HashMap<IndexTrackerDLLNode, Integer> getPointToCanvasLayer() {
        return pointToCanvasLayer;
    }
}
