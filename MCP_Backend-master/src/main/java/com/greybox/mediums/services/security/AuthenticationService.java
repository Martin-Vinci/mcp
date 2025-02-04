/*     */
package com.greybox.mediums.services.security;
/*     */
/*     */

import com.greybox.mediums.config.SchemaConfig;
/*     */ import com.greybox.mediums.entities.AccessMenu;
/*     */ import com.greybox.mediums.entities.Password;
/*     */ import com.greybox.mediums.entities.User;
/*     */ import com.greybox.mediums.entities.UserType;
/*     */ import com.greybox.mediums.models.AuthRequest;
/*     */ import com.greybox.mediums.models.AuthResponse;
/*     */ import com.greybox.mediums.models.EmailRequest;
/*     */ import com.greybox.mediums.models.ErrorData;
/*     */ import com.greybox.mediums.models.MainMenu;
/*     */ import com.greybox.mediums.models.PasswordChangeRequest;
/*     */ import com.greybox.mediums.models.SubMenu;
/*     */ import com.greybox.mediums.models.TxnResult;
/*     */ import com.greybox.mediums.models.UserAccessRoles;
/*     */ import com.greybox.mediums.repository.AccessMenuRepo;
/*     */ import com.greybox.mediums.repository.AuthenticationRepoImpl;
/*     */ import com.greybox.mediums.repository.UserRepo;
/*     */ import com.greybox.mediums.repository.UserTypeRepo;
/*     */ import com.greybox.mediums.security.Encryptor;
/*     */ import com.greybox.mediums.utils.DataUtils;
/*     */ import com.greybox.mediums.utils.EmailService;
/*     */ import com.greybox.mediums.utils.MediumException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
import java.util.Date;
/*     */ import java.util.List;
/*     */ import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
/*     */ import org.springframework.stereotype.Service;
/*     */ import org.springframework.transaction.annotation.Transactional;

/*     */
/*     */
@Service
/*     */ public class AuthenticationService {

    @Autowired
 private UserRepo repo;

    @Autowired
 private SchemaConfig schemaConfig;

    @Autowired
 private EmailService emailService;


    /*  42 */
    private MainMenu getRouteInfo(String name, String type, String tooltip, String icon, String state, List<SubMenu> subMenus) {
        return MainMenu.builder()
/*  43 */.name(name)
/*  44 */.type(type)
/*  45 */.tooltip(tooltip)
/*  46 */.icon(icon)
/*  47 */.state(state)
/*  48 */.sub(subMenus)
/*  49 */.build();
    }

    @Autowired
 private UserTypeRepo memberTypeRepo;
    @Autowired
 private AuthenticationRepoImpl pwdRepo;
    @Autowired
 private AccessMenuRepo accessMenuRepo;
    @Autowired
 Encryptor encryptor;

    public TxnResult getMenus(Integer userTypeId) {
        List<AccessMenu> screenAccessRights;
        List<MainMenu> menuList = new ArrayList<>();

        UserType memberType = this.memberTypeRepo.findUserTypeByTypeId(userTypeId);
        if (memberType == null) {
            throw new MediumException(ErrorData.builder()
                    .code("-99")
                    .message("Invalid member type Id specified")
                    .build());
        }

        screenAccessRights = (userTypeId.intValue() == -99)
                ? this.accessMenuRepo.findAccessMenus()
                : this.accessMenuRepo.findAssignedAccessMenu(userTypeId);

        if (screenAccessRights == null || screenAccessRights.isEmpty()) {
            return TxnResult.builder()
                    .code("00")
                    .message("Approved")
                    .data(UserAccessRoles.builder()
                            .menuList(menuList)
                            .build())
                    .build();
        }

        MainMenu dashboard = getRouteInfo("Dashboard", "link", "Dashboard", "dashboard", "dashboard/analytics", null);
        List<SubMenu> sysAdminMenus = new ArrayList<>();
        MainMenu systemControl = null;

        List<SubMenu> agentMenus = new ArrayList<>();
        MainMenu agentControl = null;

        List<SubMenu> billerMenus = new ArrayList<>();
        MainMenu billerControl = null;

        List<SubMenu> webCashMenus = new ArrayList<>();
        MainMenu webCashControl = null;

        List<SubMenu> transactionMenus = new ArrayList<>();
        MainMenu transactionControl = null;

        List<SubMenu> supervisionMenus = new ArrayList<>();
        MainMenu supervisionControl = null;

        for (AccessMenu screenAccessRight : screenAccessRights) {
            String menuCode = screenAccessRight.getMenuCode().toLowerCase();
            switch (menuCode) {
                case "userroleview":
                    sysAdminMenus.add(SubMenu.builder().name("User Roles").state("user-role-search").build());
                    break;
                case "usersview":
                    sysAdminMenus.add(SubMenu.builder().name("Users").state("user-search").build());
                    break;
                case "systemparameterview":
                    sysAdminMenus.add(SubMenu.builder().name("System parameters").state("system-parameter").build());
                    break;
                case "servicechannelview":
                    sysAdminMenus.add(SubMenu.builder().name("Service Channels").state("service-channel-search").build());
                    break;
                case "serviceview":
                    agentMenus.add(SubMenu.builder().name("Services").state("services-list").build());
                    break;
                case "agentsview":
                    agentMenus.add(SubMenu.builder().name("Agents").state("agent-search").build());
                    break;
                case "mobileuserview":
                    agentMenus.add(SubMenu.builder().name("Mobile Users").state("mobile-user-search").build());
                    break;
                case "billersview":
                    billerMenus.add(SubMenu.builder().name("Billers").state("biller-search").build());
                    break;
                case "paymentstatusview":
                    billerMenus.add(SubMenu.builder().name("Transaction Status").state("notification-search").build());
                    break;
                case "messagesview":
                    billerMenus.add(SubMenu.builder().name("Sent Messages").state("messages-search").build());
                    break;
                case "loancustomerview":
                    webCashMenus.add(SubMenu.builder().name("Customer").state("customer-search").build());
                    break;
                case "creditapplview":
                    webCashMenus.add(SubMenu.builder().name("Credit Application").state("credit-appl-search").build());
                    break;
                case "loanaccountview":
                    webCashMenus.add(SubMenu.builder().name("Loan Account").state("loan-account-search").build());
                    break;
                case "loanrepaymentview":
                    webCashMenus.add(SubMenu.builder().name("Loan Repayment").state("loan-repayment-search").build());
                    break;
                case "transactionsview":
                    transactionMenus.add(SubMenu.builder().name("Transaction details").state("transaction-search").build());
                    transactionMenus.add(SubMenu.builder().name("Trust Transactions").state("cente-trust-trans-summary").build());
                    break;
                case "vouchertransview":
                    transactionMenus.add(SubMenu.builder().name("Voucher details").state("voucher-search").build());
                    break;
                case "efrisgoodsview":
                    transactionMenus.add(SubMenu.builder().name("EFRIS Goods").state("efris-service-listing").build());
                    break;
                case "efrisinvoiceview":
                    transactionMenus.add(SubMenu.builder().name("EFRIS Invoices").state("supplier-payment-history").build());
                    break;
                case "mobileuserapprove":
                    supervisionMenus.add(SubMenu.builder().name("Customer Approval").state("agent-banking/mobile-user-review").build());
                    break;
                case "escrowtxnapprove":
                    supervisionMenus.add(SubMenu.builder().name("Escrow Transactions").state("transaction-control/escrow-trans-search").build());
                    break;
                case "creditapplapprove":
                    supervisionMenus.add(SubMenu.builder().name("Credit Applications").state("webcash/credit-applic-approval").build());
                    break;
            }
        }

        if (!sysAdminMenus.isEmpty()) {
            systemControl = getRouteInfo("System Admin", "dropDown", "Forms", "settings", "system-admin", sysAdminMenus);
        }
        if (!agentMenus.isEmpty()) {
            agentControl = getRouteInfo("Agent Control", "dropDown", "Forms", "settings", "agent-banking", agentMenus);
        }
        if (!billerMenus.isEmpty()) {
            billerControl = getRouteInfo("Biller Control", "dropDown", "Forms", "settings", "biller-control", billerMenus);
        }
        if (!webCashMenus.isEmpty()) {
            webCashControl = getRouteInfo("Web Cash", "dropDown", "Forms", "settings", "webcash", webCashMenus);
        }
        if (!transactionMenus.isEmpty()) {
            transactionControl = getRouteInfo("Transactions", "dropDown", "Forms", "settings", "transaction-control", transactionMenus);
        }
        if (!supervisionMenus.isEmpty()) {
            supervisionControl = getRouteInfo("Supervision", "dropDown", "Forms", "settings", null, supervisionMenus);
        }

        List<SubMenu> reportMenus = Arrays.asList(
                SubMenu.builder().name("Voucher Listing").state("voucher-listing-report").build(),
                SubMenu.builder().name("Agent commission").state("agent-commission-report").build(),
                SubMenu.builder().name("Customer listing").state("customer-listing-report").build(),
                SubMenu.builder().name("Agent Listing").state("agent-listing-report").build(),
                SubMenu.builder().name("Transaction Listing").state("transaction-listing-report").build(),
                SubMenu.builder().name("Transaction Bands").state("transaction-band-report").build(),
                SubMenu.builder().name("Transaction Summary").state("transaction-summary-report").build(),
                SubMenu.builder().name("User Listing").state("user-listing-report").build(),
                SubMenu.builder().name("Agent WithHolding Tax").state("agent-transaction-summary-report").build()
        );

        MainMenu reportControl = getRouteInfo("Reports", "dropDown", "Forms", "settings", "report-control", reportMenus);

        menuList.add(dashboard);
        if (systemControl != null) menuList.add(systemControl);
        if (agentControl != null) menuList.add(agentControl);
        if (billerControl != null) menuList.add(billerControl);
        if (webCashControl != null) menuList.add(webCashControl);
        if (transactionControl != null) menuList.add(transactionControl);
        if (supervisionControl != null) menuList.add(supervisionControl);
        if (reportControl != null) menuList.add(reportControl);

        return TxnResult.builder()
                .message("approved")
                .code("00")
                .data(UserAccessRoles.builder()
                        .menuList(menuList)
                        .screenAccessRights(screenAccessRights)
                        .build())
                .build();
    }


    @Transactional
    public TxnResult logoutUser(AuthRequest request) {
        return TxnResult.builder()
                .message("approved")
                .code("00")
                .build();
    }

    @Transactional
    public TxnResult authentication(AuthRequest request) throws Exception {
        List<User> data = repo.findUserByLoginId(request.getUsername());

        if (data.isEmpty()) {
            return TxnResult.builder()
                    .message("Unauthorized")
                    .code("403")
                    .build();
        }

        String suppliedPassword = encryptor.encrypt(request.getPassword().trim());
        User user = data.get(0);
        String savedPassword = user.getUserPwd();

        if (!suppliedPassword.equals(savedPassword.trim())) {
            return TxnResult.builder()
                    .message("Unauthorized")
                    .code("403")
                    .build();
        }

        AuthResponse response = new AuthResponse();
        response.setEmployeeId(user.getEmployeeId());
        response.setUserName(user.getUserName());
        response.setFullName(user.getFullName());
        response.setUserRoleId(user.getUserRoleId());
        response.setEmailAddress(user.getEmailAddress());
        response.setPhoneNo(user.getPhoneNo());
        response.setReceiveBillerStmnt(user.getReceiveBillerStmnt());
        response.setLockUser(user.getLockUser());
        response.setUserPwd(user.getUserPwd());
        response.setPwdEnhancedFlag(user.getPwdEnhancedFlag());
        response.setStatus(user.getStatus());
        response.setCreatedBy(user.getCreatedBy());
        response.setCreateDt(user.getCreateDt());

        repo.updateLoginActivities(new Timestamp(new Date().getTime()), user.getEmployeeId());

        request.setStatus("success");
        request.setEmployeeId(user.getEmployeeId());

        response.setProcessDate(DataUtils.toString(DataUtils.getCurrentDate().toLocalDate()));

        return TxnResult.builder()
                .message("approved")
                .code("00")
                .data(response)
                .build();
    }




    @Transactional
    public TxnResult changePassword(PasswordChangeRequest request) throws Exception {
        List<User> employee = repo.findUserByLoginId(request.getUserName());

        if (employee.isEmpty()) {
            return TxnResult.builder()
                    .message("Invalid username specified")
                    .code("403")
                    .build();
        }

        String oldPassword = encryptor.encrypt(request.getOldPassword().trim());
        String newPassword = encryptor.encrypt(request.getNewPassword().trim());
        String confirmPassword = encryptor.encrypt(request.getConfirmPassword().trim());
        User user = employee.get(0);
        String savedPassword = user.getUserPwd().trim();

        if (!oldPassword.equals(savedPassword)) {
            return TxnResult.builder()
                    .message("Invalid old password specified")
                    .code("403")
                    .build();
        }

        if (!confirmPassword.equals(newPassword)) {
            return TxnResult.builder()
                    .message("Passwords do not match")
                    .code("403")
                    .build();
        }

        Password password = new Password();
        password.setCreateDate(new Timestamp(new Date().getTime()));
        password.setEmployeeId(user.getEmployeeId());
        password.setCreatedBy(request.getUserName());
        password.setPasswordCycle(1);
        password.setStatus("A");
        password.setEncryptedPswd(newPassword);

        repo.updatePasswordChangeFlag(user.getEmployeeId(), password.getEncryptedPswd(), "Y");

        return TxnResult.builder()
                .message("approved")
                .code("00")
                .build();
    }




    @Transactional
    public TxnResult resetUserPassword(PasswordChangeRequest request) throws Exception {
        User employee = repo.findUserByLoginEmailAddress(request.getEmailAddress());

        if (employee == null) {
            return TxnResult.builder()
                    .message("New Password will be sent to the specified email address if it is valid")
                    .code("00")
                    .build();
        }

        String newPassword = StringUtil.generateRandomString(6);

        String messageBody = "Dear " + employee.getFullName() + "\n\n" +
                "Your user account password has been reset in Micropay core agent system. Your new password is " + newPassword + "\n" +
                "Please remember to change your password to proceed \n" +
                "This is a system generated email, do not reply to this email id. \n\n" +
                "Regards\n" +
                "____________\n";

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setMessageBody(messageBody);
        emailRequest.setEmailReceipient(request.getEmailAddress());
        emailRequest.setEmailSubject("Micropay Core User account");
        emailRequest.setEmailSender(schemaConfig.getEmailUserName());

        EmailService.sendEmail(emailRequest);

        newPassword = encryptor.encrypt(newPassword);
        repo.updatePasswordChangeFlag(employee.getEmployeeId(), newPassword, "N");

        return TxnResult.builder()
                .message("New Password will be sent to the specified email address if it is valid")
                .code("00")
                .build();
    }


}