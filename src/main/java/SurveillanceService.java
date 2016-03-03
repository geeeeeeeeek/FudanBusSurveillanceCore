import beans.HeadcountRequest;
import beans.HeadcountResponse;
import functions.HeadcountFunction;
import sun.jvm.hotspot.utilities.BitMap;

/**
 * Created by Zhongyi on 2/8/16.
 */
public class SurveillanceService {

    public HeadcountResponse getHeadcount(HeadcountRequest request) {

        return new HeadcountFunction(request).getResult();
    }
}
