package canvas;

import model.Canvas;
import model.CanvasImpl;

import model.MemorySmartCanvas;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class CanvasTest {

    @Test
    public void basicConstruction(){
        MemorySmartCanvas c = new MemorySmartCanvas(1); //simple constructor
        assertEquals(1000, c.getWidth());
        assertEquals(1000, c.getHeight());

        c = new MemorySmartCanvas(1, 13, 37); //height/width constructor
        assertEquals(13, c.getWidth());
        assertEquals(37, c.getHeight());

    }

    @Test
    public void basicColoring(){
        MemorySmartCanvas c = new MemorySmartCanvas(1, 15, 15);
        assertEquals(Color.white, c.getColor(0,0)); //sanity check

        c.setPixel(10,10, 123, 45, 67, 89);
        assertEquals(new Color(123, 45, 67, 89), c.getColor(10,10)); //pixel is set to the right color
        //nothing else is set to the color
        assertEquals(Color.white, c.getColor(11,10));
        assertEquals(Color.white, c.getColor(9,10));
        assertEquals(Color.white, c.getColor(10,11));
        assertEquals(Color.white, c.getColor(10,9));


    }

    @Test
    public void memoryCanvasLayerCopying(){
        MemorySmartCanvas c = new MemorySmartCanvas(1, 3, 3);
        c.copyLayer();
        c.setPixel(2,2, 0, 0, 0, 255);
        assertEquals(new Color(0, 0, 0, 255), c.getColor(2,2));

        //same colors are the same object
        assertEquals(Color.white, c.getColor(1,1));
        assertSame(c.getColor(1, 1), c.getColor(0, 1));
        assertSame(c.getColorLayer(1, 1, 0), c.getColor(1, 1));

        //layer copies still use the same color objects
        assertSame(c.getLayerCopy(0)[0][0], c.getLayer(0)[0][0]);

    }
}
