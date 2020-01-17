package com.yiran.api.constants;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class CodeDTO {
    @NotEmpty(message = "缺少参数code或code不合法")
    private String code;
    
    private String encryptedData;
    
    private String iv;
    
    private String rawData;
    
    private String signature;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
}