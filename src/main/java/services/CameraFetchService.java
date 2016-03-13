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
                        Thread.sleep(15000);
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
            System.out.println(" + Fetch service activated at: " + System.currentTimeMillis());
            URL url = new URL(baseUrl);
            System.out.println(" = Connecting to camera at: " + System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + "cm9vdDoxMjM0NTY=");
            InputStream content = (InputStream) connection.getInputStream();
            System.out.println(" = Connection succeeded at: " + System.currentTimeMillis());
            BufferedImage image = ImageIO.read(content);
            File file = new File(SurveillanceService.path + cameraLoc + "_" + cameraId + "/latest.bmp");
            file.getParentFile().mkdirs();
            System.out.println(" = Latest image saved at: " + System.currentTimeMillis());
            ImageIO.write(image, "bmp", file);
            System.out.println("   " + file.getAbsolutePath());

            SurveillanceService.defaultResponse = SurveillanceService.getHeadcount("zj_102");
            System.out.println(" = Image processing finished at: " + System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println(" = Something went wrong at: " + System.currentTimeMillis());
            e.printStackTrace();
        }
        return true;
    }
}
