package statetracker;

import model.Canvas;
import model.CanvasImpl;

import model.MemorySmartCanvas;
import model.StateTracker;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class StateTrackerTest {

    @Test
    public void paintActionTests(){
       StateTracker st = new StateTracker(0, new MemorySmartCanvas(0,10,10));
        st.receivePaintAction(new BeginStroke(1,1,0));
        st.receivePaintAction(new Draw(1,1,1,1,0));
        st.receivePaintAction(new EndStroke(1,1,0));
    }
}
