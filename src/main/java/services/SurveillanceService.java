package services;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import functions.HeadcountFunction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class SurveillanceService {
    public static HeadcountResponse defaultResponse;

    public static HeadcountResponse getHeadcount(String cameraId) {
        HeadcountRequest request = new HeadcountRequest();
        BufferedImage image, backgroundImage;
        try {
            image = ImageIO.read(new File("./cameras/" + cameraId + "/latest.bmp"));
            backgroundImage = ImageIO.read(new File("./cameras/" + cameraId + "/background.bmp"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        request.setImage(image);
        request.setBackgroundImage(backgroundImage);

        HeadcountResponse response = new HeadcountFunction(request).getResult();
        if (response.getHeadCount() == 0) {
            File file = new File("./cameras/" + cameraId + "/background.bmp");
            try {
                ImageIO.write(image, "bmp", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        defaultResponse = response;
        return response;
    }
}
