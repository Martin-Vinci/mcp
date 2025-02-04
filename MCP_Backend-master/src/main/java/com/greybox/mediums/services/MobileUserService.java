
package com.greybox.mediums.services;

import com.greybox.mediums.config.RestTemplateConfig;
import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.*;
import com.greybox.mediums.models.equiweb.CIAccount;
import com.greybox.mediums.models.equiweb.CIAccountBalance;
import com.greybox.mediums.repository.*;
import com.greybox.mediums.utils.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.greybox.mediums.utils.Logger.logError;


@Service
public class MobileUserService {

    @Autowired
    private SchemaConfig schemaConfig;
    @Autowired
    private MobileUserRepo mobileUserRepo;
    @Autowired
    private MobileUserAccountRepo accountRepo;
    @Autowired
    private CustomerDetailRepo customerDetailRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private EquiWebService equiWebService;
    @Autowired
    private ReportRepo reportRepo;
    @Autowired
    private CompanyInfoRepo companyInfoRepo;

    /*  46 */
    public static void main(String[] args) throws Exception {
        RestTemplateConfig restTemplate = new RestTemplateConfig();
        String baseUrl = "https://41.210.172.245:9000/mcp-gateway-prod/api/EQuiWeb/accountResponseByAccountNo";
        URI uri = new URI("https://41.210.172.245:9000/mcp-gateway-prod/api/EQuiWeb/accountResponseByAccountNo");
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("accountNo", "2300000274");

        JSONObject jsonObject = new JSONObject(requestData);
        String jsonRequest = jsonObject.toString();
        TxnResult wsResponse = restTemplate.post(jsonRequest, baseUrl, "POST", "eQuiWeb");

        if (!wsResponse.getCode().equals("00")) {
            throw new MediumException(ErrorData.builder()
                    .code(wsResponse.getCode())
                    .message(wsResponse.getMessage())
                    .build());
        }

        JSONObject response = new JSONObject((String) wsResponse.getData());
        logError(response);
        logError(response.toString());
        String responseCode = response.getJSONObject("response").getString("responseCode");
        String responseMessage = response.getJSONObject("response").getString("responseMessage");

        if (!responseCode.equals("0")) {
            throw new MediumException(ErrorData.builder()
                    .code(responseCode)
                    .message(responseMessage)
                    .build());
        }
    }



    public TxnResult findAgentCompanyInfo(AgentCompanyInfo request) throws MediumException {
        List<AgentCompanyInfo> mobileUsers = companyInfoRepo.findAgentCompanyInfo();
        if (mobileUsers == null || mobileUsers.isEmpty()) {
            return TxnResult.builder()
                    .code("404")
                    .message("No records found")
                    .build();
        }
        return TxnResult.builder()
                .message("approved")
                .code("00")
                .data(mobileUsers)
                .build();
    }



    public TxnResult find(MobileUser request) throws MediumException {
        List<MobileUser> customers;

        if (request.getCustomerName() != null && request.getPhoneNumber() != null) {
            request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
            customers = this.mobileUserRepo.findEntityCustomersByPhoneAndName(
                    request.getPhoneNumber(), request.getCustomerName());
        } else if (request.getCustomerName() != null && request.getPhoneNumber() == null) {
            customers = this.mobileUserRepo.findEntityCustomersByName(request.getCustomerName());
        } else if (request.getCustomerName() == null && request.getPhoneNumber() != null) {
            request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
            customers = this.mobileUserRepo.findCustomersByPhone(request.getPhoneNumber());
        } else {
            customers = this.mobileUserRepo.findMobileUser();
        }

        if (customers == null || customers.isEmpty()) {
            return TxnResult.builder()
                    .code("404")
                    .message("No records found")
                    .build();
        }

        return TxnResult.builder()
                .message("approved")
                .code("00")
                .data(customers)
                .build();
    }



    public TxnResult findAgents(MobileUser request) throws MediumException {
        if (request.getCustomerName() != null && request.getPhoneNumber() != null) {
            request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        }

        List<MobileUser> mobileUsers = this.reportRepo.findAgentDetails(request);

        if (mobileUsers == null || mobileUsers.isEmpty()) {
            return TxnResult.builder()
                    .code("404")
                    .message("No records found")
                    .build();
        }

        return TxnResult.builder()
                .message("approved")
                .code("00")
                .data(mobileUsers)
                .build();
    }



    public TxnResult findOutletDetails(OutletAuthRequest request) {
        /* 113 */
        MobileUser customers = this.mobileUserRepo.findOutletByOutletCode(request.getOutletCode());
        /* 114 */
        if (customers == null) {
            /* 115 */
            return TxnResult.builder().code("404")
/* 116 */.message("Invalid outlet code specified")
/* 117 */.build();

        }
        /* 119 */
        List<MobileUserAccount> accounts = this.accountRepo.findUserTransactingAccounts(customers.getId().intValue());
        /* 120 */
        String acctNo = null;
        /* 121 */
        if (accounts != null || !accounts.isEmpty()) {
            /* 122 */
            for (MobileUserAccount mobileUserAccount : accounts) {
                /* 123 */
                acctNo = mobileUserAccount.getAcctNo();

            }

        }
        /* 126 */
        return TxnResult.builder().message("approved")
/* 127 */.code("00").data(OutletResponse.builder()
/* 128 */.acctNo(acctNo)
/* 129 */.outletName(customers.getCustomerName())
/* 130 */.outletPhone(customers.getPhoneNumber()).build())
/* 131 */.build();

    }


    public TxnResult findSuperAgentDetails(OutletAuthRequest request) {
        /* 136 */
        MobileUser customers = this.mobileUserRepo.findSuperAgentByOutletCode(request.getOutletCode().trim());
        /* 137 */
        if (customers == null) {
            /* 138 */
            return TxnResult.builder().code("404")
/* 139 */.message("Invalid super agent code specified")
/* 140 */.build();

        }
        /* 142 */
        List<MobileUserAccount> accounts = this.accountRepo.findUserTransactingAccounts(customers.getId().intValue());
        /* 143 */
        String acctNo = null;
        /* 144 */
        if (accounts != null || !accounts.isEmpty()) {
            /* 145 */
            for (MobileUserAccount mobileUserAccount : accounts) {
                /* 146 */
                acctNo = mobileUserAccount.getAcctNo();

            }

        }
        /* 149 */
        return TxnResult.builder().message("approved")
/* 150 */.code("00").data(OutletResponse.builder()
/* 151 */.acctNo(acctNo)
/* 152 */.outletName(customers.getCustomerName().trim())
/* 153 */.outletPhone(customers.getPhoneNumber().trim()).build())
/* 154 */.build();

    }


    public TxnResult findTransactingAgents(MobileUser request) {
        /* 158 */
        List<MobileUser> customers = this.mobileUserRepo.findOutlets();
        /* 159 */
        if (customers == null || customers.isEmpty())
            /* 160 */ return TxnResult.builder().code("404")
/* 161 */.message("No records found")
/* 162 */.build();
        /* 163 */
        return TxnResult.builder().message("approved")
/* 164 */.code("00").data(customers).build();

    }


    public TxnResult findOutlets(MobileUser request) {
        /* 169 */
        List<MobileUser> customers = this.mobileUserRepo.findOutlets(request.getEntityCode());
        /* 170 */
        if (customers == null || customers.isEmpty())
            /* 171 */ return TxnResult.builder().code("404")
/* 172 */.message("No records found")
/* 173 */.build();
        /* 174 */
        return TxnResult.builder().message("approved")
/* 175 */.code("00").data(customers).build();

    }


    public TxnResult findPendingCustomers(MobileUser request) {
        /* 179 */
        List<MobileUser> customers = this.mobileUserRepo.findPendingCustomers();
        /* 180 */
        if (customers == null || customers.isEmpty())
            /* 181 */ return TxnResult.builder().code("404")
/* 182 */.message("No records found")
/* 183 */.build();
        /* 184 */
        return TxnResult.builder().message("approved")
/* 185 */.code("00").data(customers).build();

    }


    public TxnResult findCustomerDetails(MobileUser request) {
        /* 189 */
        CustomerDetail customers = this.customerDetailRepo.findCustomerDetails(request.getId().intValue());
        /* 190 */
        if (customers == null) {
            /* 191 */
            return TxnResult.builder().code("404")
/* 192 */.message("No records found")
/* 193 */.build();

        }
        /* 195 */
        customers.setPhotoBase64String(ImageUtils.getImageToString(customers.getCustomerPhoto()));
        /* 196 */
        customers.setSignatureBase64String(ImageUtils.getImageToString(customers.getCustomerSignature()));
        /* 197 */
        return TxnResult.builder().message("approved")
/* 198 */.code("00").data(customers).build();

    }


    public TxnResult find(MobileUserAccount request) {
        /* 203 */
        List<MobileUserAccount> customers = this.accountRepo.findMobileUserAcct(request.getMobileUserId());
        /* 204 */
        if (customers == null || customers.isEmpty())
            /* 205 */ return TxnResult.builder().code("404")
/* 206 */.message("No records found")
/* 207 */.build();
        /* 208 */
        return TxnResult.builder().message("approved")
/* 209 */.code("00").data(customers).build();

    }


    public TxnResult accountValidationByPhoneNo(String phoneNo) throws MediumException {
        /* 213 */
        phoneNo = StringUtil.formatPhoneNumber(phoneNo);
        /* 214 */
        MobileUser customers = this.mobileUserRepo.findMobileUserByPhoneNo(phoneNo);
        /* 215 */
        if (customers == null) {
            /* 216 */
            throw new MediumException(ErrorData.builder()
/* 217 */.code("404")
/* 218 */.message("Invalid mobile phone specified").build());

        }
        /* 220 */
        String acctType = customers.getAcctType().trim();
        /* 221 */
        List<AccountData> accountList = new ArrayList<>();
        /* 222 */
        List<MobileUserAccount> accounts = this.accountRepo.findUserTransactingAccounts(customers.getId().intValue());
        /* 223 */
        if (accounts != null || !accounts.isEmpty()) {
            /* 224 */
            for (MobileUserAccount mobileUserAccount : accounts) {
                /* 225 */
                accountList.add(AccountData.builder()
/* 226 */.acctNo(mobileUserAccount.getAcctNo())
/* 227 */.acctType(mobileUserAccount.getAcctType())
/* 228 */.description(mobileUserAccount.getDescription()).build());

            }

        }

        /* 232 */
        return TxnResult.builder().message("approved")
/* 233 */.code("00").data(CustomerData.builder()
/* 234 */.customerName(customers.getCustomerName().trim())
/* 235 */.lockedFlag(customers.getLockedFlag().booleanValue())
/* 236 */.deviceID(customers.getAuthImei())
/* 237 */.entityType(acctType)
/* 238 */.accountList(accountList)
/* 239 */.outletCode(customers.getPhoneNumber())
/* 240 */.pinChangeFlag(customers.getPinChangeFlag().booleanValue())
/* 241 */.phoneNo(phoneNo)
/* 242 */.build()).build();

    }


    @Transactional
    public TxnResult pinAuthentication(OutletAuthRequest request) throws MediumException, NoSuchAlgorithmException {
        request.setUserPhoneNo(StringUtil.formatPhoneNumber(request.getUserPhoneNo()));
        MobileUser customers = this.mobileUserRepo.findMobileUserByPhoneNo(request.getUserPhoneNo());
        if (customers == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        if (customers.getLockedFlag().booleanValue()) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Authentication failed. Device locked, contact customer care").build());
        }
        if (request.getChannelCode().equals("MOBILE") && (request
                .getNewDeviceFlag() == null || !request.getNewDeviceFlag().equals("Y"))) {
            if (!customers.getUseAndroidChannel().booleanValue())
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authentication failed. Mobile app access is disabled, contact customer care").build());
            if (request.getDeviceId() == null)
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Invalid device Id specified").build());
            if (customers.getAuthImsi() == null)
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authorisation Error: No Device Information found on the account. Please reactivate your device").build());
            if (!customers.getAuthImsi().trim().equals(request.getDeviceId()))
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authorisation Error: Specified device is not authorised for this mobile phone").build());
        }
        if (request.getChannelCode().equals("USSD") &&
                !customers.getUseAndroidChannel().booleanValue()) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Authentication failed. Mobile app access is disabled, contact customer care").build());
        }

        String decryptedPin = PINDecryptor.decrypt(request.getPinNo());
        String requestPinString = decryptedPin + "-" + request.getUserPhoneNo();
        String requestEncryptedPinString = Encrypter.encryptWithSHA256(requestPinString);

        if (customers.getPin() == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        if (!requestEncryptedPinString.trim().equals(customers.getPin().trim())) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        }

        List<AccountData> accountList = new ArrayList<>();
        List<MobileUserAccount> accounts = this.accountRepo.findUserTransactingAccounts(customers.getId().intValue());
        if (accounts != null || !accounts.isEmpty()) {
            for (MobileUserAccount mobileUserAccount : accounts) {
                accountList.add(AccountData.builder()
                        .acctNo(mobileUserAccount.getAcctNo())
                        .acctType(mobileUserAccount.getAcctType())
                        .description(mobileUserAccount.getDescription()).build());
            }
        }
        return TxnResult.builder().message("approved")
                .code("00").data(CustomerData.builder().customerName(customers.getCustomerName())
                        .lockedFlag(customers.getLockedFlag().booleanValue())
                        .deviceID(customers.getAuthImei())
                        .entityType(customers.getAcctType().trim())
                        .accountList(accountList)
                        .outletCode(customers.getOutletCode())
                        .pinChangeFlag(customers.getPinChangeFlag().booleanValue())
                        .phoneNo(request.getUserPhoneNo())
                        .build()).build();
    }


    public void internalPinAuthentication(OutletAuthRequest request) throws NoSuchAlgorithmException {

        if (request.getOutletCode() == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Username or Outlet code").build());
        }
        if (request.getOutletCode().equals("EXTERNAL")) {
            return;
        } else if (request.getChannelCode().equals("MCPORTAL")) {
            return;
        }
        if (request.getPinNo() == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        }

        request.setUserPhoneNo(StringUtil.formatPhoneNumber(request.getUserPhoneNo()));
        MobileUser customers = this.mobileUserRepo.findMobileUserByPhoneNo(request.getUserPhoneNo());
        if (customers == null)
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        if (customers.getPin() == null) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        }
        if (customers.getLockedFlag().booleanValue()) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Authentication failed. Device locked, contact customer care").build());
        }
        if (request.getChannelCode().equals("USSD") &&
                !customers.getUseAndroidChannel().booleanValue()) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Authentication failed. Mobile app access is disabled, contact customer care").build());
        }
        if (request.getChannelCode().equals("MOBILE")) {
            if (request.getDeviceId() == null) {
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Invalid device Id specified").build());
            }
            if (!customers.getUseAndroidChannel().booleanValue()) {
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authentication failed. Mobile app access is disabled, contact customer care").build());
            }
            if (customers.getAuthImsi() == null) {
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authorisation Error: No Device Information found on the account. Please reactivate your device").build());
            }
            if (!customers.getAuthImsi().trim().equals(request.getDeviceId())) {
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Authorisation Error: Specified device is not authorised for this mobile phone").build());
            }
        }

        String decryptedPin = PINDecryptor.decrypt(request.getPinNo());
        String requestPinString = decryptedPin + "-" + request.getUserPhoneNo();
        String requestEncryptedPinString = Encrypter.encryptWithSHA256(requestPinString);
        if (!requestEncryptedPinString.trim().equals(customers.getPin().trim())) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Invalid Pin or Mobile phone").build());
        }
        if (!customers.getPinChangeFlag().booleanValue()) {
            throw new MediumException(ErrorData.builder()
                    .code("404")
                    .message("Please change your first time pin").build());
        }
        CustomerData.builder()
                .customerName(customers.getCustomerName())
                .lockedFlag(customers.getLockedFlag().booleanValue())
                .deviceID(customers.getAuthImei())
                .entityType(customers.getAcctType().trim())
                .outletCode(customers.getPhoneNumber())
                .pinChangeFlag(customers.getPinChangeFlag().booleanValue())
                .phoneNo(request.getUserPhoneNo()).build();
    }


    public TxnResult pinChange(PinRequestData request) throws MediumException, NoSuchAlgorithmException {
        request.setUserPhoneNo(StringUtil.formatPhoneNumber(request.getUserPhoneNo()));
        MobileUser customers = this.mobileUserRepo.findMobileUserByPhoneNo(request.getUserPhoneNo());
        if (customers == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Invalid Pin or Mobile phone").build());
        } else {
            String newPin = PINDecryptor.decrypt(request.getNewPin());
            String confirmPin = PINDecryptor.decrypt(request.getConfirmPin());
            String pinNo = PINDecryptor.decrypt(request.getPinNo());
            if (!PinValidator.isPinValid(newPin)) {
                throw new MediumException(ErrorData.builder().code("404").message("Weak Pin, Please change").build());
            } else if (!newPin.equals(confirmPin)) {
                throw new MediumException(ErrorData.builder().code("404").message("New PIN and Confirm PIN do not match").build());
            } else {
                String requestPinString = pinNo + "-" + request.getUserPhoneNo();
                String requestEncryptedPinString = Encrypter.encryptWithSHA256(requestPinString);
                if (!requestEncryptedPinString.trim().equals(customers.getPin().trim())) {
                    throw new MediumException(ErrorData.builder().code("404").message("Invalid Old Pin or Mobile phone").build());
                } else {
                    requestPinString = newPin + "-" + request.getUserPhoneNo();
                    requestEncryptedPinString = Encrypter.encryptWithSHA256(requestPinString);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(2, 5);
                    Timestamp expiryDt = new Timestamp(calendar.getTimeInMillis());
                    this.mobileUserRepo.changePin(customers.getId(), requestEncryptedPinString, expiryDt);
                    return TxnResult.builder().message("approved").code("00").build();
                }
            }
        }
    }


    public TxnResult saveUserAccount(MobileUserAccount request) throws Exception {
        /* 448 */
        MobileUserAccount mobileUserAccount = this.accountRepo.findAccountDetails(request.getAcctNo());
        /* 449 */
        if (mobileUserAccount != null) {
            /* 450 */
            throw new MediumException(ErrorData.builder()
/* 451 */.code("404")
/* 452 */.message("Account number is already assigned to another user.").build());

        }
        /* 454 */
        validateAccountFromEQuiWeb(request.getAcctNo());
        /* 455 */
        request.setDateAdded(DataUtils.getCurrentTimeStamp());
        /* 456 */
        request.setCurrentBalance(BigDecimal.ZERO);
        /* 457 */
        this.accountRepo.save(request);
        /* 458 */
        return TxnResult.builder().message("approved")
/* 459 */.code("00").data(request).build();

    }


    @Transactional
    public TxnResult pinReset(MobileUser request) throws NoSuchAlgorithmException, URISyntaxException, MediumException {
        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        MobileUser customers = this.mobileUserRepo.findMobileUserByPhoneNo(request.getPhoneNumber());
        if (customers == null) {
            throw new MediumException(ErrorData.builder().code("404").message("Invalid Mobile phone.").build());
        } else {
            String plainPin = StringUtil.generateRandomNumber(4);
            String pinString = plainPin + "-" + request.getPhoneNumber();
            String encryptedPin = Encrypter.encryptWithSHA256(pinString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(10, 5);
            Timestamp expiryDt = new Timestamp(calendar.getTimeInMillis());
            this.mobileUserRepo.resetCustomerPin(request.getPhoneNumber(), encryptedPin, expiryDt);
            MessageOutbox messageOutbox = new MessageOutbox();
            String expiryTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(expiryDt);
            String messageText = "Micropay: Your Account PIN is " + plainPin + ". Expiry date: " + expiryTime;
            messageOutbox.setRecipientNumber(request.getPhoneNumber());
            messageOutbox.setMessageText(messageText);
            messageOutbox.setDeliverSMS(true);
            this.messageService.logSMS(messageOutbox);
            return TxnResult.builder().message("approved").code("00").data(customers).build();
        }
    }


    public TxnResult pairCustomerDevice(OutletAuthRequest request) throws MediumException {
        request.setUserPhoneNo(StringUtil.formatPhoneNumber(request.getUserPhoneNo()));
        mobileUserRepo.pairCustomerDevice(request.getUserPhoneNo(), request.getDeviceId(), request.getImeiNumber());
        return TxnResult.builder().message("approved").
                code("00").build();
    }

    public TxnResult enrollCustomerByAgent(MobileUser request) throws MediumException {
        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        request.setPin("");
        request.setApprovalStatus(false);
        //request.setWapOtpExpiry(DataUtils.getCurrentTimeStamp());
        request.setDateCreated(DataUtils.getCurrentDate().toLocalDate());
        MobileUser response = mobileUserRepo.save(request);

        if (request.getAccountList().isEmpty())
            throw new MediumException(ErrorData.builder().code("-99")
                    .message("Missing account number(s)").build());
        for (MobileUserAccount account : request.getAccountList()) {
            account.setMobileUserId(response.getId());
            account.setDateAdded(DataUtils.getCurrentTimeStamp());
            account.setActive(true);
            accountRepo.save(account);
        }
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public void validateAccountFromEQuiWeb(String accountNo) throws Exception {
        RestTemplateConfig restTemplate = new RestTemplateConfig();
        final String baseUrl = schemaConfig.getMcpGateWayURL() + "/EQuiWeb/accountResponseByAccountNo";
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("accountNo", accountNo);

        JSONObject jsonObject = new JSONObject(requestData);
        String jsonRequest = jsonObject.toString();
        TxnResult wsResponse = restTemplate.post(jsonRequest, baseUrl, "POST", "eQuiWeb");
        if (!wsResponse.getCode().equals("00"))
            throw new MediumException(ErrorData.builder()
                    .code(wsResponse.getCode())
                    .message(wsResponse.getMessage()).build());

        JSONObject response = new JSONObject((String) wsResponse.getData());
        logError(response);
        logError(response.toString());
        String responseCode = response.getJSONObject("response").getString("responseCode");
        String responseMessage = response.getJSONObject("response").getString("responseMessage");
        if (!responseCode.equals("0"))
            throw new MediumException(ErrorData.builder()
                    .code(responseCode)
                    .message(responseMessage).build());
    }

    public TxnResult registerCustomer(MobileUser request) throws MediumException {
        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        if ((request.getAcctType().equals("AGENT") || request.getAcctType().equals("SUPER_AGENT")) && request.getId() == null) {
            Integer agentCode = mobileUserRepo.findMaxAgentCode();
            agentCode = agentCode == null ? 1000 : agentCode + 1;
            request.setEntityCode(agentCode);
            if (request.getAcctType().equals("SUPER_AGENT"))
                request.setOutletCode(agentCode.toString());
        }

        if (request.getAcctType().equals("OUTLET") && request.getId() == null) {
            Integer outletCode = mobileUserRepo.findOutletCount(request.getEntityCode());
            outletCode = outletCode == null ? 100 : outletCode + 1;
            String outletNo = request.getEntityCode() + StringUtil.padLeftZeros(outletCode.toString(), 3);
            request.setOutletCode(outletNo);
        }
        request.setApprovalStatus(false);
        request.setDateCreated(DataUtils.getCurrentDate().toLocalDate());
        mobileUserRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    public TxnResult maintainAgent(MobileUser request) throws MediumException {
        if (!request.getAcctType().equals("AGENT"))
            request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));

        if ((request.getAcctType().equals("AGENT") || request.getAcctType().equals("SUPER_AGENT"))) {
            if (request.getRsmId() == null)
                throw new MediumException(ErrorData.builder()
                        .code("404")
                        .message("Relationship Manager is required.").build());

            if (request.getId() == null) {
                Integer agentCode = mobileUserRepo.findMaxAgentCode();
                agentCode = agentCode == null ? 1000 : agentCode + 1;
                request.setEntityCode(agentCode);
                if (request.getAcctType().equals("SUPER_AGENT"))
                    request.setOutletCode(agentCode.toString());
            }
        }

        if (request.getAcctType().equals("OUTLET") && request.getId() == null) {
            Integer outletCode = mobileUserRepo.findOutletCount(request.getEntityCode());
            outletCode = outletCode == null ? 100 : outletCode + 1;
            String outletNo = request.getEntityCode() + StringUtil.padLeftZeros(outletCode.toString(), 3);
            request.setOutletCode(outletNo);
        }
        request.setApprovalStatus(false);
        request.setDateCreated(DataUtils.getCurrentDate().toLocalDate());
        mobileUserRepo.save(request);
        return TxnResult.builder().message("approved").
                code("00").data(request).build();
    }

    @Transactional
    public TxnResult reviewMobileUser(MobileUser request) throws NoSuchAlgorithmException, MediumException {
        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        MobileUser customers = mobileUserRepo.findMobileUserByPhoneNo(request.getPhoneNumber());
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("Invalid Mobile phone")
                    .build();

        if (request.getReviewAction().equals("Decline")) {
            mobileUserRepo.save(customers);
            mobileUserRepo.approveMobileUser(request.getPhoneNumber(),
                    "",
                    false,
                    true,
                    request.getApprovedBy(),
                    DataUtils.getCurrentTimeStamp(),
                    DataUtils.getCurrentTimeStamp()
            );
            return TxnResult.builder().message("approved").
                    code("00").data(customers).build();
        }

        String plainPin = StringUtil.generateRandomNumber(4);
        String pinString = plainPin + "-" + request.getPhoneNumber();
        String encryptedPin = Encrypter.encryptWithSHA256(pinString);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 5);
        Timestamp expiryDt = new Timestamp(calendar.getTimeInMillis());

        mobileUserRepo.approveMobileUser(request.getPhoneNumber(),
                encryptedPin,
                false,
                true,
                request.getApprovedBy(),
                DataUtils.getCurrentTimeStamp(),
                expiryDt
        );

        MessageOutbox messageOutbox = new MessageOutbox();
        String expiryTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(expiryDt);
        String messageText = "Congratulation: Your Micropay Account has been approved. Your PIN is " + plainPin + ". Expiry date: "
                + expiryTime + ". Please change your pin to enjoy Micropay services.";
        messageOutbox.setRecipientNumber(request.getPhoneNumber());
        messageOutbox.setMessageText(messageText);
        messageOutbox.setDeliverSMS(true);
        messageService.logSMS(messageOutbox);
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    @Transactional
    public TxnResult performDevicePairing(MobileUser request) throws MediumException {
        if (request.getActivationCode() == null)
            throw new MediumException(
                    ErrorData.builder()
                            .code("404")
                            .message("Invalid activation code specified. Device Pairing failed.").build());

        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        MobileUser customers = mobileUserRepo.findMobileUserByPhoneNo(request.getPhoneNumber(), request.getActivationCode());
        //MobileUser customers = customerRefRepo.findMobileUserByPhoneNo(request.getPhoneNumber());
        if (customers == null)
            throw new MediumException(
                    ErrorData.builder()
                            .code("404")
                            .message("Invalid Phone number. Device Pairing failed.").build());
        customers.setAuthImsi(request.getAuthImsi());
        customers.setAuthImei(request.getAuthImei());
        mobileUserRepo.save(customers);
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    @Transactional
    public TxnResult generateDeviceActivationCode(MobileUser request) throws MediumException {
        request.setPhoneNumber(StringUtil.formatPhoneNumber(request.getPhoneNumber()));
        MobileUser customers = mobileUserRepo.findMobileUserByPhoneNo(request.getPhoneNumber());
        if (customers == null)
            return TxnResult.builder().code("404")
                    .message("Activation code error. Invalid Mobile phone.")
                    .build();

        String generatedCode = StringUtil.generateRandomNumber(5);
        LocalDateTime expiryDt = DataUtils.getCurrentTimeStamp().toLocalDateTime().plusMinutes(10);
        customers.setWapOtp(generatedCode);
        customers.setWapOtpExpiry(expiryDt);
        mobileUserRepo.save(customers);

        MessageOutbox messageOutbox = new MessageOutbox();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String expiryTime = formatter.format(expiryDt);
        String messageText = "Micropay: Your activation code is " + generatedCode + ". Expiry date: " + expiryTime + "";
        messageOutbox.setRecipientNumber(request.getPhoneNumber());
        messageOutbox.setMessageText(messageText);
        messageOutbox.setDeliverSMS(true);
        messageService.logSMS(messageOutbox);
        return TxnResult.builder().message("approved").
                code("00").data(customers).build();
    }

    public void processAccountBalanceUpdate() {
        AccountRequest accountRequest;
        TxnResult accountBalanceResponse;
        List<MobileUserAccount> accountList = accountRepo.findAccountsForBalanceUpdate();
        for (int i = 0; i < accountList.size(); i++) {
            accountRequest = new AccountRequest();
            try {
                accountRequest.setAcctNo(accountList.get(i).getAcctNo());
                accountBalanceResponse = equiWebService.doAccountBalance(accountRequest);
                if (!accountBalanceResponse.getCode().equals("00")) {
                    logError(accountBalanceResponse.getMessage() + " during balance update");
                }
                CIAccountBalance ciAccountBalance = (CIAccountBalance) accountBalanceResponse.getData();
                accountRepo.updateAccountBalance(accountRequest.getAcctNo(), ciAccountBalance.getAvailableBalance());
            } catch (Exception e) {
                logError("======================= " + accountRequest.getAcctNo() + " Balance update Error =>" + e);
            }
        }
    }


    public TxnResult accountInquiryByPhoneNo(CIAccount request) {
        request.setPhoneNo(StringUtil.formatPhoneNumber(request.getPhoneNo()));
        MobileUser mobileUser = this.mobileUserRepo.findMobileUserByPhoneNo(request.getPhoneNo());

        if (mobileUser != null && !mobileUser.getAcctType().trim().equals("CUSTOMER")) {
            return TxnResult.builder()
                    .message("Account Phone number is restricted from Cash deposit")
                    .code("404")
                    .build();
        }

        TxnResult txnResult = this.equiWebService.accountInquiryByPhoneNo(request);
        return txnResult;
    }

    @Transactional
    public TxnResult createCustomer(CustomerDetail request) {
        MobileUser mobileUser = new MobileUser();
        String customerName = request.getFirstName() + " " + request.getFirstName();
        mobileUser.setCustomerName(customerName);
        mobileUser.setGender(request.getGender());
        mobileUser.setAcctType("CUSTOMER");
        mobileUser.setCreatedBy(Long.valueOf(-99L));
        mobileUser.setPhoneNumber(StringUtil.formatPhoneNumber(request.getMobilePhone()));
        mobileUser.setPin("");
        mobileUser.setApprovalStatus(Boolean.valueOf(false));
        mobileUser.setLockedFlag(Boolean.valueOf(false));
        mobileUser.setPinChangeFlag(Boolean.valueOf(false));
        mobileUser.setUseAndroidChannel(Boolean.valueOf(true));
        mobileUser.setUseUssdChannel(Boolean.valueOf(true));
        mobileUser.setDateCreated(DataUtils.getCurrentDate().toLocalDate());
        mobileUser.setCustomerName(mobileUser.getCustomerName().toUpperCase());
        MobileUser mobileUser1 = this.mobileUserRepo.save(mobileUser);

        MobileUserAccount account = new MobileUserAccount();
        account.setAcctNo(request.getMobilePhone());
        account.setAcctType("SA");
        account.setDescription(customerName);
        account.setMobileUserId(mobileUser1.getId().intValue());
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setDateAdded(DataUtils.getCurrentTimeStamp());
        account.setActive(Boolean.valueOf(true));
        MobileUserAccount account1 = this.accountRepo.save(account);
        request.setOutletNo(request.getOutletCode());
        request.setUserId(mobileUser1.getId());
        request.setCreateDate(DataUtils.getCurrentTimeStamp().toLocalDateTime());
        request.setCustomerPhoto(ImageUtils.getImage(request.getPhotoBase64String()));
        request.setCustomerSignature(ImageUtils.getImage(request.getSignatureBase64String()));
        this.customerDetailRepo.save(request);

        TxnResult txnResult = this.equiWebService.createCustomer(request);
        if (!txnResult.getCode().equals("00")) {
            throw new MediumException(ErrorData.builder().code(txnResult.getCode()).message(txnResult.getMessage()).build());
        }

        CIAccount ciAccount = (CIAccount) txnResult.getData();
        account1.setAcctNo(ciAccount.getAccountNo());
        this.accountRepo.save(account1);
        logError("====================== CUSTOMER CREATION RESPONSE FROM EQUIWEB =============");
        logError(txnResult);
        return txnResult;
    }
}