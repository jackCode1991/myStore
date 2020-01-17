package com.yiran.system.mapper;

import java.util.List;

import com.yiran.system.domain.MiniProgrameUser;	

/**
 * 微信小程序用户 数据层
 * 
 * @author yiran
 * @date 2019-12-31
 */
public interface MiniProgrameUserMapper 
{
	/**
     * 查询微信小程序用户信息
     * 
     * @param openId 微信小程序用户ID
     * @return 微信小程序用户信息
     */
	public MiniProgrameUser selectMiniProgrameUserById(String openId);
	
	/**
     * 查询微信小程序用户列表
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 微信小程序用户集合
     */
	public List<MiniProgrameUser> selectMiniProgrameUserList(MiniProgrameUser miniProgrameUser);
	
	/**
     * 新增微信小程序用户
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 结果
     */
	public int insertMiniProgrameUser(MiniProgrameUser miniProgrameUser);
	
	/**
     * 修改微信小程序用户
     * 
     * @param miniProgrameUser 微信小程序用户信息
     * @return 结果
     */
	public int updateMiniProgrameUser(MiniProgrameUser miniProgrameUser);
	
	/**
     * 删除微信小程序用户
     * 
     * @param openId 微信小程序用户ID
     * @return 结果
     */
	public int deleteMiniProgrameUserById(String openId);
	
	/**
     * 批量删除微信小程序用户
     * 
     * @param openIds 需要删除的数据ID
     * @return 结果
     */
	public int deleteMiniProgrameUserByIds(String[] openIds);
	
}