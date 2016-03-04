package beans;

import java.awt.image.BufferedImage;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountRequest {
    private BufferedImage image, backgroundImage;
    private String date;
    private String message;
    private int cameraIndex;

    public HeadcountRequest() {
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCameraIndex() {
        return cameraIndex;
    }

    public void setCameraIndex(int cameraIndex) {
        this.cameraIndex = cameraIndex;
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
}
