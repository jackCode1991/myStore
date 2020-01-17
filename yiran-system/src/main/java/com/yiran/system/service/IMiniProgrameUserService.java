package com.yiran.system.service;

import java.util.List;

import com.yiran.system.domain.MiniProgrameUser;

/**
 * 微信小程序用户 服务层
 * 
 * @author yiran
 * @date 2019-12-31
 */
public interface IMiniProgrameUserService 
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
     * 删除微信小程序用户信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteMiniProgrameUserByIds(String ids);
	
}
