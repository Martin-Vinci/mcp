namespace micropay_apis.Utils
{
	public class PROPERTIES 
    {
        public static string DATABASE_NAME = System.Configuration.ConfigurationManager.AppSettings["DBName"];
        public static string DATABASE_USER = System.Configuration.ConfigurationManager.AppSettings["DBUser"];
        public static string DATABASE_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["DBPwd"];
        public static string SCHEMA_NAME = System.Configuration.ConfigurationManager.AppSettings["DBSchema"];
        public static string DATABASE_SERVER = System.Configuration.ConfigurationManager.AppSettings["DataSource"];
        public static string VENDOR_NAME = System.Configuration.ConfigurationManager.AppSettings["VCode"];
        public static string DATABASE_BACKUP_PATH = System.Configuration.ConfigurationManager.AppSettings["DataBaseBackUPPath"];
        public static string VENDOR_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["VPassword"];
        public static string ERROR_LOG_PATH = System.Configuration.ConfigurationManager.AppSettings["ErrorLogFile"];
        public static string YO_UGANDA_PAYMENT_GATEWAY = System.Configuration.ConfigurationManager.AppSettings["YoUgandaPaymentURL"];
        public static string YO_UGANDA_API_USERNAME = System.Configuration.ConfigurationManager.AppSettings["YoUgandaAPIUserName"];
        public static string YO_UGANDA_API_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["YoUgandaAPIPassword"];

        public static string InterSwitchQuickTellerBaseURL = System.Configuration.ConfigurationManager.AppSettings["InterSwitchQuickTellerBaseURL"];
        public static string InterSwitchSVABaseURL = System.Configuration.ConfigurationManager.AppSettings["InterSwitchSVABaseURL"];
        public static string InterSwitchClientSecret = System.Configuration.ConfigurationManager.AppSettings["InterSwitchClientSecret"];
        public static string InterSwitchClientId = System.Configuration.ConfigurationManager.AppSettings["InterSwitchClientId"];
        public static string InterSwitchTerminalId = System.Configuration.ConfigurationManager.AppSettings["InterSwitchTerminalId"];
        public static string InterSwitchBankCBNCode = System.Configuration.ConfigurationManager.AppSettings["InterSwitchBankCBNCode"];
        public static string InterSwitchRequestReferencePrefix = System.Configuration.ConfigurationManager.AppSettings["InterSwitchRequestReferencePrefix"];
        public static string LycaMobileBaseURL = System.Configuration.ConfigurationManager.AppSettings["LycaMobileBaseURL"];
        public static string WhiteListedIPAddresses = System.Configuration.ConfigurationManager.AppSettings["WhiteListedIPAddresses"];

        public static string API_USER_NAME = System.Configuration.ConfigurationManager.AppSettings["APIUserName"]; 
        public static string API_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["APIPassword"];
        public static string SMS_GATEWAY = System.Configuration.ConfigurationManager.AppSettings["SMSReceiver"]; 
        public static string EQUIWEB_USER = System.Configuration.ConfigurationManager.AppSettings["eQuiWebUser"];
        public static string PEGASUS_PRIVATE_CERT_PATH = System.Configuration.ConfigurationManager.AppSettings["PegasusPrivateCertPath"];
        public static string PEGASUS_PRIVATE_CERT_PWD = System.Configuration.ConfigurationManager.AppSettings["PegasusPrivateCertPwd"];
        public static string PEGASUS_CERT_ALIAS = System.Configuration.ConfigurationManager.AppSettings["PegasusCertAlias"];
        public static string PEGASUS_VENDOR_USERNAME = System.Configuration.ConfigurationManager.AppSettings["PegasusVendorUserName"];
        public static string PEGASUS_VENDOR_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["PegasusVendorPassword"];
        public static string PRETUPS_AIRTEL = System.Configuration.ConfigurationManager.AppSettings["PreTUPAirtelAPI"];
        public static string MICROPAY_CORE_API = System.Configuration.ConfigurationManager.AppSettings["MicropayCoreAPI"];
        public static string MTNAirtimeURL = System.Configuration.ConfigurationManager.AppSettings["MTNAirtimeURL"];
        public static string MTNDataURL = System.Configuration.ConfigurationManager.AppSettings["MTNDataURL"];
        public static string CENTE_AGENT_FUND_ACCOUNT_MTN = System.Configuration.ConfigurationManager.AppSettings["CENTE_AGENT_FUND_ACCOUNT_MTN"];
        public static string CENTE_AGENT_FUND_ACCOUNT_AIRTEL = System.Configuration.ConfigurationManager.AppSettings["CENTE_AGENT_FUND_ACCOUNT_AIRTEL"];
        public static string TOTAL_FUND_ACCOUNT = System.Configuration.ConfigurationManager.AppSettings["TOTAL_FUND_ACCOUNT"];
        public static int INSTITUTION_ID = CONVERTER.toInt(System.Configuration.ConfigurationManager.AppSettings["InstitutionId"]);
        public static string DB_CONNECTION_STRING = "Server=" + DATABASE_SERVER + "; Port=5432; Database=" + DATABASE_NAME + ";User Id=" + DATABASE_USER + ";Password=" + DATABASE_PASSWORD + "";
        public static string MobileUpdatePath = System.Configuration.ConfigurationManager.AppSettings["MobileUpdatePath"];
        public static string MobileVersion = System.Configuration.ConfigurationManager.AppSettings["MobileVersion"];

        public static string CORPORATE_AGENT_URL = System.Configuration.ConfigurationManager.AppSettings["CoporateAgentURL"];
        public static string CORPORATE_AGENT_PRIVATE_CERTIFICATE_PATH = System.Configuration.ConfigurationManager.AppSettings["CoporateAgentPrivateCertPath"];
        public static string CORPORATE_AGENT_PUBLIC_CERTIFICATE_PATH = System.Configuration.ConfigurationManager.AppSettings["CoporateAgentPublicCertPath"];
        public static string CENTE_PRIVATE_CERT_PWD = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentPrivateCertPwd"];
        public static string CORPORATE_AGENT_CHANNELCODE = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentChannelCode"];
        public static string CORPORATE_AGENT_OUTLET_PIN = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentChannelPIN"];
        public static string CORPORATE_AGENT_DEVICE_ID = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentDeviceID"];
        public static string CORPORATE_AGENT_IMEI_NUMBER = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentImeiNumber"];
        public static string CORPORATE_AGENT_OUTLET_PHONE = System.Configuration.ConfigurationManager.AppSettings["CorporateAgentPhoneNo"];

        public static string CRDB_ESB_PASSWORD = System.Configuration.ConfigurationManager.AppSettings["CRDB_ESB_PASSWORD"];
        public static string CRDB_ESB_UNIQUE_USER = System.Configuration.ConfigurationManager.AppSettings["CRDB_ESB_UNIQUE_USER"];
        public static string ESB_CHANNEL_CODE = System.Configuration.ConfigurationManager.AppSettings["ESB_CHANNEL_CODE"];

        public static string AIRTEL_MONEY_URL = System.Configuration.ConfigurationManager.AppSettings["AIRTEL_MONEY_URL"];
        public static string AIRTEL_MONEY_CLIENT_ID = System.Configuration.ConfigurationManager.AppSettings["AIRTEL_MONEY_CLIENT_ID"];
        public static string AIRTEL_MONEY_CLIENT_SECRET = System.Configuration.ConfigurationManager.AppSettings["AIRTEL_MONEY_CLIENT_SECRET"];
        public static string AIRTEL_MONEY_PIN = System.Configuration.ConfigurationManager.AppSettings["AIRTEL_MONEY_PIN"];

        public static string DTBUrl = System.Configuration.ConfigurationManager.AppSettings["DTBUrl"];
        public static string DTBTerminalId = System.Configuration.ConfigurationManager.AppSettings["DTBTerminalId"];
        public static string DTBAgentCode = System.Configuration.ConfigurationManager.AppSettings["DTBAgentCode"];
        public static string DTBUserId = System.Configuration.ConfigurationManager.AppSettings["DTBUserId"];
        public static string DTBPassword = System.Configuration.ConfigurationManager.AppSettings["DTBPassword"];
        public static string DTBAgentFloatAccount = System.Configuration.ConfigurationManager.AppSettings["DTBAgentFloatAccount"];

        public static string SurePayUrl = System.Configuration.ConfigurationManager.AppSettings["SurePayUrl"];
        public static string SurePayMerchantSecretKey = System.Configuration.ConfigurationManager.AppSettings["SurePayMerchantSecretKey"];

    } 
}    
   