package utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class ImageUtil {
    public static BufferedImage getBinaryImage(BufferedImage image) {
        int height = image.getHeight(), width = image.getWidth();

        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                binaryImage.setRGB(i, j, rgb);
            }
        }
        return binaryImage;
    }

    public static BufferedImage getDiffedImage(BufferedImage requestImage, BufferedImage backgroundImage, int tolerance) {
        int height = requestImage.getHeight(), width = requestImage.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int requestImageRGB = requestImage.getRGB(x, y),
                        backgroundImageRGB = backgroundImage.getRGB(x, y);
                if (Math.abs(getGrayScale(requestImageRGB) - getGrayScale(backgroundImageRGB)) < tolerance) {
                    requestImage.setRGB(x, y, 0xffffffff);
                } else {
                    requestImage.setRGB(x, y, 0xff000000);
                }
            }
        }
        return requestImage;
    }

    public static BufferedImage getDenoisedImage(BufferedImage image, int tolerance) {
        int height = image.getHeight(), width = image.getWidth();
        BufferedImage imageCopy = deepCopyBufferImage(image);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int count = getContrastNeighborsCount(image, x, y);
                if (count > tolerance) imageCopy.setRGB(x, y, 0xff000000 | (0xffffffff - image.getRGB(x, y)));
            }
        }
        return imageCopy;
    }

    public static BufferedImage getSeparatedBlockImage(BufferedImage image, int tolerance) {
        int height = image.getHeight(), width = image.getWidth();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; ) {
                y = fillSeparatedBlocksVertically(image, x, y, y + tolerance);
            }
        }
        return image;
    }

    private static int[] getRGBArray(int rgb) {
        int red = (rgb & 0x00ff0000) >> 16;
        int green = (rgb & 0x0000ff00) >> 8;
        int blue = rgb & 0x000000ff;

        return new int[]{red, green, blue};
    }

    private static double getGrayScale(int rgb) {
        int[] rgbArray = getRGBArray(rgb);

        return getGrayScale(rgbArray[0], rgbArray[1], rgbArray[2]);
    }

    private static double getGrayScale(int r, int g, int b) {
        return r * 0.299 + g * 0.587 + b * 0.114;
    }

    /* Length of a must be equal to b. */
    private static double getVectorDistance(int[] a, int[] b) {
        if (a.length != b.length) return -1;

        double sqrDistance = 0;

        for (int i = 0; i < a.length; i++) {
            sqrDistance += Math.pow(a[i] - b[i], 2);
        }

        return Math.sqrt(sqrDistance);
    }

    private static int getContrastNeighborsCount(BufferedImage image, int x, int y) {
        int height = image.getHeight(), width = image.getWidth();

        int[] offset = new int[]{-1, 0, 1};
        int count = 0;
        int ref = image.getRGB(x, y);
        for (int anOffset : offset) {
            for (int anotherOffset : offset) {
                int xx = x + anOffset, yy = y + anotherOffset;
                if (xx >= 0 && xx < width && yy >= 0 && yy < height) {
                    int value = image.getRGB(xx, yy);
                    if (value != ref) count += 1;
                }
            }
        }
        return count;
    }

    private static BufferedImage deepCopyBufferImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static int fillSeparatedBlocksVertically(BufferedImage image, int x, int y1, int y2) {
        int height = image.getHeight();

        y2 = Math.min(y2, height);

        if (image.getRGB(x, y1) == 0xffffffff) return y1 + 1;

        int startFillingFlag = y2;
        for (int i = y2 - 1; i > y1; i--) {
            if (startFillingFlag == y2 && image.getRGB(x, i) == 0xff000000) {
                startFillingFlag = i;
            }
            if (startFillingFlag != y2) image.setRGB(x, i, 0xff000000);
        }
        return startFillingFlag;
    }
}