package statetracker;

import model.*;

import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.ActionPointTracker;
import model.helpers.BoundingBox;
import model.helpers.DrawUtil;
import model.helpers.IndexTrackerDLLNode;
import model.paintActions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class StateTrackerTest {

    private static final int TRANSPARENT = DrawUtil.EfficientColor.toColor(0, 0, 0, 0);
    private static final int BLACK = DrawUtil.EfficientColor.toColor(0, 0, 0, 255);

    @Test
    public void paintActionTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> apt = st.getActionPointTracker();
        HashMap<IndexTrackerDLLNode, Integer> ptc = st.getPointToCanvasLayer();

        //user id 0 draws something
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        //should update timeline, canvas, apt, pointtocanvaslayer,
        assertEquals(new BeginStroke(1,1,1,0,0,0,255, 0), tl.get(0));
        assertEquals(new Draw(1,1,5,5,1,0,0,0,255, 0), tl.get(1));
        assertEquals(new EndStroke(5,5,1,0), tl.get(2));

        assertEquals(TRANSPARENT, c.getColor(0,0));
        assertEquals(BLACK, c.getColor(1,1));
        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(2,c.getNumLayers());

        assertNotNull(apt.get(0));
        assertEquals(0, apt.get(0).getLatestUndoPoint().indexNumber);

        assertEquals(0, ptc.get(new IndexTrackerDLLNode(true,0,0)));

        st.receiveControlAction(new Undo(0));

        //user id 0 draws same thing but now it overwrites stuff
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        assertEquals(new BeginStroke(1,1,1,0,0,0,255, 0), tl.get(3));
        assertEquals(new Draw(1,1,5,5,1,0,0,0,255, 0), tl.get(4));
        assertEquals(new EndStroke(5,5,1,0), tl.get(5));

        assertEquals(TRANSPARENT, c.getColor(0,0));
        assertEquals(BLACK, c.getColor(1,1));
        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(3,c.getNumLayers());

        assertNotNull(apt.get(0));
        assertEquals(3, apt.get(0).getLatestUndoPoint().indexNumber);

        assertEquals(1, ptc.get(new IndexTrackerDLLNode(true,3,3))); //the layer we were just on


        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));
        st.receiveControlAction(new Undo(0));
        for(int i=0; i<st.getLCC().indexNumber;i++){
            if(tl.get(i) instanceof Undoable undoable)
                assertNotEquals(Undoable.UndoStatus.UNDONE, undoable.getUndoStatus());
        }

    }

    @Test
    public void controlActionTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> apt = st.getActionPointTracker();
        HashMap<IndexTrackerDLLNode, Integer> ptc = st.getPointToCanvasLayer();

        //user id 0 draws something
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        st.receiveControlAction(new Undo(0));
        //updates the canvas, apt, lcc
        assertEquals(TRANSPARENT, c.getColor(1,1));
        assertEquals(TRANSPARENT, c.getColor(3,3));
        assertEquals(TRANSPARENT, c.getColor(5,5));

        assertEquals(0,apt.get(0).getEarliestUnavailableUndoPoint().indexNumber);
        assertEquals(2,apt.get(0).getEarliestRedoPoint().indexNumber);

        st.receiveControlAction(new Redo(0));
        assertEquals(BLACK, c.getColor(1,1));
        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(BLACK, c.getColor(5,5));

        assertEquals(0,apt.get(0).getLatestUndoPoint().indexNumber);
        assertEquals(2,apt.get(0).getLatestUnavailableRedoPoint().indexNumber);

        st.receiveControlAction(new Undo(1)); //no effect
        assertEquals(BLACK, c.getColor(1,1));
        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(BLACK, c.getColor(5,5));
        assertEquals(0,apt.get(0).getLatestUndoPoint().indexNumber);
        assertEquals(2,apt.get(0).getLatestUnavailableRedoPoint().indexNumber);

        st.receiveControlAction(new Undo(0));
        //overwrite
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,1,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,1,1,0));

        assertEquals(3,apt.get(0).getLatestUndoPoint().indexNumber);
        assertEquals(5,apt.get(0).getLatestUnavailableRedoPoint().indexNumber);
        assertTrue(apt.get(0).unavailableUndosEmpty());
        assertTrue(apt.get(0).availableRedosEmpty());


    }

    @Test
    public void syncUpdateTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> apt = st.getActionPointTracker();
        HashMap<IndexTrackerDLLNode, Integer> ptc = st.getPointToCanvasLayer();

        //no update
        BoundingBox aabb = st.affectedAreaBoundingBox(true);
        assertEquals(0,aabb.minX);
        assertEquals(0,aabb.minY);
        assertEquals(0,aabb.maxX);
        assertEquals(0,aabb.maxY);


        //user 0 draws a diagonal line with thickness 1
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        assertEquals(0, st.lastSyncIndex.indexNumber);
        aabb = st.affectedAreaBoundingBox(true);
        assertEquals(1,aabb.minX);
        assertEquals(1,aabb.minY);
        assertEquals(5,aabb.maxX);
        assertEquals(5,aabb.maxY);
        assertEquals(2, st.lastSyncIndex.indexNumber);

        //no difference
        aabb = st.affectedAreaBoundingBox(true);
        assertEquals(0,aabb.minX);
        assertEquals(0,aabb.minY);
        assertEquals(0,aabb.maxX);
        assertEquals(0,aabb.maxY);
        assertEquals(2, st.lastSyncIndex.indexNumber);


    }

    @Test
    public void garbageCollectionTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<IndexTrackerDLLNode>> apt = st.getActionPointTracker();
        HashMap<IndexTrackerDLLNode, Integer> ptc = st.getPointToCanvasLayer();

        //user 0 draws 5 times
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,2,2,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(2,2,1,0));

        st.receivePaintAction(new BeginStroke(2,2,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(2,2,3,3,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(3,3,1,0));

        st.receivePaintAction(new BeginStroke(3,3,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(3,3,4,4,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(4,4,1,0));

        st.receivePaintAction(new BeginStroke(4,4,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(4,4,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        st.receivePaintAction(new BeginStroke(5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(5,5,6,6,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(6,6,1,0));

        //user 0 undos 3 times
        st.receiveControlAction(new Undo(0));
        st.receiveControlAction(new Undo(0));
        st.receiveControlAction(new Undo(0));

        //user 0 draws something new, and overwrites the old stuff
        st.receivePaintAction(new BeginStroke(3,3,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(3,3,4,4,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(4,4,1,0));

        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(BLACK, c.getColor(4,4));
        assertEquals(TRANSPARENT, c.getColor(5,5));
        assertEquals(TRANSPARENT, c.getColor(6,6));

        st.cleanTimeline(false);

        //nothing should have changed
        assertEquals(BLACK, c.getColor(3,3));
        assertEquals(BLACK, c.getColor(4,4));
        assertEquals(TRANSPARENT, c.getColor(5,5));
        assertEquals(TRANSPARENT, c.getColor(6,6));



    }
}
