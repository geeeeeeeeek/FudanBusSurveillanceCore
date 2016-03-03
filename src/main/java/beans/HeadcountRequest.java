package beans;

import sun.jvm.hotspot.utilities.BitMap;

import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountRequest {
    private BufferedImage bitMap;
    private Date date;
    private String message;
    private int cameraIndex;

    public HeadcountRequest() {
    }

    public BufferedImage getImage() {
        return bitMap;
    }

    public void setImage(BufferedImage image) {
        this.bitMap = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
}
