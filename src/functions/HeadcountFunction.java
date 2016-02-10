package functions;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import utils.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountFunction {
    private HeadcountRequest mRequest;
    private BufferedImage croppedImage;

    public HeadcountFunction(HeadcountRequest request) {
        mRequest = request;
    }

    public HeadcountResponse getResult() {
        BufferedImage backgroundImage = pickBackgroundImage(mRequest.getImage());
        if (null == backgroundImage) return null;

        final Rectangle VALID_RANGE = new Rectangle(0, 120, 580, 150);

        BufferedImage croppedBackgroundImage = ImageUtil.getCroppedImage(backgroundImage, VALID_RANGE),
                croppedRequestImage = ImageUtil.getCroppedImage(mRequest.getImage(), VALID_RANGE);
        croppedImage = croppedRequestImage;
        /* TODO: Based on experience. Should be calculated by algorithm automatically. */
        final int DIFF_TOLERANCE = 20;
        BufferedImage diffedImage = ImageUtil.getDiffedImage(croppedRequestImage, croppedBackgroundImage, DIFF_TOLERANCE);

        final int CONTRAST_NEIGHBORS_COUNT_TOLERANCE = 4;
        BufferedImage denoisedImage = ImageUtil.getDenoisedImage(diffedImage, CONTRAST_NEIGHBORS_COUNT_TOLERANCE);

        final int VERTICAL_SEPARATION_TOLERANCE = 15, HORIZONTAL_SEPARATION_TOLERANCE = 5;
        BufferedImage separatedBlockImage = ImageUtil.getSeparatedBlockImage(denoisedImage,
                VERTICAL_SEPARATION_TOLERANCE, HORIZONTAL_SEPARATION_TOLERANCE);
        BufferedImage blockMarkedImage = separateBodiesAsBlocks(separatedBlockImage);

        File output = new File("output.bmp");
        try {
            ImageIO.write(blockMarkedImage, "bmp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private BufferedImage pickBackgroundImage(BufferedImage requestImage) {
        try {
            return ImageIO.read(new File("testcase/blank.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private BufferedImage separateBodiesAsBlocks(BufferedImage image) {
        int height = image.getHeight(), width = image.getWidth();

        java.util.List<Rectangle> blockList = new ArrayList<>();
        int[][] blockMap = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blockMap[x][y] = -1;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ImageUtil.extendConnectedRegion(image, blockList, blockMap, x, y, blockList.size(), 0);
            }
        }

        for (Rectangle block : blockList) {
            if (isValidBody(image, block)) {
                ImageUtil.markRectOnTheImage(croppedImage, block, ImageUtil.RGB_GREEN);
            } else {
                ImageUtil.markRectOnTheImage(croppedImage, block, ImageUtil.RGB_RED);
                List<Rectangle> newBlockList = splitLargeBlockHorizontally(image, block);
                for (Rectangle newBlock : newBlockList) {
                    ImageUtil.markRectOnTheImage(croppedImage, newBlock, ImageUtil.RGB_BLUE);
                }
            }

        }
        return croppedImage;
    }

    private boolean isValidBody(BufferedImage image, Rectangle block) {
        double width = block.getWidth(), height = block.getHeight();

        double S = width * height;
        if (S < 800) return false;

        double C = height / width;
        if (C < 1.2 || C > 4) return false;

        double A = ImageUtil.getValidPixelsCountInRegion(image, block) / S;
        if (A < 0.25 || A > 0.9) return false;
        return true;
    }

    private List<Rectangle> splitLargeBlockHorizontally(BufferedImage image, Rectangle rectangle) {
        int height = image.getHeight(), width = image.getWidth(),
                x1 = (int) rectangle.getX(), y1 = (int) rectangle.getY(),
                x2 = Math.min(x1 + (int) rectangle.getWidth() - 1, width),
                y2 = Math.min(y1 + (int) rectangle.getHeight() - 1, height);

//        System.out.print(x1 + "," + x2 + ":  ");
        List<Rectangle> result = new ArrayList<>();
        double[] stripContent = new double[(int) rectangle.getWidth()];
        List<Integer> stripIndex = new ArrayList<>();
        int[] stripCounter = new int[rectangle.width];
        for (int x = x1; x <= x2; x++) {
            Rectangle strip = new Rectangle(x, y1, 1, rectangle.height);
            stripContent[x - x1] = ImageUtil.getValidPixelsCountInRegion(image, strip) / rectangle.getHeight();
            if (x > x1) {
                if (stripContent[x - x1] > stripContent[x - x1 - 1]) {
                    stripCounter[x - x1] = stripCounter[x - x1 - 1] + 1;
                } else {
                    stripCounter[x - x1] = stripCounter[x - x1 - 1] - 1;
                }
            }
        }
        stripIndex.add(x1);
        final int STEP = 10;
        for (int i = STEP; i < rectangle.width - STEP; i++) {
            int leftCounter = 0, rightCounter = 0;
            for (int j = 1; j <= STEP; j++) {
                if (stripContent[i - j + 1] - stripContent[i - j] <= 0) {
                    leftCounter += 1;
                }
                if (stripContent[i + j - 1] - stripContent[i + j] <= 0) {
                    rightCounter += 1;
                }
            }
            if (leftCounter > STEP * 0.6 && rightCounter > STEP * 0.6) {
                stripIndex.add(x1 + i);
//                System.out.print(x1 + i + "(" + leftCounter + "," + rightCounter + ")  ");
            }
        }
//        System.out.println();
        stripIndex.add(x2);

        for (int i = 0; i < stripIndex.size() - 1; i++) {
            for (int j = i + 1; j < stripIndex.size(); j++) {
                Rectangle newRec = new Rectangle(stripIndex.get(i), y1,
                        stripIndex.get(j) - stripIndex.get(i), (int) rectangle.getHeight());
                if (isValidBody(image, newRec)) {
                    result.add(newRec);
                    ImageUtil.markRectOnTheImage(image, newRec, ImageUtil.RGB_BLUE);

                    i = j - 1;
                    break;
                }
            }
        }

        return result;
    }
}