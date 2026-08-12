package model.helpers;

/**
 * Bounding box with points that are more like array indices than points on the grid
 */
public class BoundingBox {
    public int minX;
    public int minY;
    public int maxX;
    public int maxY;

    public BoundingBox(int x0, int y0, int x1, int y1){
        minX = x0;
        minY = y0;
        maxX = x1;
        maxY = y1;
    }

    public static BoundingBox combine(BoundingBox b1, BoundingBox b2){
        int minX = Math.min(b1.minX,b2.minX);
        int minY = Math.min(b1.minY,b2.minY);
        int maxX = Math.max(b1.maxX,b2.maxX);
        int maxY = Math.max(b1.maxY,b2.maxY);

        return new BoundingBox(minX,minY,maxX,maxY);
    }

}
