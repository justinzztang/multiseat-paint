package model;

import model.constants.CanvasConstants;
import model.controlActions.ControlAction;
import model.helpers.ActionPointTracker;
import model.helpers.BoundingBox;
import model.helpers.IndexTrackerDLLNode;
import model.paintActions.*;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Receives user actions and updates the canvas accordingly, keeping track of undoable actions to allow for undo/redo functionality.
 */
public class StateTracker {

    //metadata
    /** Unique ID */
    public int id;

    /** The number of unique users that have sent an action to this stateTracker */
    public AtomicInteger uniqueUsers = new AtomicInteger(0);

    public AtomicInteger activeConnections = new AtomicInteger(0);

    //state

    private int nodeCounter = 0;

    /** Doubly Linked List of index objects, each representing an integer index shared across multiple trackers */
    private IndexTrackerDLLNode indexTrackerDLL = new IndexTrackerDLLNode(false,0, 0);
    private IndexTrackerDLLNode indexTrackerHead = indexTrackerDLL; //should always be index 0
    private IndexTrackerDLLNode indexTrackerEnd = indexTrackerDLL; //should always be index timeline.size()-1

    private int undoPoints = 0;
    private int redoPoints = 0;

    /** Stores the pixels of all "uncommitted" operations, such as selection or text, so they can sync with other users */
    private Canvas bufferLayers;

    /** The canvas that stores the current state of the drawing, alongside "snapshots" of previous states for undo/redo usage */
    private LayeredCanvas<TiledCanvas> canvas;

    //index -> layer number
    private LinkedHashMap<IndexTrackerDLLNode,Integer> pointToCanvasLayer = new LinkedHashMap<>();

    /** Index of the "last common canvas," before any undone operations */
    private IndexTrackerDLLNode lcc = indexTrackerHead;

    /** ArrayList containing user actions in sequence */
    private ArrayList<PaintAction> timeline = new ArrayList<>();

    /** Index in timeline where the last synchronization ended at */
    public IndexTrackerDLLNode lastSyncIndex = indexTrackerEnd;

    /** Stores each player's undo data */
    private HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> actionPointTracker = new HashMap<>();

    public ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock(true);

    /** Update the last sync index of this StateTracker */
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
    @Deprecated
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

    /** @return an array of CanvasTiles that were modified since the last synchronization */
    public CanvasTile[] affectedAreaTiles(boolean shouldUpdate){

        if(timeline.isEmpty() || lastSyncIndex.indexNumber == timeline.size()-1){
            return new CanvasTile[]{}; //empty
        }
        Set<CanvasTile> affectedTiles = new HashSet<>();
        for(int i=lastSyncIndex.indexNumber; i<timeline.size(); i++){
            BoundingBox bb = timeline.get(i).getBoundingBox();

            int minXTile = Math.clamp(bb.minX,0,canvas.getWidth()) / CanvasConstants.TILE_SIDE;
            int maxXTile = Math.clamp(bb.maxX,0,canvas.getWidth()) / CanvasConstants.TILE_SIDE;
            int minYTile = Math.clamp(bb.minY,0,canvas.getHeight()) / CanvasConstants.TILE_SIDE;
            int maxYTile = Math.clamp(bb.maxY,0,canvas.getHeight()) / CanvasConstants.TILE_SIDE;

            for(int y = minYTile;y<=maxYTile;y++){
                for(int x = minXTile;x<=maxXTile;x++){
                    affectedTiles.add(canvas.getTop().getTile(x,y));
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

    /** Handle user actions that do not directly affect the canvas, such as undo operations
     * Must process each request in order
     */
    public synchronized void receiveControlAction(ControlAction controlAction){

        stateLock.writeLock().lock();
        stateLock.readLock().lock();
        try {
            //create if not initialized
            if (!actionPointTracker.containsKey(controlAction.getUserID())) {
                actionPointTracker.put(controlAction.getUserID(), new ActionPointTracker<>());
            }
            ActionPointTracker<IndexTrackerDLLNode> apt = actionPointTracker.get(controlAction.getUserID());

            this.lcc = getLCC();
            lastSyncIndex = lcc;

            controlAction.runAction(canvas, pointToCanvasLayer, timeline, apt, lcc);
        }
        finally{
            stateLock.writeLock().unlock();
            stateLock.readLock().unlock();

        }
    }

    /** Handle user actions that directly affect the canvas, such as brushstrokes
     * Must process each request in order
     */
    public synchronized void receivePaintAction(PaintAction paintAction){

        stateLock.writeLock().lock();
        stateLock.readLock().lock();
        try {
            //create if not initialized
            if (!actionPointTracker.containsKey(paintAction.getUserID())) {
                actionPointTracker.put(paintAction.getUserID(), new ActionPointTracker<>());
            }

            //store it in the timeline
            //TODO but only if its undoable...?
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
                    undoPoints++;

                } else if (undoable.getPointType().equals(Undoable.PointType.REDOPOINT)) {
                    temp.isIndex = true;
                    apt.addRedo(temp);
                    redoPoints++;
                }

            }

            paintAction.apply(canvas);
            indexTrackerEnd = temp;
            nodeCounter++;
            if(paintAction instanceof Fill && !(paintAction instanceof EndFill)){
                this.receivePaintAction(new EndFill(0, 0, 0, 0, 0, 0, paintAction.getUserID()));
            }
        }
        finally {
            stateLock.writeLock().unlock();
            stateLock.readLock().unlock();
        }
    }

    /** Clean up the timeline, deleting inaccessible actions such as overwritten actions and those before any undo limits
     * Also deletes unreachable layers of the canvas
     */
    public synchronized void cleanTimeline(boolean emergency){

        if(timeline.isEmpty()) return;
        stateLock.writeLock().lock();
        stateLock.readLock().lock();

        try{
            ArrayList<PaintAction> filteredTimeline = new ArrayList<>();

            int absMin = timeline.size() - CanvasConstants.MAX_TIMELINE_SIZE;

            int maxCommands = emergency ? CanvasConstants.EMERGENCY_MAX_COMMANDS_PER_USER : CanvasConstants.MAX_COMMANDS_PER_USER;
            maxCommands *= Math.max(1,activeConnections.get());

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

            Set<Integer> userIDDeleteList = new HashSet<>();

            int curIndex = 0;
            assert(indexTrackerEnd.indexNumber < timeline.size());
            for(IndexTrackerDLLNode node : indexTrackerDLL){
                assert(node.indexNumber == curIndex);
                boolean add = true;
                if(userIDDeleteList.contains(timeline.get(curIndex).getUserID())){
                    add = false;
                    //splice out the node
                    node.spliceOut();
                    if(node.prev==null){
                        indexTrackerHead = node.next;
                    }
                    if(node.next==null){
                        indexTrackerEnd = node.prev;
                    }
                    //if this was a redopoint, we're done and the user id needs to be removed
                    if(timeline.get(curIndex) instanceof Undoable undoable){
                        assert(undoable.getPointType()!= Undoable.PointType.UNDOPOINT); //we're in here because we're deleting everything up to the next redo point, how did an undo point get here?
                        if(undoable.getPointType()== Undoable.PointType.REDOPOINT){
                            //if theres an unavailable redo with no corresponding available undo, it will be fixed eventually
                            userIDDeleteList.remove(timeline.get(curIndex).getUserID());
                            actionPointTracker.get(timeline.get(curIndex).getUserID()).remove(node);
                            redoPoints--;
                        }
                    }
                }
                else if(curIndex < absMin || undoPoints > maxCommands){ //do not add
                    add = false;
                    //splice out the node
                    node.spliceOut();
                    if(node.prev==null){
                        indexTrackerHead = node.next;
                    }
                    if(node.next==null){
                        indexTrackerEnd = node.prev;
                    }
                    //if this was an undopoint, we must delete everything else with the same user id until we get to the next redo point
                    if(timeline.get(curIndex) instanceof Undoable undoable){
                        if(undoable.getPointType()== Undoable.PointType.UNDOPOINT){
                            userIDDeleteList.add(timeline.get(curIndex).getUserID());
                            actionPointTracker.get(timeline.get(curIndex).getUserID()).remove(node);
                            undoPoints--;
                        }
                    }

                }
                else if(curIndex < earliestUndoLimit){ //do not add
                    add = false;
                    //splice out the node
                    node.spliceOut();
                    if(node.prev==null){
                        indexTrackerHead = node.next;
                    }
                    if(node.next==null){
                        indexTrackerEnd = node.prev;
                    }
                }
                else if(timeline.get(curIndex) instanceof Undoable undoable){
                    if(undoable.getUndoStatus() == Undoable.UndoStatus.OVERWRITTEN){ //see above
                        add = false;
                        node.spliceOut();
                        if(node.prev==null){
                            indexTrackerHead = node.next;
                        }
                        if(node.next==null){
                            indexTrackerEnd = node.prev;
                        }
                    }
                }
                if(add){
                    filteredTimeline.add(timeline.get(curIndex));
                }
                curIndex++;
            }
            indexTrackerDLL=indexTrackerHead;

            curIndex = 0;
            for(IndexTrackerDLLNode node : indexTrackerHead){
                node.indexNumber = curIndex;
                curIndex++;
            }
            //timeline is filtered, indices are updated
            timeline = filteredTimeline;

            //find the first layer that doesnt need to be cleaned
            int firstSafeLayer=-1;
            Iterator<Map.Entry<IndexTrackerDLLNode, Integer>> it = pointToCanvasLayer.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<IndexTrackerDLLNode, Integer> entry = it.next();
                if (entry.getKey().deleted) {
                    it.remove();
                    continue;
                }
                if (firstSafeLayer == -1) {
                    firstSafeLayer = entry.getValue();
                }
                if(firstSafeLayer >=0 ) pointToCanvasLayer.put(entry.getKey(), entry.getValue()-firstSafeLayer); //update the canvas layers
            }
            //delete the first few layers of the canvas
            if(firstSafeLayer > 0 ) canvas.deleteLayers(0,firstSafeLayer);

        }
        finally{
            stateLock.writeLock().unlock();
            stateLock.readLock().unlock();
        }

    }


    //debug and testing methods
    public void debugBreakpoint(){
        return;
    }

    public void actuallyCleanupGarbage(){
        Runtime.getRuntime().gc();
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
