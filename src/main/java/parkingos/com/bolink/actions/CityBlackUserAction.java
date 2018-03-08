package parkingos.com.bolink.actions;


import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import parkingos.com.bolink.service.CityBlackUserService;
import parkingos.com.bolink.utils.RequestUtil;
import parkingos.com.bolink.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/cityblackuser")
public class CityBlackUserAction {

    Logger logger = Logger.getLogger(CityBlackUserAction.class);

    @Autowired
    private CityBlackUserService cityBlackUserService;

    @RequestMapping(value = "/query")
    public String query(HttpServletRequest request, HttpServletResponse resp) {

        Map<String, String> reqParameterMap = RequestUtil.readBodyFormRequset(request);

        JSONObject result = cityBlackUserService.selectResultByConditions(reqParameterMap);
        //把结果返回页面
        StringUtils.ajaxOutput(resp, result.toJSONString());
        return null;
    }

    @RequestMapping(value = "/edit")
    public String edit(HttpServletRequest request, HttpServletResponse resp) {

        Long id = RequestUtil.getLong(request, "id", -1L);
        Integer state = RequestUtil.getInteger(request, "state", -1);


        JSONObject result = cityBlackUserService.editBlackUser(id,state);
//        把结果返回页面
        StringUtils.ajaxOutput(resp, result.toJSONString());
        return null;
    }

}
