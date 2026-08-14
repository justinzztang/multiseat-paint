package canvas;

import model.*;

import model.Canvas;
import model.constants.CanvasConstants;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CanvasTest {

    @Test
    public void basicConstruction(){
        COWTileCanvas c = new COWTileCanvas(1); //simple constructor
        assertEquals(1, c.getId());
        assertEquals(CanvasConstants.MAX_WIDTH, c.getWidth());
        assertEquals(CanvasConstants.MAX_HEIGHT, c.getHeight());
        assertEquals(1,c.getNumLayers());

        c = new COWTileCanvas(1, 13, 37); //height/width constructor
        assertEquals(13, c.getWidth());
        assertEquals(37, c.getHeight());

    }

    @Test
    public void basicColoring(){
        COWTileCanvas c = new COWTileCanvas(1, 15, 15);
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
    public void copyOperations(){
        COWTileCanvas c = new COWTileCanvas(1, 2*CanvasConstants.TILE_SIDE, 2*CanvasConstants.TILE_SIDE);
        ArrayList<Pair<boolean[][], CanvasTile[][]>> tl = c.getTileLayers();
        assertEquals(1,c.getNumLayers());
        //all marked as original tiles
        assertTrue(tl.get(0).first()[0][0]);
        assertTrue(tl.get(0).first()[1][0]);
        assertTrue(tl.get(0).first()[0][1]);
        assertTrue(tl.get(0).first()[1][1]);

        c.copyTopLayer();
        assertEquals(2,c.getNumLayers());
        //no longer original tiles
        assertFalse(tl.get(1).first()[0][0]);
        assertFalse(tl.get(1).first()[1][0]);
        assertFalse(tl.get(1).first()[0][1]);
        assertFalse(tl.get(1).first()[1][1]);
        //since no changes yet, its still a reference
        assertSame(tl.get(0).second()[0][0],tl.get(1).second()[0][0]);
        assertSame(tl.get(0).second()[1][0],tl.get(1).second()[1][0]);
        assertSame(tl.get(0).second()[0][1],tl.get(1).second()[0][1]);
        assertSame(tl.get(0).second()[1][1],tl.get(1).second()[1][1]);

        c.setPixel(10,10,255,255,255,255); //"fake" update
        assertFalse(tl.get(1).first()[0][0]);
        assertFalse(tl.get(1).first()[1][0]);
        assertFalse(tl.get(1).first()[0][1]);
        assertFalse(tl.get(1).first()[1][1]);
        assertSame(tl.get(0).second()[0][0],tl.get(1).second()[0][0]);
        assertSame(tl.get(0).second()[1][0],tl.get(1).second()[1][0]);
        assertSame(tl.get(0).second()[0][1],tl.get(1).second()[0][1]);
        assertSame(tl.get(0).second()[1][1],tl.get(1).second()[1][1]);

        c.setPixel(10,10,123,123,123,255); //update the first tile

        assertTrue(tl.get(1).first()[0][0]);
        assertFalse(tl.get(1).first()[1][0]);
        assertFalse(tl.get(1).first()[0][1]);
        assertFalse(tl.get(1).first()[1][1]);

        assertNotSame(tl.get(0).second()[0][0],tl.get(1).second()[0][0]);
        assertSame(tl.get(0).second()[1][0],tl.get(1).second()[1][0]);
        assertSame(tl.get(0).second()[0][1],tl.get(1).second()[0][1]);
        assertSame(tl.get(0).second()[1][1],tl.get(1).second()[1][1]);


    }

    @Test
    public void canvasReading(){
        COWTileCanvas c = new COWTileCanvas(1, 2*CanvasConstants.TILE_SIDE, 2*CanvasConstants.TILE_SIDE);
        c.copyTopLayer();
        c.setPixel(10,10,123,123,123,255);
        c.setPixel(40,10,123,123,123,255);
        c.setPixel(10,70,123,123,123,255);
        c.setPixel(100,10,123,123,123,255);
        c.setPixel(40,40,123,123,123,255);

        Color[][] image = c.getTop();
        assertEquals(Color.white, image[0][0]);
        assertEquals(new Color(123,123,123,255), image[10][10]);
        assertEquals(new Color(123,123,123,255), image[10][40]);
        assertEquals(new Color(123,123,123,255), image[70][10]);
        assertEquals(new Color(123,123,123,255), image[10][100]);
        assertEquals(new Color(123,123,123,255), image[10][10]);
        assertEquals(new Color(123,123,123,255), image[40][40]);

        assertEquals(Color.white, c.getColorLayer(0,0,1));
        assertEquals(new Color(123,123,123,255), c.getColorLayer(10,10,1));
    }

    @Test
    public void errorCatching(){
        COWTileCanvas c = new COWTileCanvas(1, 2*CanvasConstants.TILE_SIDE, 2*CanvasConstants.TILE_SIDE);
        assertDoesNotThrow(() -> c.setPixel(1000,1000,123,123,123,255));
    }

}
