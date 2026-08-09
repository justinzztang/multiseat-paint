package canvas;

import model.Canvas;
import model.CanvasImpl;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanvasTest {

    @Test
    public void basicConstruction(){
        CanvasImpl c = new CanvasImpl(1);
        assertEquals(1000, c.getWidth());
        assertEquals(1000, c.getHeight());

        c = new CanvasImpl(1, 13, 37);
        assertEquals(13, c.getWidth());
        assertEquals(37, c.getHeight());

    }

    @Test
    public void basicColoring(){
        CanvasImpl c = new CanvasImpl(1);
        assertEquals(Color.white, c.getColor(0,0));

        c.setPixel(10,10, 123, 45, 67, 89);
        assertEquals(new Color(123, 45, 67, 89), c.getColor(10,10));

    }
}
