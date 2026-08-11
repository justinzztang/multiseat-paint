package statetracker;

import model.MemorySmartCanvas;
import model.StateTracker;
import model.helpers.ActionPointTracker;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionPointTrackerTest {
    @Test
    public void undoTests() throws Exception {
        ActionPointTracker<Integer> apt = new ActionPointTracker<>();
        apt.addUndo(0);
        assertEquals(0, apt.getLatestUndoPoint());
        assertThrows(Exception.class, apt::getEarliestUnavailableUndoPoint); //nothing unavailable yet
        apt.addRedo(1);
        assertEquals(1, apt.getLatestUnavailableRedoPoint());

        apt.undoUpdate();
        assertEquals(0, apt.getEarliestUnavailableUndoPoint()); //now undo is unavailable
        assertEquals(1, apt.getEarliestRedoPoint()); //redo is available now

        apt.addUndo(0);
        apt.addRedo(1);
        apt.addUndo(2);
        apt.addRedo(3);
        apt.addUndo(4);
        apt.addRedo(5);

        assertEquals(4, apt.getLatestUndoPoint());

        apt.undoUpdate();
        apt.undoUpdate();
        assertEquals(0, apt.getLatestUndoPoint());

    }

}
