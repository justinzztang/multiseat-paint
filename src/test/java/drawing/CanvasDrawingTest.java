package drawing;

import model.MemorySmartCanvas;
import model.StateTracker;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class CanvasDrawingTest {
    @Test
    public void drawThenUndoRedo(){
        MemorySmartCanvas canvas = new MemorySmartCanvas(1, 5, 5);
        StateTracker stateTracker = new StateTracker(1, canvas);
        stateTracker.receivePaintAction(new BeginStroke(1,1,0));
        stateTracker.receivePaintAction(new Draw(1,1,1,1,0));
        stateTracker.receivePaintAction(new EndStroke(1,1,0));

        assertEquals(new Color(0,0,0,255), canvas.getColor(1,1)); //TODO temp color

        stateTracker.receiveControlAction(new Undo(0));

        assertEquals(Color.white, canvas.getColor(1,1));

        stateTracker.receiveControlAction(new Redo(0));

        assertEquals(new Color(0,0,0,255), canvas.getColor(1,1));

        stateTracker.receiveControlAction(new Undo(0));

        assertEquals(Color.white, canvas.getColor(1,1));

        stateTracker.receivePaintAction(new BeginStroke(1,1,0));
        stateTracker.receivePaintAction(new Draw(1,1,1,1,0));
        stateTracker.receivePaintAction(new EndStroke(1,1,0));

    }

}
