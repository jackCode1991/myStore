package com.yiran.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yiran.common.support.Convert;
import com.yiran.system.domain.MiniProgrameUser;
import com.yiran.system.mapper.MiniProgrameUserMapper;
import com.yiran.system.service.IMiniProgrameUserService;

/**
 * 微信小程序用户 服务层实现
 * 
 * @author yiran
 * @date 2019-12-31
 */
@Service
public class MiniProgrameUserServiceImpl implements IMiniProgrameUserService 
{
	@Autowired
	private MiniProgrameUserMapper miniProgrameUserMapper;

	/**
     * 查询微信小程序用户信息
     * 
     * @param openId 微信小程序用户ID
     * @return 微信小程序用户信息
     */
    @Override
	public MiniProgrameUser selectMiniProgrameUserById(String openId)
	{
	    return miniProgrameUserMapper.selectMiniProgrameUserById(openId);
	}
	
	/**
     * 查询微信小程序用户列表
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 微信小程序用户集合
     */
	@Override
	public List<MiniProgrameUser> selectMiniProgrameUserList(MiniProgrameUser miniProgrameUser)
	{
	    return miniProgrameUserMapper.selectMiniProgrameUserList(miniProgrameUser);
	}
	
    /**
     * 新增微信小程序用户
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 结果
     */
	@Override
	public int insertMiniProgrameUser(MiniProgrameUser miniProgrameUser)
	{
	    return miniProgrameUserMapper.insertMiniProgrameUser(miniProgrameUser);
	}
	
	/**
     * 修改微信小程序用户
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 结果
     */
	@Override
	public int updateMiniProgrameUser(MiniProgrameUser miniProgrameUser)
	{
	    return miniProgrameUserMapper.updateMiniProgrameUser(miniProgrameUser);
	}

	/**
     * 删除微信小程序用户对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	@Override
	public int deleteMiniProgrameUserByIds(String ids)
	{
		return miniProgrameUserMapper.deleteMiniProgrameUserByIds(Convert.toStrArray(ids));
	}
	
}
