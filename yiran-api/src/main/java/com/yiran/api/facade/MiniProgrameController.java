package com.yiran.api.facade;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.yiran.common.base.ResultWrapper;
import com.yiran.common.utils.PageRequest;
import com.yiran.common.utils.PageResult;
import com.yiran.framework.web.base.BaseController;
import com.yiran.wechat.domain.WechatProduct;
import com.yiran.wechat.service.IWechatIconSortService;
import com.yiran.wechat.service.IWechatIndexColumnService;
import com.yiran.wechat.service.IWechatIndexPicService;
import com.yiran.wechat.service.IWechatProductService;
import com.yiran.wechat.service.IWechatShopProductCategoryService;
import com.yiran.wechat.service.IWechatShoppingCartService;
import com.yiran.weixin.service.IWeixinUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/yiran/miniPrograme")
@Api(value="微信商城接口",description="微信商城接口")
public class MiniProgrameController extends BaseController{
	private static final Logger logger = LoggerFactory.getLogger(MiniProgrameController.class);
	@Autowired
	private IWechatIndexPicService wechatIndexPicService;
	@Autowired
	private IWechatIconSortService wechatIconSortService;
	@Autowired
	private IWechatIndexColumnService wechatIndexColumnService;
	@Autowired
	private IWechatShopProductCategoryService wechatShopProductCategoryService;
	@Autowired
	private IWechatShoppingCartService wechatShoppingCartService;
	@Autowired
	private IWechatProductService wechatProductService;
	
	@Autowired
	private IWeixinUserService weixinUserService;
	
	
	@PostMapping(value="/category",produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("获取商品类目列表信息")
    public ResultWrapper<Map<String,Object>> productCategorySecondList(@RequestBody PageRequest request){
		Map<String, Object> map = new HashMap<>();
		PageResult queryCategory = wechatShopProductCategoryService.queryCategory(request);
		logger.info("获取商品类目列表信息:" + JSON.toJSONString(queryCategory.getContent()));
		map.put("items", queryCategory.getContent());
		queryCategory.setContent(Collections.EMPTY_LIST);
		map.put("paginate", queryCategory);
        return ResultWrapper.ok().putData(map);
    }
	
	@GetMapping("/category/goodsList")
    @ApiOperation("获取商品类目列表信息")
    public ResultWrapper<Map<String,Object>> goodsList(@RequestParam Integer pageNum,@RequestParam Integer pageSize,Integer type){
		PageRequest request=new PageRequest();
		request.setPageNum(pageNum);
		request.setPageSize(pageSize);
		Map<String, Object> map = new HashMap<>();
		WechatProduct wechatProduct =new WechatProduct();
		wechatProduct.setCategoryId(String.valueOf(type));
		logger.info("获取商品类目列表信息-->类目ID:" + type);
		PageResult queryGoodsListByCid = wechatProductService.queryGoodsListByCid(request,wechatProduct);
		logger.info("获取商品类目列表信息:" + JSON.toJSONString(queryGoodsListByCid.getContent()));
		map.put("items", queryGoodsListByCid.getContent());
		queryGoodsListByCid.setContent(Collections.EMPTY_LIST);
		map.put("paginate", queryGoodsListByCid);
        return ResultWrapper.ok().putData(map);
    }
	
}
