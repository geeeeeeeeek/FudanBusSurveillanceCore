package functions;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import services.SurveillanceService;
import sun.misc.BASE64Encoder;
import utils.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountFunction {
    private HeadcountRequest mRequest;
    private HeadcountResponse mResponse;


    public HeadcountFunction(HeadcountRequest request) {
        mRequest = request;
    }

    public HeadcountResponse getResult() {
        mResponse = new HeadcountResponse();

        BufferedImage backgroundImage = mRequest.getBackgroundImage();
        if (null == backgroundImage) return null;

        /* TODO: Based on experience. Should be calculated by algorithm automatically. */
        final int DIFF_TOLERANCE = 30;
        BufferedImage diffedImage = ImageUtil.getDiffedImage(mRequest.getImage(), backgroundImage, DIFF_TOLERANCE);
        final Rectangle PEOPLE_RANGE = new Rectangle(0, 120, 580, 180),
                BUS_RANGE = new Rectangle(580, 100, 180, 100),
                ALL_RANGE = new Rectangle(0, 100, 720, 240);

        BufferedImage croppedPeopleImage = ImageUtil.getCroppedImage(diffedImage, PEOPLE_RANGE),
                croppedBusImage = ImageUtil.getCroppedImage(diffedImage, BUS_RANGE),
                croppedAllImage = ImageUtil.getCroppedImage(mRequest.getImage(), ALL_RANGE);

        Integer headcount = -1;
        boolean isBusComing = false;
        try {
            isBusComing = getBusComing(croppedBusImage, new Rectangle(0, 0, 180, 100));
            headcount = getHeadCount(croppedPeopleImage);
        } catch (StackOverflowError e) {
            System.out.println("Stack overflow.");
            mResponse.setMessage("Too many people.");
        }

//        BufferedImage gradientImage = ImageUtil.getGradientEdge(croppedPeopleImage, 20);
        BufferedImage greyImage = ImageUtil.getGreyImage(croppedAllImage);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageInByte = new byte[0];
        try {
            ImageIO.write(greyImage, "bmp", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File output = new File(SurveillanceService.path + "output.bmp");
        try {
            ImageIO.write(greyImage, "bmp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mResponse.setHeadCount(headcount);
        mResponse.setBusComing(isBusComing);
        mResponse.setImage(new BASE64Encoder().encode(imageInByte));
        mResponse.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        return mResponse;
    }

    private boolean getBusComing(BufferedImage diffedImage, Rectangle range) {

        int count = ImageUtil.getValidPixelsCountInRegion(diffedImage, range);
        return count > range.height * range.width / 3;
    }

    private int getHeadCount(BufferedImage diffedImage) {
        final int CONTRAST_NEIGHBORS_COUNT_TOLERANCE = 4;
        BufferedImage denoisedImage = ImageUtil.getDenoisedImage(diffedImage, CONTRAST_NEIGHBORS_COUNT_TOLERANCE);

        final int VERTICAL_SEPARATION_TOLERANCE = 15, HORIZONTAL_SEPARATION_TOLERANCE = 5;
        BufferedImage separatedBlockImage = ImageUtil.getSeparatedBlockImage(denoisedImage,
                VERTICAL_SEPARATION_TOLERANCE, HORIZONTAL_SEPARATION_TOLERANCE);
        return separateBodiesAsBlocks(separatedBlockImage);
    }

    private int separateBodiesAsBlocks(BufferedImage image) {
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

        int headCount = 0;
        for (Rectangle block : blockList) {
            if (isValidBody(image, block)) {
                headCount += 1;
//                ImageUtil.markRectOnTheImage(image, block, ImageUtil.RGB_GREEN);
            } else {
//                ImageUtil.markRectOnTheImage(image, block, ImageUtil.RGB_RED);
                List<Rectangle> newBlockList = splitLargeBlockHorizontally(image, block);
                headCount += newBlockList.size();
//                for (Rectangle newBlock : newBlockList) {
//                    ImageUtil.markRectOnTheImage(image, newBlock, ImageUtil.RGB_BLUE);
//                }
            }

        }
        return headCount;
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
            }
        }
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