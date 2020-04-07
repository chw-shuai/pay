package com.imooc.pay.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @author 常红伟
 */
@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AliPayAccountConfig {

    private String appId;

    private String aliPayPublicKey;

    private String privateKey;

    private String notifyUrl;

    private String returnUrl;

}
