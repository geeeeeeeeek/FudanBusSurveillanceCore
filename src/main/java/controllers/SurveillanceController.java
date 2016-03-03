package controllers;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Zhongyi on 3/1/16.
 * Base url: bus.fdu13ss.org/zj
 */
@Controller
@RequestMapping("/cameras")
public class SurveillanceController {
    @RequestMapping(value = "/zj", method = RequestMethod.GET)
    public
    @ResponseBody
    HeadcountResponse getCameraInfo() {
        HeadcountResponse response = new HeadcountResponse();
        response.setBusComing(true);
        response.setHeadCount(2333);
        response.setMessage("Meow meow meow!");
        response.setRandom(true);
        response.setTimeStamp("" + System.currentTimeMillis());
        return response;
    }
}
