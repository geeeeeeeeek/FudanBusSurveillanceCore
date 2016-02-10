import beans.HeadcountRequest;
import beans.HeadcountResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

/**
 * Created by Zhongyi on 2/8/16.
 */
class SurveillanceServiceTest {
    private SurveillanceService surveillanceService;

    public static void main(String[] args) {
        SurveillanceServiceTest testInstance = new SurveillanceServiceTest();

        testInstance.testGetHeadcount();
    }

    public SurveillanceServiceTest() {
        this.surveillanceService = new SurveillanceService();
    }

    private void testGetHeadcount() {
        HeadcountRequest request = new HeadcountRequest();
        BufferedImage image;
        try {
            image = ImageIO.read(new File("testcase/9.bmp"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        request.setImage(image);
        request.setDate(new Date());
        request.setMessage("Testcase 0.");
        request.setCameraIndex(101);
        HeadcountResponse result = surveillanceService.getHeadcount(request);
    }
}
