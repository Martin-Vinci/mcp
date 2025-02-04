package com.greybox.mediums.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "io.ci")
@Configuration("schemaConfig")
public class SchemaConfig {
    private String coreSchema;
    private int defaultLimit;
    private int yearsToKeepLog;
    private String enableDebug;
    private Integer idleTimeout;
    private String jwtSecret;
    private int jwtExpirationMs;
    private String smtpServer;
    private String smtpPort;
    private String emailUserName;
    private String emailAddress;
    private String equiwebWSClient;
    private String emailPassword;
    private String mcpGateWayURL;
    private String gateWayUserName;
    private String gateWayPassword;
    private int equiWebInstitutionId;
    private String equiwebVendorCode;
    private String equiwebVendorPassword;
    private String equiwebUsername;
    private String yoUgandaPaymentURL;
    private String yoUgandaAPIUserName;
    private String yoUgandaAPIPassword;
    private String efrisBranchId;
    private String efrisOfflineURL;
    private String efrisAppId;
    private String efrisDeviceNo;
}
