package functions;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import utils.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountFunction {
    public HeadcountFunction() {
    }

    public HeadcountResponse getResult(HeadcountRequest request) {
        BufferedImage backgroundImage = pickBackgroundImage(request.getImage());
        if (null == backgroundImage) return null;

        /* TODO: Based on experience. Should be calculated by algorithm automatically. */
        final int DIFF_TOLERANCE = 25;
        BufferedImage diffedImage = ImageUtil.getDiffedImage(request.getImage(), backgroundImage, DIFF_TOLERANCE);

        final int CONTRAST_NEIGHBORS_COUNT_TOLERANCE = 4;
        BufferedImage denoisedImage = ImageUtil.getDenoisedImage(diffedImage, CONTRAST_NEIGHBORS_COUNT_TOLERANCE);

        final int VERTICAL_SEPARATION_TOLERANCE = 10, VERTICAL_FILLING_TOLERANCE = 5;
        BufferedImage separatedBlockImage = ImageUtil.getSeparatedBlockImage(denoisedImage, VERTICAL_SEPARATION_TOLERANCE);


        File output = new File("output.bmp");
        try {
            ImageIO.write(separatedBlockImage, "bmp", output);
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
}
