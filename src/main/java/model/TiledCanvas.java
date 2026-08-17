package model;

/**
 * A {@link Canvas} that partitions itself into tiles
 */
public interface TiledCanvas extends Canvas {

    /** @return the tile object at the provided tile coordinates */
    CanvasTile getTile(int tileX, int tileY);

    /** Set the tile object at the provided tile coordinates */
    void setTile(int tileX, int tileY, CanvasTile tile);

    @Override
    TiledCanvas copy();
}