package beans;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class HeadcountResponse {
    private Integer headCount;
    private Boolean isRandom;
    private String message;
    private Boolean isBusComing;
    private String timeStamp;
    private String image;

    public Integer getHeadCount() {
        return headCount;
    }

    public void setHeadCount(Integer headCount) {
        this.headCount = headCount;
    }

    public Boolean isRandom() {
        return isRandom;
    }

    public void setRandom(Boolean random) {
        isRandom = random;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean isBusComing() {
        return isBusComing;
    }

    public void setBusComing(Boolean busComing) {
        isBusComing = busComing;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
