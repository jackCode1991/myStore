package com.yiran.api.facade;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yiran.api.constants.CodeDTO;
import com.yiran.api.form.LoginForm;
import com.yiran.api.response.Code2SessionResponse;
import com.yiran.api.utils.HttpUtil;
import com.yiran.common.base.ResultWrapper;
import com.yiran.common.config.Global;
import com.yiran.framework.config.JwtApiInterceptor;
import com.yiran.framework.shiro.service.SysPasswordService;
import com.yiran.framework.util.JwtUtils;
import com.yiran.framework.util.WeChatUrl;
import com.yiran.framework.web.base.BaseController;
import com.yiran.system.domain.MiniProgrameUser;
import com.yiran.system.domain.SysUser;
import com.yiran.system.service.IMiniProgrameUserService;
import com.yiran.system.service.ISysUserService;
import com.yiran.weixin.kit.WeixinBasicKit;
import com.yiran.weixin.servlet.WeixinContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/yiran")
@Api(value="登录接口",description="系统登录接口")
public class LoginFacade  extends BaseController{
	private static final Logger logger = LoggerFactory.getLogger(JwtApiInterceptor.class);
	@Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SysPasswordService sysPasswordService;
    
    @Autowired
    private IMiniProgrameUserService programeUserService;
    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
	@PostMapping("/loginUser")
    @ApiOperation(value = "登录",notes="登录")
    public ResultWrapper<Map<String,Object>> login(@RequestBody LoginForm form) throws Exception{
    	Map<String, Object> map = new HashMap<>();
    	System.out.println("api登录请求参数："+JSON.toJSONString(form));
    	//生成token
        String app_token =null;
        //用户登录
    	SysUser user = sysUserService.selectUserByPhoneNumber(form.getMobile());
    	if(user!=null){
    		String password = sysPasswordService.encryptPassword(user.getLoginName(), form.getPassword(), user.getSalt());
    		System.out.println("加密后的密码："+password);
    		if(!password.equals(user.getPassword())){
    			return ResultWrapper.error().newInstance("0003", "登录密码错误");
    		}
    		//app_token = JwtUtils.createToken(user.getUserName());
    		//app_token = JwtUtils.createToken(user);
    		map.put("token", app_token);
            map.put("expire", Global.getJwtOutTime());
            map.put("user", user);
    	}else{
    		return ResultWrapper.error().newInstance("0003", "用户名不存在");
    	}
        return ResultWrapper.ok().putData(map);
    }
	
	@PostMapping("/user/wechat/wxLogin")
    @ApiOperation(value = "微信小程序登录",notes="微信小程序登录")
    public ResultWrapper<Map<String,Object>> wxLogin(@RequestBody @Validated CodeDTO request) throws Exception{
    	Map<String, Object> map = new HashMap<>();
    	logger.info("wechat登录请求参数："+request.getCode());
    	//1 . code2session返回JSON数据
        String resultJson = code2Session(request.getCode());
    	//2 . 解析数据
        Code2SessionResponse response = JSONObject.toJavaObject(JSONObject.parseObject(resultJson), Code2SessionResponse.class);
        if (!response.getErrcode().equals("0"))
        	return ResultWrapper.other("code2session失败 : " + response.getErrmsg());
        else {
        	logger.info("调用微信成功");
        	// 用户非敏感信息：rawData
            // 签名：signature
            JSONObject rawDataJson = JSON.parseObject(request.getRawData());
            // 3.接收微信接口服务 获取返回的参数
            String openid = response.getOpenid();
            String sessionKey = response.getSession_key();
            // 4.校验签名 小程序发送的签名signature与服务器端生成的签名signature2 = sha1(rawData + sessionKey)
            String signature2 = DigestUtils.sha1Hex(request.getRawData() + sessionKey);
            if (!request.getSignature().equals(signature2)) {
                return ResultWrapper.error().newInstance("500", "签名校验失败");
            }
            // 5.根据返回的User实体类，判断用户是否是新用户，是的话，将用户信息存到数据库；不是的话，更新最新登录时间
            MiniProgrameUser user = programeUserService.selectMiniProgrameUserById(openid);
            // uuid生成唯一key，用于维护微信小程序用户与服务端的会话
            //String skey = JwtUtils.createToken(user);
            String skey = UUID.randomUUID().toString().replaceAll("-","");
            if (user == null) {
                // 用户信息入库
                String nickName = rawDataJson.getString("nickName");
                String avatarUrl = rawDataJson.getString("avatarUrl");
                String gender = rawDataJson.getString("gender");
                String city = rawDataJson.getString("city");
                String country = rawDataJson.getString("country");
                String province = rawDataJson.getString("province");
     
                user = new MiniProgrameUser();
                user.setOpenId(openid);
                user.setSkey(skey);
                user.setCreateTime(new Date());
                user.setLastVisitTime(new Date());
                user.setSessionKey(sessionKey);
                user.setCity(city);
                user.setProvince(province);
                user.setCountry(country);
                user.setAvatarUrl(avatarUrl);
                user.setGender(Integer.parseInt(gender));
                user.setNickName(nickName);
     
                this.programeUserService.insertMiniProgrameUser(user);
            } else {
                // 已存在，更新用户登录时间
                user.setLastVisitTime(new Date());
                // 重新设置会话skey
                user.setSkey(skey);
                programeUserService.updateMiniProgrameUser(user);
            }
            map.put("token", skey);
            map.put("userInfo",user);
            //encrypteData比rowData多了appid和openid
            //JSONObject userInfo = WechatUtil.getUserInfo(encrypteData, sessionKey, iv);
            //6. 把新的skey返回给小程序
        }
        return ResultWrapper.ok().putData(map);
    }
	
	/**
     * 微信的 code2session 接口 获取微信用户信息
     * 官方说明 : https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
     */
    private static String code2Session(String jsCode) {
        String code2SessionUrl = WeChatUrl.JS_CODE_2_SESSION.getUrl();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("appid", WeixinContext.getInstance().getMiniAppId());
        params.add("secret", WeixinContext.getInstance().getMiniAppSecurt());
        params.add("js_code", jsCode);
        params.add("grant_type", "grant_type");
        URI code2Session = HttpUtil.getURIwithParams(code2SessionUrl, params);
        return WeixinBasicKit.sendGet(code2Session.toString());
    }
    
}
