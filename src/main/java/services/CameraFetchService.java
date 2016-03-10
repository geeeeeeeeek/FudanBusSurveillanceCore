package services;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import controllers.SurveillanceController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Zhongyi on 3/3/16.
 */
public class CameraFetchService {
    public void fetch() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    fetchCamera("zj", 102);
                    try {
                        Thread.sleep(30000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }

    private boolean fetchCamera(String cameraLoc, int cameraId) {
        String baseUrl = "http://192.168.0." + cameraId + "/axis-cgi/bitmap/image.bmp";

        try {
            URL url = new URL(baseUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + "cm9vdDoxMjM0NTY=");
            InputStream content = (InputStream) connection.getInputStream();
            BufferedImage image = ImageIO.read(content);
            File file = new File("./cameras/" + cameraLoc + "_" + cameraId + "/latest.bmp");
            file.getParentFile().mkdirs();
            ImageIO.write(image, "bmp", file);
            System.out.println(file.getAbsolutePath());

            SurveillanceService.defaultResponse = SurveillanceService.getHeadcount("zj_102");
            System.out.println(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
