package parkingos.com.bolink.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.dao.mybatis.mapper.ParkInfoMapper;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.enums.FieldOperator;
import parkingos.com.bolink.models.*;
import parkingos.com.bolink.qo.PageOrderConfig;
import parkingos.com.bolink.qo.SearchBean;
import parkingos.com.bolink.service.CityParkService;
import parkingos.com.bolink.service.SaveLogService;
import parkingos.com.bolink.service.SupperSearchService;
import parkingos.com.bolink.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CityParkServiceImpl implements CityParkService {

    Logger logger = Logger.getLogger(CityParkServiceImpl.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private SupperSearchService<ComInfoTb> supperSearchService;
    @Autowired
    private CommonMethods commonMethods;
    @Autowired
    private ParkInfoMapper parkInfoMapper;
    @Autowired
    private SaveLogService saveLogService;

    @Override
    public JSONObject selectResultByConditions(Map<String, String> reqmap) {
        String str = "{\"total\":0,\"page\":1,\"rows\":[]}";
        JSONObject result = JSONObject.parseObject(str);

        int count = 0;
        List<ComInfoTb> list = null;
        List<Map<String, Object>> resList = new ArrayList<>();

        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setState(0);

//        Map searchMap = supperSearchService.getGroupOrCitySearch(liftRodTb,reqmap);
//        LiftRodTb baseQuery =(LiftRodTb)searchMap.get("base");
//        List<SearchBean> supperQuery =(List<SearchBean>)searchMap.get("supper");
//        PageOrderConfig config = (PageOrderConfig)searchMap.get("config");
//
//        count = commonDao.selectCountByConditions(baseQuery,supperQuery);
//        if(count>0){
//            list = commonDao.selectListByConditions(baseQuery,supperQuery,config);
//            if (list != null && !list.isEmpty()) {
//                for (LiftRodTb liftRodTb1 : list) {
//                    OrmUtil<LiftRodTb> otm = new OrmUtil<>();
//                    Map<String, Object> map = otm.pojoToMap(liftRodTb1);
//                    resList.add(map);
//                }
//                result.put("rows", JSON.toJSON(resList));
//            }
//        }
        String groupidStart = reqmap.get("groupid_start");
        if(!Check.isEmpty(groupidStart)){
            comInfoTb.setGroupid(Long.parseLong(groupidStart));
        }
        String groupid = reqmap.get("groupid");
        String cityid = reqmap.get("cityid");
        System.out.println("=====groupid:" + groupid + "===cityid:" + cityid);

        Map searchMap = supperSearchService.getBaseSearch(comInfoTb, reqmap);
        logger.info(searchMap);
        if (searchMap != null && !searchMap.isEmpty()) {
            ComInfoTb baseQuery = (ComInfoTb) searchMap.get("base");
            List<SearchBean> supperQuery = null;
            if (searchMap.containsKey("supper"))
                supperQuery = (List<SearchBean>) searchMap.get("supper");
            PageOrderConfig config = null;
            if (searchMap.containsKey("config"))
                config = (PageOrderConfig) searchMap.get("config");

            List parks = new ArrayList();

            if (groupid != null && !"".equals(groupid)) {
                parks = commonMethods.getParks(Long.parseLong(groupid));
            } else if (cityid != null && !"".equals(cityid)) {
                parks = commonMethods.getparks(Long.parseLong(cityid));
            }

            System.out.println("=======parks:" + parks);


            if (parks == null || parks.size() < 1) {
                return result;
            }

            //封装searchbean  城市和集团下所有车场
            SearchBean searchBean = new SearchBean();
            searchBean.setOperator(FieldOperator.CONTAINS);
            searchBean.setFieldName("id");
            searchBean.setBasicValue(parks);

//            SearchBean searchBean1 = new SearchBean();
//            searchBean1.setOperator(FieldOperator.CONTAINS);
//            searchBean1.setFieldName("state");
//            ArrayList stateList = new ArrayList<Integer>();
//            stateList.add(0);
//            stateList.add(2);
//            searchBean1.setBasicValue(stateList);

            if (supperQuery == null) {
                supperQuery = new ArrayList<>();
            }
            supperQuery.add(searchBean);
//            supperQuery.add(searchBean1);

            count = commonDao.selectCountByConditions(baseQuery, supperQuery);
            if (count > 0) {
                list = commonDao.selectListByConditions(baseQuery, supperQuery, config);
                if (list != null && !list.isEmpty()) {
                    for (ComInfoTb comInfoTb1 : list) {
                        OrmUtil<ComInfoTb> otm = new OrmUtil<>();
                        Map<String, Object> map = otm.pojoToMap(comInfoTb1);

                        Long parkid = comInfoTb1.getId();

                        List<HashMap<String, Object>> tokenList = getParkStatusbc(parkid);
                        if(tokenList!=null&&tokenList.size()>0&&tokenList.get(0).get("beat_time")!=null){
                            map.put("beat_time",tokenList.get(0).get("beat_time"));
                            //更新车场的心跳时间
//                            comInfoTb1.setBeatTime(Long.parseLong(tokenList.get(0).get("beat_time")+""));
//                            int update = commonDao.updateByPrimaryKey(comInfoTb1);
//                            logger.error("更新车场心跳时间"+update);
                        }


//                        OrderTb newOrder = new OrderTb();
//                        newOrder.setComid(parkid);
//                        newOrder.setState(0);
//                        newOrder.setIshd(0);
//
//                        int total = commonDao.selectCountByConditions(newOrder);
////
//                        logger.error("=======>>>在场车辆"+total);
//
//
//                            Integer parking_total = 0;
//                            if(comInfoTb1.getParkingTotal()!= null){
//                                parking_total=comInfoTb1.getParkingTotal();//车场车位数
//                            }
//                            Integer shareNumber = 0;
//                            if(comInfoTb1.getShareNumber() != null){
//                                shareNumber=comInfoTb1.getShareNumber();//车场车位分享数
//                            }
//                            Integer parktotal = 0;
//                            if(shareNumber > 0){
//                                parktotal = shareNumber;
//                            }else{
//                                parktotal = parking_total;
//                            }
//
//                            Integer blank = parktotal -total;
//                            if(blank<=0){
//                                blank = 0;
//                            }
//                            map.put("blank",blank);

                            resList.add(map);
                    }
                    result.put("rows", JSON.toJSON(resList));
                }
            }
        }
        result.put("total", count);
        result.put("page", Integer.parseInt(reqmap.get("page")));
        logger.error("============>>>>>返回数据" + result);
        return result;
    }

    @Override
    public JSONObject createPark(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("state", 0);
        result.put("msg", "创建车场失败");

        String bolinkid = RequestUtil.getString(request, "bolink_id");
        if (bolinkid != null && !"".equals(bolinkid)) {
            ComInfoTb infoTb = new ComInfoTb();
            infoTb.setBolinkId(bolinkid);
            infoTb.setState(0);
            int infoCount = commonDao.selectCountByConditions(infoTb);
            if(infoCount>0){
                result.put("msg", "创建失败,泊链车场编号重复");
                return result;
            }

            if (Check.isNumber(bolinkid)) {
                //验证填写的泊链编号在yun是不是重复
                Long comid = Long.parseLong(bolinkid);
                ComInfoTb comInfoTb = new ComInfoTb();
                comInfoTb.setId(comid);
                int count = commonDao.selectCountByConditions(comInfoTb);
                if (count > 0) {
                    result.put("msg", "创建失败,云平台编号重复");
                    return result;
                }
            }
        }

        Long id = RequestUtil.getLong(request, "id", -1L);
        Long cityid = RequestUtil.getLong(request, "cityid", -1L);

        Long groupId = RequestUtil.getLong(request, "groupid", -1L);
        if (groupId == -1) {
            groupId = RequestUtil.getLong(request, "group_id", -1L);
        }

        String nickname = StringUtils.decodeUTF8(RequestUtil.getString(request,"nickname1"));
        Long uin = RequestUtil.getLong(request, "loginuin", -1L);

        String company = RequestUtil.processParams(request, "company_name");
        company = company.replace("\r", "").replace("\n", "");
        String address = StringUtils.decodeUTF8(RequestUtil.processParams(request, "address"));
        address = address.replace("\r", "").replace("\n", "");
        String mobile = RequestUtil.processParams(request, "mobile");
        if (mobile.length() > 15) {
            result.put("msg", "手机号输入有误");
            return result;
        }
        Integer parking_total = RequestUtil.getInteger(request, "parking_total", 0);
        Integer state = RequestUtil.getInteger(request, "state", 0);
        Integer city = RequestUtil.getInteger(request, "city", 0);
        Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
        Double latitude = RequestUtil.getDouble(request, "latitude", 0d);


        if (longitude == 0 || latitude == 0) {
            result.put("msg", "请标注地理位置");
            return result;
        }

        //判断地图位置是否冲突
        ComInfoTb newCominfoTb = new ComInfoTb();

//        logger.info("park zuobiao:"+longitude+"~~"+new BigDecimal(longitude).setScale(6, BigDecimal.ROUND_HALF_UP));
        newCominfoTb.setLongitude(new BigDecimal(longitude).setScale(6, BigDecimal.ROUND_HALF_UP));
        newCominfoTb.setLatitude(new BigDecimal(latitude).setScale(6, BigDecimal.ROUND_HALF_UP));
        int count = commonDao.selectCountByConditions(newCominfoTb);
        if (count > 0) {
            result.put("msg", "地理位置冲突，请重新标注!");
            return result;
        }


        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setState(state);
        comInfoTb.setCompanyName(company);
        comInfoTb.setLatitude(new BigDecimal(latitude));
        comInfoTb.setLongitude(new BigDecimal(longitude));
        comInfoTb.setAddress(address);
        comInfoTb.setCity(city);
        comInfoTb.setMobile(mobile);
        comInfoTb.setParkingTotal(parking_total);
        comInfoTb.setBolinkId(bolinkid);

        if (id == -1) {
//            if(groupId==null||groupId==-1){
//                groupId = RequestUtil.getLong(request,"group_id",-1L);
//                if(groupId==null||groupId==-1){
//                    result.put("msg","请选择运营集团");
//                    return result;
//                }
//            }
            //获取id
            Long comid = commonDao.selectSequence(ComInfoTb.class);
            comInfoTb.setId(comid);
            comInfoTb.setGroupid(groupId);
            comInfoTb.setCityid(cityid);
            //添加自动生成车场16位秘钥的逻辑
            String ukey = StringUtils.createRandomCharData(16);
            comInfoTb.setUkey(ukey);
            comInfoTb.setCreateTime(System.currentTimeMillis() / 1000);


//            int insert = commonDao.insert(comInfoTb);
//            if (insert == 1) {
//                result.put("state", 1);

            List<Map<String, Object>> unionInfoList = commonDao.getObjectBySql("select oc.union_id, oc.ukey union_key, og.operatorid operator_id from org_city_merchants oc " +
                    "left outer join org_group_tb og on oc.id = og.cityid " +
                    "where og.id = " + groupId);
            String operator_id = "";
            String union_key = "";
            String union_id = "";
            if (unionInfoList != null && unionInfoList.size() > 0) {
                if (unionInfoList.get(0).get("operator_id") != null) {
                    operator_id = unionInfoList.get(0).get("operator_id") + "";
                }
                union_key = unionInfoList.get(0).get("union_key") + "";
                union_id = unionInfoList.get(0).get("union_id") + "";
            } else {
                //查询没有集团编号的 车场
                unionInfoList = commonDao.getObjectBySql("select oc.union_id, oc.ukey union_key from org_city_merchants oc " +
                        "where oc.id = " + cityid);
                union_key = unionInfoList.get(0).get("union_key") + "";
                union_id = unionInfoList.get(0).get("union_id") + "";
            }

            //判断车场是否要上传到泊链,如果没有写bolinkid,那么上传
            if (bolinkid == null || "".equals(bolinkid)) {
                //查询他的厂商编号以及服务商编号 (查询有集团编号的)
//                List<Map<String, Object>> unionInfoList = commonDao.getObjectBySql("select oc.union_id, oc.ukey union_key, og.operatorid operator_id from org_city_merchants oc " +
//                        "left outer join org_group_tb og on oc.id = og.cityid " +
//                        "left outer join com_info_tb co on co.groupid = og.id " +
//                        "where co.id = " + comid);
                int uploadCount = 0;
                int unUploadCount = 0;
                //String url = "https://127.0.0.1/api-web/park/addpark";
                String url = CustomDefind.UNIONIP + "park/addpark";
                //String url = "https://s.bolink.club/unionapi/park/addpark";
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("park_id", comid);
                paramMap.put("name", company);
                paramMap.put("address", address);
                paramMap.put("phone", mobile);
                paramMap.put("lng", longitude);
                paramMap.put("lat", latitude);
                paramMap.put("total_plot", parking_total);
                paramMap.put("empty_plot", parking_total);
                paramMap.put("price_desc", getPrice(comid));
                paramMap.put("remark", "");
                paramMap.put("union_id", union_id);
//                    paramMap.put("server_id", server_id);
                paramMap.put("operator_id", operator_id);
                paramMap.put("rand", Math.random());
                String ret = "";
                try {
                    logger.error(url+paramMap);
                    String linkParams = StringUtils.createLinkString(paramMap) + "key=" + union_key;
                    System.out.println(linkParams);
                    String sign = StringUtils.MD5(linkParams).toUpperCase();
                    logger.error(sign);
                    paramMap.put("sign", sign);
                    //param = DesUtils.encrypt(param,"NQ0eSXs720170114");
                    String param = StringUtils.createJson(paramMap);
                    logger.error(param);
                    ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
                    JSONObject object = JSONObject.parseObject(ret);
                    if (object != null) {
                        Integer uploadState = Integer.parseInt(object.get("state") + "");
                        System.out.println("chenbowen:" + uploadState);
                        if (uploadState == 1) {
                            ComInfoTb comInfoTb1 = new ComInfoTb();
                            comInfoTb1.setUploadUnionTime(System.currentTimeMillis() / 1000L);
                            comInfoTb1.setUnionState(2);
                            comInfoTb1.setId(comid);
                            uploadCount = commonDao.updateByPrimaryKey(comInfoTb1);
                            int insert = commonDao.insert(comInfoTb);
                            logger.error("上传车场个数:" + uploadCount + "云平台新建车场:" + insert);
                            result.put("state", 1);
                            result.put("msg", "新建车场成功,上传到泊链成功");

                            if(groupId>0){
                                ParkLogTb parkLogTb = new ParkLogTb();
                                parkLogTb.setOperateUser(nickname);
                                parkLogTb.setOperateTime(System.currentTimeMillis()/1000);
                                parkLogTb.setOperateType(1);
                                parkLogTb.setContent(uin+"("+nickname+")"+"新建了车场"+comid+company);
                                parkLogTb.setType("parkinfo");
                                parkLogTb.setGroupId(groupId);
                                saveLogService.saveLog(parkLogTb);
                            }


                            return result;
                        } else {
                            result.put("state", 0);
                            logger.error(object.get("errmsg"));
                            String errmsg = object.get("errmsg") + "";
                            if (errmsg.contains("运营商编号")) {
                                result.put("msg", "新建车场失败,泊链运营集团编号错误");
                                return result;
                            }
                            result.put("msg", "新建车场失败,上传到泊链失败");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.error(ret);
            }else {//如果填写了泊链车场编号,证明泊链那边有车场,去验证所填泊链编号是否正确
                String url = CustomDefind.UNIONIP + "park/checkparkid";
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("park_id", bolinkid);
                paramMap.put("union_id",union_id);
                String param = StringUtils.createJson(paramMap);
                try{
                    String ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
                    JSONObject object = JSONObject.parseObject(ret);
                    Integer checkstate = Integer.parseInt(object.get("state") + "");
                    if(checkstate==1){//泊链车场编号正确
                        int insert = commonDao.insert(comInfoTb);
                        logger.error("填写了泊链编号进行校验新建车场:"+insert);
                        result.put("state", 1);
                        result.put("msg","新建车场成功");
                        return result;
                    }else{
                        result.put("state", 0);
                        result.put("msg","新建车场失败,泊链车场编号错误");
                        return result;
                    }
                }catch (Exception e){
                    logger.error("去泊链查询operatorid是否存在出现异常");
                }

            }
//            }
        } else {
            comInfoTb.setId(id);
            comInfoTb.setUpdateTime(System.currentTimeMillis() / 1000);
            int update = commonDao.updateByPrimaryKey(comInfoTb);
            if (update == 1) {
                result.put("state", 1);
                result.put("msg", "修改车场成功");

                if(groupId>0){
                    ParkLogTb parkLogTb = new ParkLogTb();
                    parkLogTb.setOperateUser(nickname);
                    parkLogTb.setOperateTime(System.currentTimeMillis()/1000);
                    parkLogTb.setOperateType(2);
                    parkLogTb.setContent(uin+"("+nickname+")"+"修改了车场"+id);
                    parkLogTb.setType("parkinfo");
                    parkLogTb.setGroupId(groupId);
                    saveLogService.saveLog(parkLogTb);
                }
            }
        }
        return result;
    }

    @Override
    public JSONObject editpark(ComInfoTb comInfoTb) {
        JSONObject result = new JSONObject();
        result.put("state", 0);
        result.put("msg", "修改车场失败");

        Double longitude = comInfoTb.getLongitude().doubleValue();
        Double latitude = comInfoTb.getLatitude().doubleValue();
        if (longitude == 0 || latitude == 0) {
            result.put("msg", "请标注地理位置");
            return result;
        }

        //判断地图位置是否冲突
        ComInfoTb newCominfoTb = new ComInfoTb();
        newCominfoTb.setLongitude(new BigDecimal(longitude).setScale(6, BigDecimal.ROUND_HALF_UP));
        newCominfoTb.setLatitude(new BigDecimal(latitude).setScale(6, BigDecimal.ROUND_HALF_UP));
        int count = commonDao.selectCountByConditions(newCominfoTb);
        if (count > 0) {
            result.put("msg", "地理位置冲突，请重新标注!");
            return result;
        }

        //重新处理参数 封装
        String company = comInfoTb.getCompanyName();
        company = company.replace("\r", "").replace("\n", "");
        comInfoTb.setCompanyName(company);
        String address = comInfoTb.getAddress();
        address = address.replace("\r", "").replace("\n", "");
        comInfoTb.setAddress(address);

        if (comInfoTb.getId() != null && comInfoTb.getId() != -1) {
            comInfoTb.setUpdateTime(System.currentTimeMillis() / 1000);
            int update = commonDao.updateByPrimaryKey(comInfoTb);
            if (update == 1) {
                result.put("state", 1);
                result.put("msg", "修改车场成功");
            }
        }

        return result;
    }

    @Override
    public JSONObject deletepark(ComInfoTb comInfoTb) {
        JSONObject result = new JSONObject();
        result.put("state", 0);
        result.put("msg", "删除失败");

        int count = commonDao.updateByPrimaryKey(comInfoTb);
        if (count == 1) {
            result.put("state", 1);
            result.put("msg", "删除成功");
        }
        return result;
    }

    @Override
    public JSONObject setpark(Long comid) {

        JSONObject result = new JSONObject();

        List<Map<String, Object>> parkList = commonDao.getObjectBySql("select * from com_info_tb where id=" + comid);
//        Integer parking_type = 0;
        String info = "";
        if (parkList != null) {
            info = "名称：" + parkList.get(0).get("company_name") + "，地址：" + parkList.get(0).get("address") + "<br/>创建时间："
                    + TimeTools.getTime_yyyyMMdd_HHmm((Long) parkList.get(0).get("create_time") * 1000) + "，车位总数：" + parkList.get(0).get("parking_total")
                    + "，分享车位：" + parkList.get(0).get("share_number") + "，经纬度：(" + parkList.get(0).get("longitude") + "," + parkList.get(0).get("latitude") + ")";
//            parking_type = (Integer)parkList.get(0).get("parking_type");
            result.put("info", info);
        }
        return result;
    }

    @Override
    public JSONObject resetParkData(Long comid, Long loginuin, String password) {
        JSONObject result = new JSONObject();
        result.put("state",0);
        result.put("msg","重置失败");

        //根据登录厂商的账号查询密码 进行匹配
        UserInfoTb userInfoTb = new UserInfoTb();
        userInfoTb.setId(loginuin);
        userInfoTb = (UserInfoTb)commonDao.selectObjectByConditions(userInfoTb);
        if(userInfoTb!=null&&userInfoTb.getPassword()!=null){
            if (!password.equals(userInfoTb.getPassword())){
                result.put("msg","密码错误");
                return result;
            }
        }else{
            result.put("msg","用户不存在");
            return result;
        }

        //密码正确，根据车场编号搜索需要重置的数据
        try {
            OrderTb orderTb = new OrderTb();
            orderTb.setComid(comid);
            orderTb.setIshd(0);
            PageOrderConfig pageOrderConfig = new PageOrderConfig();
            pageOrderConfig.setPageInfo(null,null);
            List<OrderTb> orderTbList = commonDao.selectListByConditions(orderTb,pageOrderConfig);
            if(orderTbList!=null&&orderTbList.size()>0){
                for(OrderTb order:orderTbList){
                    order.setIshd(1);
                    commonDao.updateByPrimaryKey(order);
                }
            }
            logger.error(comid+"重置订单完成,开始重置抬杆数据");
            LiftRodTb liftRodTb = new LiftRodTb();
            liftRodTb.setComid(comid);
            liftRodTb.setIsDelete(0);
            List<LiftRodTb> liftRodTbList = commonDao.selectListByConditions(liftRodTb,pageOrderConfig);
            if(liftRodTbList!=null&&liftRodTbList.size()>0){
                for(LiftRodTb liftRod:liftRodTbList){
                    liftRod.setIsDelete(1);
                    commonDao.updateByPrimaryKey(liftRod);
                }
            }
            logger.error(comid+"重置抬杆数据完成");
        }catch (Exception e){
            result.put("msg","重置失败，重置过程出现异常");
            return result;
        }
        result.put("state",1);
        result.put("msg","重置数据成功！");
        return result;
    }

    private String getPrice(Long parkId) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        //开始小时
        int bhour = calendar.get(Calendar.HOUR_OF_DAY);
        List<Map<String, Object>> priceList = commonDao.getObjectBySql("select * from price_tb where comid=" + parkId + " and state=0 and pay_type=0 order by id desc");
        if (priceList == null || priceList.size() == 0) {//没有按时段策略
            //查按次策略
//            priceList=daService.getAll("select * from price_tb where comid=? " +
//                    "and state=? and pay_type=? order by id desc", new Object[]{parkId,0,1});
            priceList = commonDao.getObjectBySql("select * from price_tb where comid=" + parkId + " and state=0 and pay_type=1 order by id desc");
            ;
            if (priceList == null || priceList.size() == 0) {//没有按次策略，返回提示
                return "0元/次";
            } else {//有按次策略，直接返回一次的收费
                Map timeMap = priceList.get(0);
                Integer unit = (Integer) timeMap.get("unit");
                if (unit != null && unit > 0) {
                    if (unit > 60) {
                        String t = "";
                        if (unit % 60 == 0)
                            t = unit / 60 + "小时";
                        else
                            t = unit / 60 + "小时 " + unit % 60 + "分钟";
                        return timeMap.get("price") + "元/" + t;
                    } else {
                        return timeMap.get("price") + "元/" + unit + "分钟";
                    }
                } else {
                    return timeMap.get("price") + "元/次";
                }
            }
            //发短信给管理员，通过设置好价格
        } else {//从按时段价格策略中分拣出日间和夜间收费策略
            if (priceList.size() > 0) {
                //logger.error(priceList);
                for (Map map : priceList) {
                    Integer btime = (Integer) map.get("b_time");
                    Integer etime = (Integer) map.get("e_time");
                    Double price = Double.valueOf(map.get("price") + "");
                    Double fprice = Double.valueOf(map.get("fprice") + "");
                    Integer ftime = (Integer) map.get("first_times");
                    if (ftime != null && ftime > 0) {
                        if (fprice > 0)
                            price = fprice;
                    }
                    if (btime < etime) {//日间
                        if (bhour >= btime && bhour < etime) {
                            return price + "元/" + map.get("unit") + "分钟";
                        }
                    } else {
                        if (bhour >= btime || bhour < etime) {
                            return price + "元/" + map.get("unit") + "分钟";
                        }
                    }
                }
            }
        }
        return "0.0元/小时";
    }


    private List<HashMap<String, Object>> getParkStatusbc(Long parkid) {
        List<HashMap<String, Object>> parkState = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> parkLoginList = parkInfoMapper.getParkLogin(parkid + "");
        if (parkLoginList != null && parkLoginList.size() > 0) {
            for (HashMap<String, Object> loginmap : parkLoginList){
                HashMap<String, Object> parkstatusmap = new HashMap<String, Object>();
                Long beattime = (Long) loginmap.get("beattime");
                Long logintime = (Long) loginmap.get("logintime");
                String localid = (String) loginmap.get("localid");
                if(localid == null)localid="";
                boolean isonline = false;
                if (beattime != null) {
                    //心跳在60秒内证明在线
                    isonline = isParkOnline(beattime.longValue(), 60);

                    if (!isonline) {
                        isonline = isParkOnline(logintime.longValue(), 10);
                    }
                }
                if (isonline) {
                    parkstatusmap.put("state", 1);
//                    parkstatusmap.put("localid", localid.substring(localid.indexOf("_")+1));
//                    parkstatusmap.put("beat_time",beattime);
                } else {
                    parkstatusmap.put("state", 0);

//                    parkstatusmap.put("localid", localid.substring(localid.indexOf("_")+1));
                }
                if(beattime!=null){
                    parkstatusmap.put("beat_time",beattime);
                }else{
                    parkstatusmap.put("beat_time",logintime);
                }
                parkState.add(parkstatusmap);
            }
        }

        return parkState;
    }

    /**
     * 判断车场是否在线
     * @param time
     * @param delayTime
     * @return
     * @time 2017年 下午12:03:41
     * @author QuanHao
     */
    private boolean isParkOnline(long time,int delayTime){
        long curTime = System.currentTimeMillis()/1000;
        long margin = curTime-time;
        if(margin-delayTime<=0){
            return true;
        }
        return false;
    }

}
