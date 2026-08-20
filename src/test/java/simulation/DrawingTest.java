package simulation;

import model.COWTileCanvas;
import model.StateTracker;
import model.constants.CanvasConstants;
import model.controlActions.Undo;
import model.helpers.DrawUtil;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DrawingTest {

    private static int rgba(int r, int g, int b, int a) {
        return DrawUtil.EfficientColor.toColor(r, g, b, a);
    }

    @Test
    public void twoPlayerTest(){

        COWTileCanvas c = new COWTileCanvas(0,CanvasConstants.TILE_SIDE*2,CanvasConstants.TILE_SIDE*2);
        StateTracker st = new StateTracker(0, c);

        //p1 draws, p2 draws over, p1 undos, p2 is still there
        st.receivePaintAction(new BeginStroke(1,1,1,255,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,255,0,0,255, 0));
        st.receivePaintAction(new EndStroke(5,5,1,0));

        st.receivePaintAction(new BeginStroke(1,5,1,0,0,255,255, 1));
        st.receivePaintAction(new Draw(1,5,5,1,1,0,0,255,255, 1));
        st.receivePaintAction(new EndStroke(5,1,1,1));

        st.receiveControlAction(new Undo(0));

        assertEquals(rgba(0,0,255,255), c.getColor(3,3));

        st.receiveControlAction(new Undo(1));
        //handle "interwoven" commands


        st.receivePaintAction(new BeginStroke(1,1,1,255,0,0,255, 0));
        st.receivePaintAction(new BeginStroke(1,1,1,0,0,255,255, 1));

        st.receivePaintAction(new Draw(1,1,5,5,1,255,0,0,255, 0));
        st.receivePaintAction(new Draw(1,1,5,5,1,0,0,255,255, 1));

        st.receivePaintAction(new Draw(5,5,9,9,1,255,0,0,255, 0));
        st.receivePaintAction(new Draw(5,5,9,9,1,0,0,255,255, 1));

        st.receivePaintAction(new EndStroke(9,9,1,0));
        st.receivePaintAction(new EndStroke(9,9,1,1));

        //since id 1 went after each time, it should be them on top
        assertEquals(rgba(0,0,255,255), c.getColor(1,1));
        assertEquals(rgba(0,0,255,255), c.getColor(5,5));
        assertEquals(rgba(0,0,255,255), c.getColor(9,9));


    }

}
