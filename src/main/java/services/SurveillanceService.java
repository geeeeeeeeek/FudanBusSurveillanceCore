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
    public static String path = "/tmp/cameras/";

    public static HeadcountResponse getHeadcount(String cameraId) {
        HeadcountRequest request = new HeadcountRequest();
        BufferedImage image = null, backgroundImage;
        try {
            image = ImageIO.read(new File(path + cameraId + "/latest.bmp"));
            backgroundImage = ImageIO.read(new File(path + cameraId + "/background.bmp"));
        } catch (Exception e) {
            backgroundImage = image;
        }

        request.setImage(image);
        request.setBackgroundImage(backgroundImage);

        HeadcountResponse response = new HeadcountFunction(request).getResult();
        if (response.getHeadCount() == 0) {
            File file = new File(path + cameraId + "/background.bmp");
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
