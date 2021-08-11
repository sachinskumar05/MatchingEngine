package com.sk.matching.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("app-cfg")
public class AppCfg {

    private String orderPrefixBuy;
    private String orderPrefixSell;

    private String dataDir;
    private String symbolFile;
    private String symbolFileContentSeparator;

    private String aeronDriverDir;


}
