package model.helpers;

import model.CanvasTile;
import model.constants.CanvasConstants;

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

        //8 bit integer overflow
        //these coords should never be more than 1000, so 16 bits is fine
        int sxhigh = (startingX >> 8) & 0xFF;
        int sxlow = startingX & 0xFF;

        int syhigh = (startingY >> 8) & 0xFF;
        int sylow = startingY & 0xFF;

        int whigh = (w >> 8) & 0xFF;
        int wlow = w & 0xFF;

        int hhigh = (h >> 8) & 0xFF;
        int hlow = h & 0xFF;

        //big endian
        baos.write(sxhigh);
        baos.write(sxlow);

        baos.write(syhigh);
        baos.write(sylow);

        baos.write(whigh);
        baos.write(wlow);

        baos.write(hhigh);
        baos.write(hlow);

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


    public static byte[] tileSetToBytestream(CanvasTile[] tiles) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //metadata:
        //number of tiles we send out
        //big endian
        baos.write((tiles.length >> 8) & 0xFF); //high bits of length
        baos.write(tiles.length & 0xFF); //low bits of length

        //then for each tile:
        for(CanvasTile tile : tiles){
            int startingX = tile.tileX * CanvasConstants.TILE_SIDE;
            int startingY = tile.tileY * CanvasConstants.TILE_SIDE;
            int w = tile.width;
            int h = tile.height;

            int sxhigh = (startingX >> 8) & 0xFF;
            int sxlow = startingX & 0xFF;

            int syhigh = (startingY >> 8) & 0xFF;
            int sylow = startingY & 0xFF;

            int whigh = (w >> 8) & 0xFF;
            int wlow = w & 0xFF;

            int hhigh = (h >> 8) & 0xFF;
            int hlow = h & 0xFF;

            //startingx
            baos.write(sxhigh);
            baos.write(sxlow);

            //startingy
            baos.write(syhigh);
            baos.write(sylow);

            //width
            baos.write(whigh);
            baos.write(wlow);

            //height
            baos.write(hhigh);
            baos.write(hlow);

            //colordata
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color c = tile.colorArray[y][x];

                    baos.write(c.getRed());
                    baos.write(c.getGreen());
                    baos.write(c.getBlue());
                    baos.write(c.getAlpha());
                }
            }
        }

        return baos.toByteArray();
    }


}
