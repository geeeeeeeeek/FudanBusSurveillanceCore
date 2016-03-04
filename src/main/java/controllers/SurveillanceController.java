package controllers;

import beans.HeadcountRequest;
import beans.HeadcountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import services.CameraFetchService;
import services.SurveillanceService;

import java.awt.image.BufferedImage;

/**
 * Created by Zhongyi on 3/1/16.
 * Base url: bus.fdu13ss.org/zj
 */
@Controller
@RequestMapping("/cameras")
public class SurveillanceController {
//    private boolean initialized;

//    @Autowired
//    private CameraFetchService cameraFetchService;
    @Autowired
    private SurveillanceService surveillanceService;

    @RequestMapping(value = "/zj", method = RequestMethod.GET)
    public
    @ResponseBody
    HeadcountResponse getCameraInfo() {
//        if (!initialized) {
//            cameraFetchService.fetch();
//        }

        return surveillanceService.defaultResponse;
    }
}
