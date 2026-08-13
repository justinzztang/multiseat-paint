package model.helpers;

import java.awt.*;
import java.io.ByteArrayOutputStream;

public class CanvasUtil {

    /** Given a 2d array of colors, write a section of it to a bytestream
     * Precondition: startingX < endingX and startingY < endingY
     */
    public static byte[] colorArraySectionToBytestream(Color[][] image, int startingX, int startingY, int endingX, int endingY) { //TODO specify these are inclusive

        startingX = Math.max(0, startingX);
        startingY = Math.max(0, startingY);

        endingX = Math.min(image[0].length-1, endingX);
        endingY = Math.min(image.length-1, endingY);

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
                baos.write(c.getGreen());
                baos.write(c.getBlue());
                baos.write(c.getAlpha());
            }
        }
        return baos.toByteArray();
    }

}
