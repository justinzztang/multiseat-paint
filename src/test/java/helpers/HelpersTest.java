package helpers;

import model.CanvasTile;
import model.TileCanvasImpl;
import model.helpers.CanvasUtil;
import model.helpers.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HelpersTest {

    @Test
    public void BoundingBoxTest() {
        //itself
        BoundingBox a = new BoundingBox(1,2,3,4);
        BoundingBox b = BoundingBox.combine(a,a);
        assertEquals(1, b.minX);
        assertEquals(1, b.minX);
        assertEquals(1, b.minX);
        assertEquals(1, b.minX);
        //intersecting
        a = new BoundingBox(1, 1, 3, 3);
        b = new BoundingBox(0, 0, 2, 6);
        BoundingBox combined = BoundingBox.combine(a, b);
        assertEquals(0, combined.minX);
        assertEquals(0, combined.minY);
        assertEquals(3, combined.maxX);
        assertEquals(6, combined.maxY);

        //fully disjoint
        a = new BoundingBox(-5, -5, -1, -1);
        b = new BoundingBox(1, 1, 5, 5);
        combined = BoundingBox.combine(a, b);
        assertEquals(-5, combined.minX);
        assertEquals(-5, combined.minY);
        assertEquals(5, combined.maxX);
        assertEquals(5, combined.maxY);
    }

    @Test
    public void tileSetToBytestreamTest() {
        TileCanvasImpl tci = new TileCanvasImpl(1, CanvasConstants.TILE_SIDE*2, CanvasConstants.TILE_SIDE); // 2 tiles side by side
        CanvasTile[] tiles = { tci.getTile(0, 0), tci.getTile(1, 0) };

        byte[] bytes = CanvasUtil.tileSetToBytestream(tiles);
        assertEquals(2, ((bytes[0] & 0xff) << 8) + (bytes[1] & 0xff));

        int secondTileIndex = 2 + 8 + 64 * 64 * 4;
        int startingX = ((bytes[secondTileIndex] & 0xff) << 8) + (bytes[secondTileIndex + 1] & 0xff);
        assertEquals(64, startingX);
    }

}
