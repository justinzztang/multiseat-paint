package model;

import java.awt.*;

/**
 * A {@link Canvas} that stores several canvas layers
 * @param <C> the implementation of stored canvas layers
 */

public interface LayeredCanvas<C extends Canvas> extends Canvas{

    /** @return the number of stored layers*/
    int getNumLayers();

    /** Delete the canvas layer stored at the provided index
     * @param layer The index of the layer to delete
     */
    void deleteLayer(int layer);

    /** Delete the canvas layers from the starting index (inclusive) to the ending index (exclusive)
     */
    void deleteLayers(int start, int end);

    /** Create a copy of the top canvas layer and add it to the top*/ //TODO kind of ambiguous wording
    void copyTopLayer();

    /** Set the top canvas layer to the provided canvas */
    void setTopLayer(C layer);

    /** @return the canvas layer stored at the provided index
     * @param layer The index of the layer to get
     */
    C getLayer(int layer);

    /** @return the top canvas layer
     */
    C getTop();
}
