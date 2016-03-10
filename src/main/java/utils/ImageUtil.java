package utils;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class ImageUtil {
    public static final int RGB_BLACK = 0xff000000, RGB_WHITE = 0xffffffff,
            RGB_RED = 0xffff0000, RGB_GREEN = 0xff00ff00, RGB_BLUE = 0xff0000ff;

    public static BufferedImage getGreyImage(BufferedImage image) {
        int height = image.getHeight(), width = image.getWidth();

        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                binaryImage.setRGB(i, j, rgb);
            }
        }
        return binaryImage;
    }

    public static BufferedImage getDiffedImage(BufferedImage requestImage, BufferedImage backgroundImage, int tolerance) {
        BufferedImage resultImage = deepCopyBufferImage(requestImage);
        int height = resultImage.getHeight(), width = resultImage.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int requestImageRGB = resultImage.getRGB(x, y),
                        backgroundImageRGB = backgroundImage.getRGB(x, y);
                if (Math.abs(getGrayScale(requestImageRGB) - getGrayScale(backgroundImageRGB)) < tolerance) {
                    resultImage.setRGB(x, y, RGB_WHITE);
                } else {
                    resultImage.setRGB(x, y, RGB_BLACK);
                }
            }
        }
        return resultImage;
    }

    public static BufferedImage getDenoisedImage(BufferedImage image, int tolerance) {
        int height = image.getHeight(), width = image.getWidth();
        BufferedImage imageCopy = deepCopyBufferImage(image);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int count = getContrastNeighborsCount(image, x, y);
                if (count > tolerance) imageCopy.setRGB(x, y, RGB_BLACK | (RGB_WHITE - image.getRGB(x, y)));
            }
        }
        return imageCopy;
    }

    public static BufferedImage getSeparatedBlockImage(BufferedImage image, int verticalTolerance, int horizontalTolerance) {
        int height = image.getHeight(), width = image.getWidth();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; ) {
                y = fillSeparatedBlocksVertically(image, x, y, y + verticalTolerance);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; ) {
                x = fillSeparatedBlocksHorizontally(image, y, x, x + horizontalTolerance);
            }
        }
        return image;
    }

    public static BufferedImage getCroppedImage(BufferedImage image, int x, int y, int width, int height) {
        BufferedImage img = image.getSubimage(x, y, width, height);
        BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copyOfImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        return copyOfImage;
    }

    public static BufferedImage getCroppedImage(BufferedImage image, Rectangle rec) {
        return getCroppedImage(image, (int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight());
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
        raster.createWritableChild(0, 0, image.getWidth(), image.getHeight(), 0, 0, null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static int fillSeparatedBlocksVertically(BufferedImage image, int x, int y1, int y2) {
        int height = image.getHeight();

        y2 = Math.min(y2, height);

        if (image.getRGB(x, y1) == RGB_WHITE) return y1 + 1;

        int startFillingFlag = y2;
        for (int i = y2 - 1; i > y1; i--) {
            if (startFillingFlag == y2 && image.getRGB(x, i) == RGB_BLACK) {
                startFillingFlag = i;
            }
            if (startFillingFlag != y2) image.setRGB(x, i, RGB_BLACK);
        }
        return startFillingFlag;
    }

    private static int fillSeparatedBlocksHorizontally(BufferedImage image, int y, int x1, int x2) {
        int width = image.getWidth();

        x2 = Math.min(x2, width);

        if (image.getRGB(x1, y) == RGB_WHITE) return x1 + 1;

        int startFillingFlag = x2;
        for (int i = x2 - 1; i > x1; i--) {
            if (startFillingFlag == x2 && image.getRGB(i, y) == RGB_BLACK) {
                startFillingFlag = i;
            }
            if (startFillingFlag != x2) image.setRGB(i, y, RGB_BLACK);
        }
        return startFillingFlag;
    }

    public static void markRectOnTheImage(BufferedImage image, Rectangle rectangle, int rgb) {
        int height = image.getHeight(), width = image.getWidth(),
                x1 = (int) rectangle.getX(), y1 = (int) rectangle.getY(),
                x2 = Math.min(x1 + (int) rectangle.getWidth() - 1, width),
                y2 = Math.min(y1 + (int) rectangle.getHeight() - 1, height);

        for (int i = x1; i <= x2; i++) {
            image.setRGB(i, y1, rgb);
            image.setRGB(i, y2, rgb);
        }
        for (int i = y1; i <= y2; i++) {
            image.setRGB(x1, i, rgb);
            image.setRGB(x2, i, rgb);
        }
    }

    public static void extendConnectedRegion(BufferedImage image, List<Rectangle> blockList, int[][] blockMap, int x, int y, int index, int depth) {
        int height = image.getHeight(), width = image.getWidth();

        if (image.getRGB(x, y) == RGB_BLACK && blockMap[x][y] == -1) {
            if (index == blockList.size()) {
                blockList.add(new Rectangle(x, y, 1, 1));
            } else {
                blockList.get(index).add(x, y);
            }
            blockMap[x][y] = index;
        } else {
            return;
        }

        int[] offset = new int[]{-1, 0, 1};
        for (int anOffset : offset) {
            for (int anotherOffset : offset) {
                int xx = x + anOffset, yy = y + anotherOffset;
                if (xx >= 0 && xx < width && yy >= 0 && yy < height) {
                    extendConnectedRegion(image, blockList, blockMap, xx, yy, index, depth + 1);
                }
            }
        }
    }

    public static int getValidPixelsCountInRegion(BufferedImage image, Rectangle rectangle) {
        int height = image.getHeight(), width = image.getWidth(),
                x1 = (int) rectangle.getX(), y1 = (int) rectangle.getY(),
                x2 = Math.min(x1 + (int) rectangle.getWidth() - 1, width),
                y2 = Math.min(y1 + (int) rectangle.getHeight() - 1, height);

        int count = 0;
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                if (image.getRGB(i, j) != RGB_WHITE) count += 1;
            }
        }

        return count;
    }

    public static BufferedImage getGradientEdge(BufferedImage image, int tolerance) {
        int height = image.getHeight(), width = image.getWidth();
        BufferedImage imageCopy = deepCopyBufferImage(image);
        if (image.getWidth() < 3 || image.getHeight() < 3) {
            return null;
        }
        double[][] gradientBucket = new double[width][height];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {

                double horizonG =
                        getGrayScale(image.getRGB(i + 1, j - 1)) + getGrayScale(image.getRGB(i + 1, j)) + getGrayScale(image.getRGB(i + 1, j + 1)) -
                                getGrayScale(image.getRGB(i - 1, j - 1)) - getGrayScale(image.getRGB(i - 1, j)) - getGrayScale(image.getRGB(i - 1, j + 1));
                double verticalG =
                        getGrayScale(image.getRGB(i + 1, j + 1)) + getGrayScale(image.getRGB(i, j + 1)) + getGrayScale(image.getRGB(i - 1, j + 1)) -
                                getGrayScale(image.getRGB(i - 1, j - 1)) - getGrayScale(image.getRGB(i, j - 1)) - getGrayScale(image.getRGB(i + 1, j - 1));
                gradientBucket[i][j] = Math.sqrt(
                        Math.pow(horizonG, 2) + Math.pow(verticalG, 2)
                );
                if (gradientBucket[i][j] > max) {
                    max = gradientBucket[i][j];
                }
                if (gradientBucket[i][j] < min) {
                    min = gradientBucket[i][j];
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                gradientBucket[i][j] = ((gradientBucket[i][j] - min) / (max - min)) * 255;
                if (gradientBucket[i][j] > tolerance) {
                    imageCopy.setRGB(i, j, RGB_BLACK);
                } else {
                    imageCopy.setRGB(i, j, RGB_WHITE);
                }
            }
        }
        return imageCopy;
    }
}