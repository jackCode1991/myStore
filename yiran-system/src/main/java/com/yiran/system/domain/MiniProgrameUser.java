package com.yiran.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.yiran.common.base.BaseEntity;
import java.util.Date;

/**
 * 微信小程序用户表 mini_programe_user
 * 
 * @author yiran
 * @date 2019-12-31
 */
public class MiniProgrameUser extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** open_id */
	private String openId;
	/** skey */
	private String skey;
	/** 创建时间 */
	private Date createTime;
	/** 最后登录时间 */
	private Date lastVisitTime;
	/** session_key */
	private String sessionKey;
	/** 市 */
	private String city;
	/** 省 */
	private String province;
	/** 国 */
	private String country;
	/** 头像 */
	private String avatarUrl;
	/** 性别 */
	private Integer gender;
	/** 网名 */
	private String nickName;

	public void setOpenId(String openId) 
	{
		this.openId = openId;
	}

	public String getOpenId() 
	{
		return openId;
	}
	public void setSkey(String skey) 
	{
		this.skey = skey;
	}

	public String getSkey() 
	{
		return skey;
	}
	public void setCreateTime(Date createTime) 
	{
		this.createTime = createTime;
	}

	public Date getCreateTime() 
	{
		return createTime;
	}
	public void setLastVisitTime(Date lastVisitTime) 
	{
		this.lastVisitTime = lastVisitTime;
	}

	public Date getLastVisitTime() 
	{
		return lastVisitTime;
	}
	public void setSessionKey(String sessionKey) 
	{
		this.sessionKey = sessionKey;
	}

	public String getSessionKey() 
	{
		return sessionKey;
	}
	public void setCity(String city) 
	{
		this.city = city;
	}

	public String getCity() 
	{
		return city;
	}
	public void setProvince(String province) 
	{
		this.province = province;
	}

	public String getProvince() 
	{
		return province;
	}
	public void setCountry(String country) 
	{
		this.country = country;
	}

	public String getCountry() 
	{
		return country;
	}
	public void setAvatarUrl(String avatarUrl) 
	{
		this.avatarUrl = avatarUrl;
	}

	public String getAvatarUrl() 
	{
		return avatarUrl;
	}
	public void setGender(Integer gender) 
	{
		this.gender = gender;
	}

	public Integer getGender() 
	{
		return gender;
	}
	public void setNickName(String nickName) 
	{
		this.nickName = nickName;
	}

	public String getNickName() 
	{
		return nickName;
	}

    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("openId", getOpenId())
            .append("skey", getSkey())
            .append("createTime", getCreateTime())
            .append("lastVisitTime", getLastVisitTime())
            .append("sessionKey", getSessionKey())
            .append("city", getCity())
            .append("province", getProvince())
            .append("country", getCountry())
            .append("avatarUrl", getAvatarUrl())
            .append("gender", getGender())
            .append("nickName", getNickName())
            .toString();
    }
}
