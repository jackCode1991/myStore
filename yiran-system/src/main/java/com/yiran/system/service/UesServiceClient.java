package com.yiran.system.service;

import com.yiran.common.enums.EncryptType;

/**
 * UES接口
 * @author pandaa
 *
 */
public interface UesServiceClient {

    /**
     * 解密
     * @param ticket
     * @return
     */
    public String getDataByTicket(String ticket,EncryptType encryptType);

	/**
	 * 加密
	 * @param src
	 * @return
	 * @throws Exception
	 */
	String encryptData(String plainText,EncryptType encryptType) throws Exception;
}
