package statetracker;

import model.*;

import model.Canvas;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.ActionPointTracker;
import model.helpers.BoundingBox;
import model.paintActions.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class StateTrackerTest {

    @Test
    public void paintActionTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<Integer>> apt = st.getActionPointTracker();
        HashMap<Integer, Integer> ptc = st.getPointToCanvasLayer();

        //user id 0 draws something
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        //should update timeline, canvas, apt, pointtocanvaslayer,
        assertEquals(new BeginStroke(1,1,1,0,0,0,255, 0), tl.get(0));
        assertEquals(new Draw(1,1,5,5,1,0,0,0,255, 0), tl.get(1));
        assertEquals(new EndStroke(5,5,1,0), tl.get(2));

        assertEquals(Color.white, c.getColor(0,0));
        assertEquals(new Color(0,0,0), c.getColor(1,1));
        assertEquals(new Color(0,0,0), c.getColor(3,3));
        assertEquals(2,c.getNumLayers());

        assertNotNull(apt.get(0));
        assertEquals(0, apt.get(0).getLatestUndoPoint());

        assertEquals(0, ptc.get(0));

        st.receiveControlAction(new Undo(0));

        //user id 0 draws same thing but now it overwrites stuff
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        assertEquals(new BeginStroke(1,1,1,0,0,0,255, 0), tl.get(3));
        assertEquals(new Draw(1,1,5,5,1,0,0,0,255, 0), tl.get(4));
        assertEquals(new EndStroke(5,5,1,0), tl.get(5));

        assertEquals(Color.white, c.getColor(0,0));
        assertEquals(new Color(0,0,0), c.getColor(1,1));
        assertEquals(new Color(0,0,0), c.getColor(3,3));
        assertEquals(3,c.getNumLayers());

        assertNotNull(apt.get(0));
        assertEquals(3, apt.get(0).getLatestUndoPoint());

        assertEquals(1, ptc.get(3)); //the layer we were just on


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
        for(int i=0; i<st.getLCC();i++){
            if(tl.get(i) instanceof Undoable undoable)
                assertNotEquals(Undoable.UndoStatus.UNDONE, undoable.getUndoStatus());
        }

    }

    @Test
    public void controlActionTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<Integer>> apt = st.getActionPointTracker();
        HashMap<Integer, Integer> ptc = st.getPointToCanvasLayer();

        //user id 0 draws something
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        st.receiveControlAction(new Undo(0));
        //updates the canvas, apt, lcc
        assertEquals(Color.white, c.getColor(1,1));
        assertEquals(Color.white, c.getColor(3,3));
        assertEquals(Color.white, c.getColor(5,5));

        assertEquals(0,apt.get(0).getEarliestUnavailableUndoPoint());
        assertEquals(2,apt.get(0).getEarliestRedoPoint());

        st.receiveControlAction(new Redo(0));
        assertEquals(Color.black, c.getColor(1,1));
        assertEquals(Color.black, c.getColor(3,3));
        assertEquals(Color.black, c.getColor(5,5));

        assertEquals(0,apt.get(0).getLatestUndoPoint());
        assertEquals(2,apt.get(0).getLatestUnavailableRedoPoint());

        st.receiveControlAction(new Undo(1)); //no effect
        assertEquals(Color.black, c.getColor(1,1));
        assertEquals(Color.black, c.getColor(3,3));
        assertEquals(Color.black, c.getColor(5,5));
        assertEquals(0,apt.get(0).getLatestUndoPoint());
        assertEquals(2,apt.get(0).getLatestUnavailableRedoPoint());

        st.receiveControlAction(new Undo(0));
        //overwrite
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,1,1,0,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,1,1,0));

        assertEquals(3,apt.get(0).getLatestUndoPoint());
        assertEquals(5,apt.get(0).getLatestUnavailableRedoPoint());
        assertTrue(apt.get(0).unavailableUndosEmpty());
        assertTrue(apt.get(0).availableRedosEmpty());


    }

    @Test
    public void syncUpdateTests() throws Exception {

        COWTileCanvas c = new COWTileCanvas(0,10,10);
        StateTracker st = new StateTracker(0, c);
        ArrayList<PaintAction> tl = st.getTimeline();
        HashMap<Integer, ActionPointTracker<Integer>> apt = st.getActionPointTracker();
        HashMap<Integer, Integer> ptc = st.getPointToCanvasLayer();

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

        assertEquals(0, st.lastSyncIndex);
        aabb = st.affectedAreaBoundingBox(true);
        assertEquals(1,aabb.minX);
        assertEquals(1,aabb.minY);
        assertEquals(5,aabb.maxX);
        assertEquals(5,aabb.maxY);
        assertEquals(2, st.lastSyncIndex);

        //no difference
        aabb = st.affectedAreaBoundingBox(true);
        assertEquals(0,aabb.minX);
        assertEquals(0,aabb.minY);
        assertEquals(0,aabb.maxX);
        assertEquals(0,aabb.maxY);
        assertEquals(2, st.lastSyncIndex);


    }
}
