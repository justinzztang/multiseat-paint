package model.helpers;

import java.awt.*;
import java.io.ByteArrayOutputStream;

public class CanvasUtil {

    /** Given a 2d array of colors, write a section of it to a bytestream
     * Precondition: startingX < endingX and startingY < endingY
     */
    public static byte[] colorArraySectionToBytestream(Color[][] image, int startingX, int startingY, int endingX, int endingY) { //TODO specify these are inclusive
        int w = endingX - startingX + 1;
        int h = endingY - startingY + 1;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //metadata so its possible to know where this section goes
        baos.write(startingX);
        baos.write(startingY);
        baos.write(w);
        baos.write(h);

        for (int y = startingY; y < startingY + h; y++) {
            for (int x = startingX; x < startingX + w; x++) {
                Color c = image[y][x];

                baos.write(c.getRed());
                baos.write(c.getBlue());
                baos.write(c.getGreen());
                baos.write(c.getAlpha());
            }
        }
        return baos.toByteArray();
    }

}
