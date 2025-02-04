package com.greybox.mediums.repository;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.*;
import com.greybox.mediums.models.DashboardActiveOutlet;
import com.greybox.mediums.models.SearchCriteria;
import com.greybox.mediums.models.TransactionBand;
import com.greybox.mediums.models.TransactionSummary;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.Logger;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ReportRepoImpl implements ReportRepoCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SchemaConfig schemaConfig;

    public List<TransactionRef> findTransactions(TransactionRef request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        System.out.println(dateFormat.format(new Date()) + " =========== Starting transaction query");

        StringBuilder queryBuilder = (new StringBuilder()).append("SELECT T1.* from ").append(this.schemaConfig.getCoreSchema()).append(".v_transactions T1 Where 1 = 1");
        queryBuilder.append(" and DATE(T1.trans_date) >= '" + request.getStartDate() + "'");
        queryBuilder.append(" and  DATE(T1.trans_date) <= '" + request.getEndDate() + "'");
        if (request.getServiceCode() != null)
            queryBuilder.append(" and T1.service_code = ").append(request.getServiceCode());
        if (request.getPostedBy() != null)
            queryBuilder.append(" and T1.posted_by = '").append(request.getPostedBy()).append("'");
        if (request.getInitiatorPhoneNo() != null)
            queryBuilder.append(" and T1.initiator_phone_no = '").append(request.getInitiatorPhoneNo()).append("'");
        if (request.getSuccessFlag() != null) {
            queryBuilder.append(" and T1.success_flag = '").append(request.getSuccessFlag()).append("'");
        }
        queryBuilder.append(" order by T1.trans_id desc");
        String q = queryBuilder.toString();
        System.out.println(dateFormat.format(new Date()) + " =========== Query preparation completed, proceeding to execute");
        return this.jdbcTemplate.query(q, rs -> {
            System.out.println(dateFormat.format(new Date()) + " =========== Query execution completed, proceeding to prepare object");
            List<TransactionRef> results = new ArrayList<>();
            while (rs.next()) {
                results.add(TransactionRef.builder()
                        .id(Long.valueOf(rs.getLong("trans_id")))
                        .crAcctNo(rs.getString("cr_acct_no"))
                        .drAcctNo(rs.getString("dr_acct_no"))
                        .amount(rs.getBigDecimal("amount"))
                        .isoCode(rs.getString("iso_code"))
                        .postedBy(rs.getString("iso_code"))
                        .serviceCode(Integer.valueOf(rs.getInt("service_code")))
                        .transDescr(rs.getString("trans_descr"))
                        .successFlag(rs.getString("success_flag"))
                        .transDate(rs.getTimestamp("trans_date").toLocalDateTime())
                        .utilPosted(rs.getString("util_posted"))
                        .reversalFlag(rs.getString("reversal_flag"))
                        .reversalReason(rs.getString("reversal_reason"))
                        .externalTransRef(rs.getString("external_trans_ref"))
                        .depositorPhone(rs.getString("depositor_phone"))
                        .depositorName(rs.getString("depositor_name"))
                        .agentCommission(DataUtils.getPrimitiveBigDecimal(rs.getBigDecimal("agent_commission")))
                        .totalCharge(DataUtils.getPrimitiveBigDecimal(rs.getBigDecimal("total_charge")))
                        .exciseDuty(DataUtils.getPrimitiveBigDecimal(rs.getBigDecimal("excise_duty")))
                        .withholdTax(DataUtils.getPrimitiveBigDecimal(rs.getBigDecimal("withhold_tax")))
                        .bankCommission(DataUtils.getPrimitiveBigDecimal(rs.getBigDecimal("bank_commission")))
                        .systemDate(rs.getTimestamp("system_date").toLocalDateTime())
                        .externalBankCode(rs.getString("external_bank_code"))
                        .externalAcct(rs.getString("external_acct"))
                        .initiatorPhoneNo(rs.getString("initiator_phone_no"))
                        .transactionMode(rs.getString("misc_value_1"))
                        .miscValue2(rs.getString("misc_value_2"))
                        .miscValue3(rs.getString("misc_value_3"))
                        .miscValue4(rs.getString("misc_value_4"))
                        .miscValue5(rs.getString("misc_value_5"))
                        .miscValue6(rs.getString("misc_value_6"))
                        .miscValue7(rs.getString("misc_value_7"))
                        .miscValue8(rs.getString("misc_value_8"))
                        .miscValue9(rs.getString("misc_value_9"))
                        .miscValue10(rs.getString("misc_value_10"))
                        .build());
            }
            System.out.println(dateFormat.format(new Date()) + " =========== Object preparation completed, returning results.");
            return results;
        });
    }


    public List<BillerNotif> findBillerNotifications(BillerNotif request) {
        StringBuilder queryBuilder = (new StringBuilder()).append("Select T1.biller_notif_id, T1.trans_date, T1.biller_code, T1.phone_from, T1.amount, T1.iso_code, T1.reference_no, T1.trans_descr, T1.status, T1.mcp_trans_id, T1.vendor_trans_id, T1.reversal_flag, T1.reversal_reason, T1.posted_by, T1.third_party_reference, T1.response_data, T1.request_data, T1.processing_duration, T1.channel_code from ").append(this.schemaConfig.getCoreSchema()).append(".biller_notif T1 Where 1 = 1");
        queryBuilder.append(" and DATE(T1.trans_date) >= '" + request.getStartDate() + "'");
        queryBuilder.append(" and  DATE(T1.trans_date) <= '" + request.getEndDate() + "'");
        if (request.getBillerCode() != null)
            queryBuilder.append(" and T1.biller_code = '").append(request.getBillerCode()).append("'");
        if (request.getChannelCode() != null)
            queryBuilder.append(" and T1.channel_code = '").append(request.getChannelCode()).append("'");
        queryBuilder.append(" order by T1.biller_notif_id desc");
        String q = queryBuilder.toString();
        return this.jdbcTemplate.query(q, rs -> {
            List<BillerNotif> results = new ArrayList<>();
            while (rs.next()) {
                results.add(BillerNotif.builder()
                        .id(Integer.valueOf(rs.getInt("biller_notif_id")))
                        .amount(rs.getBigDecimal("amount"))
                        .isoCode(rs.getString("iso_code"))
                        .postedBy(rs.getString("posted_by"))
                        .billerCode(rs.getString("biller_code"))
                        .transDescr(rs.getString("trans_descr"))
                        .status(rs.getString("status"))
                        .transId(rs.getLong("mcp_trans_id"))
                        .transDate(rs.getTimestamp("trans_date").toLocalDateTime())
                        .reversalFlag(rs.getString("reversal_flag"))
                        .reversalReason(rs.getString("reversal_reason"))
                        .referenceNo(rs.getString("reference_no"))
                        .initiatorPhone(rs.getString("phone_from"))
                        .extenalTransRef(rs.getString("vendor_trans_id"))
                        .thirdPartyReference(rs.getString("third_party_reference"))
                        .responseData(rs.getString("response_data"))
                        .requestData(rs.getString("request_data"))
                        .processingDuration(Integer.valueOf(rs.getInt("processing_duration")))
                        .channelCode(rs.getString("channel_code"))
                        .build());
            }
            return results;
        });
    }

    public List<MobileUser> findActiveAgents(MobileUser request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        StringBuilder queryBuilder = (new StringBuilder())
                .append("SELECT T1.* from ")
                .append(this.schemaConfig.getCoreSchema())
                .append(".v_active_agents T1 Where 1 = 1");

        queryBuilder.append(" and DATE(T1.last_trans_date) >= '").append(request.getStartDate()).append("'");
        queryBuilder.append(" and DATE(T1.last_trans_date) <= '").append(request.getEndDate()).append("'");

        if (request.getAcctType() != null)
            queryBuilder.append(" and T1.acct_type = '").append(request.getAcctType()).append("'");

        String q = queryBuilder.toString();

        return this.jdbcTemplate.query(q, rs -> {
            List<MobileUser> results = new ArrayList<>();
            while (rs.next()) {
                results.add(MobileUser.builder()
                        .id(Integer.valueOf(rs.getInt("id")))
                        .phoneNumber(rs.getString("phone_number"))
                        .customerName(rs.getString("customer_name"))
                        .dateCreated(rs.getDate("date_created").toLocalDate())
                        .lockedFlag(Boolean.valueOf(rs.getBoolean("locked_flag")))
                        .acctType(rs.getString("acct_type"))
                        .failedLoginCount(Integer.valueOf(rs.getInt("failed_login_count")))
                        .pinChangeFlag(Boolean.valueOf(rs.getBoolean("pin_change_flag")))
                        .authImsi(rs.getString("auth_imsi"))
                        .authImei(rs.getString("auth_imei"))
                        .useAndroidChannel(Boolean.valueOf(rs.getBoolean("use_android_channel")))
                        .useUssdChannel(Boolean.valueOf(rs.getBoolean("use_ussd_channel")))
                        .approvalStatus(Boolean.valueOf(rs.getBoolean("approval_status")))
                        .physicalAddress(rs.getString("physical_address"))
                        .postalAddress(rs.getString("postal_address"))
                        .gender(rs.getString("gender"))
                        .lastTransDate(rs.getDate("last_trans_date").toLocalDate())
                        .activeDays(Integer.valueOf(rs.getInt("active_days")))
                        .entityCode(Integer.valueOf(rs.getInt("entity_code")))
                        .outletCode(rs.getString("outlet_code"))
                        .build());
            }
            System.out.println(dateFormat.format(new Date()) + " =========== Object preparation completed, returning results.");
            return results;
        });
    }


    public List<MobileUser> findAgentDetails(MobileUser request) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT u.* FROM ")
                .append(this.schemaConfig.getCoreSchema())
                .append(".mobile_users u WHERE u.acct_type IN ('AGENT', 'SUPER_AGENT')");

        if (request.getPhoneNumber() != null)
            queryBuilder.append(" AND u.phone_number = '").append(request.getPhoneNumber()).append("'");

        if (request.getEntityCode() != null)
            queryBuilder.append(" AND u.entity_code = '").append(request.getEntityCode()).append("'");

        if (request.getCustomerName() != null)
            queryBuilder.append(" AND u.customer_name LIKE '%").append(request.getCustomerName().toUpperCase()).append("%'");

        if (request.getOutletCode() != null)
            queryBuilder.append(" AND u.entity_code IN (SELECT T1.entity_code from ")
                    .append(this.schemaConfig.getCoreSchema())
                    .append(".mobile_users T1 WHERE T1.outlet_code = '").append(request.getOutletCode()).append("')");

        String q = queryBuilder.toString();
        Logger.logInfo(q);

        return this.jdbcTemplate.query(q, rs -> {
            List<MobileUser> results = new ArrayList<>();
            while (rs.next()) {
                results.add(MobileUser.builder()
                        .id(rs.getInt("id"))
                        .phoneNumber(rs.getString("phone_number"))
                        .customerName(rs.getString("customer_name"))
                        .dateCreated(rs.getDate("date_created").toLocalDate())
                        .lockedFlag(rs.getBoolean("locked_flag"))
                        .acctType(rs.getString("acct_type"))
                        .failedLoginCount(rs.getInt("failed_login_count"))
                        .pinChangeFlag(rs.getBoolean("pin_change_flag"))
                        .authImsi(rs.getString("auth_imsi"))
                        .authImei(rs.getString("auth_imei"))
                        .useAndroidChannel(rs.getBoolean("use_android_channel"))
                        .useUssdChannel(rs.getBoolean("use_ussd_channel"))
                        .approvalStatus(rs.getBoolean("approval_status"))
                        .physicalAddress(rs.getString("physical_address"))
                        .postalAddress(rs.getString("postal_address"))
                        .gender(rs.getString("gender"))
                        .entityCode(rs.getInt("entity_code"))
                        .companyId(rs.getInt("company_id"))
                        .outletCode(rs.getString("outlet_code"))
                        .build());
            }
            return results;
        });
    }


    public List<TransactionBand> findTransactionBands(TransactionBand request) {
        StringBuilder queryBuilder = (new StringBuilder())
                .append("SELECT Trans_Tier, sum(Trans_Count) numberOfTrans, sum(Trans_amount) Trans_amount from (select * from  ")
                .append(this.schemaConfig.getCoreSchema())
                .append(".v_transaction_bands T1 Where 1 = 1");

        queryBuilder.append(" and DATE(T1.trans_date) >= '").append(request.getStartDate()).append("'");
        queryBuilder.append(" and DATE(T1.trans_date) <= '").append(request.getEndDate()).append("'");

        if (request.getServiceCode() != null)
            queryBuilder.append(" and T1.service_code = ").append(request.getServiceCode());

        queryBuilder.append(" ) A group by Trans_Tier, tier_no order by tier_no ");

        String q = queryBuilder.toString();
        System.out.println("====================== " + q);

        return this.jdbcTemplate.query(q, rs -> {
            List<TransactionBand> results = new ArrayList<>();
            while (rs.next()) {
                results.add(TransactionBand.builder()
                        .transAmount(rs.getBigDecimal("Trans_amount"))
                        .transCount(Integer.valueOf(rs.getInt("numberOfTrans")))
                        .description(rs.getString("Trans_Tier"))
                        .build());
            }
            return results;
        });
    }


    public List<TransactionSummary> findTransactionSummary(SearchCriteria request) {
        String queryBuilder = "SELECT s.service_code, s.description, s.status, COUNT(t.*) AS Trxn_Count, " +
                "SUM(t.amount) AS Trxn_Volume, SUM(t.agent_commission) AS Agent_Commission, SUM(t.total_charge) AS Total_charge " +
                "FROM " + this.schemaConfig.getCoreSchema() + ".service_ref s, " +
                this.schemaConfig.getCoreSchema() + ".transaction_ref t " +
                "WHERE s.service_code = t.service_code AND t.success_flag ='Y'" +
                " AND t.trans_date >= ? AND t.trans_date <= ?" +
                " GROUP BY s.service_code, s.description, s.status";

        String query = queryBuilder;
        System.out.println("====================== " + query);

        return this.jdbcTemplate.query(query, new Object[]{
                request.getFromDate(),
                request.getToDate()
        }, (rs, rowNum) -> TransactionSummary.builder()
                .serviceCode(rs.getString("service_code"))
                .description(rs.getString("description"))
                .status(rs.getString("status"))
                .transactionCount(rs.getInt("Trxn_Count"))
                .transactionVolume(rs.getBigDecimal("Trxn_Volume"))
                .agentCommission(rs.getBigDecimal("Agent_Commission"))
                .totalCharge(rs.getBigDecimal("Total_charge"))
                .build());
    }

    public Integer findNewUsers(SearchCriteria request) {
        String query = "SELECT COUNT(*) AS numberOfCounts FROM " + this.schemaConfig.getCoreSchema()
                + ".mobile_users T1 WHERE acct_type = ? AND date_created >= ? AND date_created <= ?";

        System.out.println("====================== " + query);

        return this.jdbcTemplate.query(query, new Object[]{
                request.getCategories(),
                DataUtils.getFirstDayOfMonth(),
                DataUtils.getLastDayOfMonth()
        }, rs -> rs.next() ? rs.getInt("numberOfCounts") : 0);
    }

    public BigDecimal findBankCommission(SearchCriteria request) {
        String query = "SELECT SUM(T2.amount) AS total_amount FROM " + this.schemaConfig.getCoreSchema() +
                ".transaction_ref T1, " + this.schemaConfig.getCoreSchema() +
                ".transaction_details T2 WHERE T1.trans_id = T2.main_trans_id AND T1.util_posted = 'Y' " +
                "AND T2.trans_type IN ('TRANS_AMOUNT_BANK_SHARE','NET_CHARGE') AND T1.trans_date >= ? AND T1.trans_date <= ?";

        System.out.println("====================== " + query);

        return this.jdbcTemplate.query(query, new Object[]{
                DataUtils.getFirstDayOfMonth(),
                DataUtils.getLastDayOfMonth()
        }, rs -> rs.next() ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO);
    }

    public BigDecimal findBankExpenses(SearchCriteria request) {
        String query = "SELECT SUM(T2.amount) AS total_amount FROM " + this.schemaConfig.getCoreSchema() +
                ".transaction_ref T1, " + this.schemaConfig.getCoreSchema() +
                ".transaction_details T2 WHERE T1.trans_id = T2.main_trans_id AND T1.util_posted = 'Y' " +
                "AND T2.dr_acct_no IN (SELECT param_value FROM " + this.schemaConfig.getCoreSchema() +
                ".system_parameter WHERE param_cd = 'S10') AND T1.trans_date >= ? AND T1.trans_date <= ?";

        System.out.println("====================== " + query);

        return this.jdbcTemplate.query(query, new Object[]{
                DataUtils.getFirstDayOfMonth(),
                DataUtils.getLastDayOfMonth()
        }, rs -> rs.next() ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO);
    }

    public List<MobileUserAccount> findUserBalances(SearchCriteria request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        StringBuilder queryBuilder = new StringBuilder()
                .append("select T2.* from (select T1.*, case when cur_bal < 100 then 'LOW' when cur_bal between 101 and 100000 then 'MEDIUM' else 'HIGH' end as balance_level from ")
                .append(this.schemaConfig.getCoreSchema())
                .append(".mobile_user_accounts T1 where account_type = '")
                .append(request.getCategories())
                .append("') T2 where 1 = 1");

        if (request.getScope() != null) {
            queryBuilder.append(" and T2.balance_level = '").append(request.getScope()).append("'");
        }

        queryBuilder.append(" order by T2.cur_bal desc");
        String q = queryBuilder.toString();
        System.out.println("====================== " + q);

        return this.jdbcTemplate.query(q, rs -> {
            List<MobileUserAccount> results = new ArrayList<>();
            while (rs.next()) {
                MobileUserAccount item = new MobileUserAccount();
                item.setAcctNo(rs.getString("account"));
                item.setAcctType(rs.getString("account_type"));
                item.setDescription(rs.getString("account_title"));
                item.setCurrentBalance(rs.getBigDecimal("cur_bal"));
                item.setBalanceLevels(rs.getString("balance_level"));
                results.add(item);
            }
            System.out.println(dateFormat.format(new Date()) + " =========== Object preparation completed, returning results.");
            return results;
        });
    }


    public List<DashboardActiveOutlet> findActiveAgentsByTransactions(SearchCriteria request) {
        LocalDate startDate, endDate;
        if (request.getScope().equals("MONTHLY")) {
            startDate = DataUtils.getFirstDayOfMonth();
            endDate = DataUtils.getLastDayOfMonth();
        } else {
            startDate = DataUtils.getCurrentDate().toLocalDate();
            endDate = DataUtils.getCurrentDate().toLocalDate();
        }

        String queryBuilder = "select * from (select T1.posted_by, T2.customer_name, count(*) trans_count from " +
                this.schemaConfig.getCoreSchema() + ".transaction_ref T1," +
                this.schemaConfig.getCoreSchema() + ".mobile_users T2 " +
                "where T1.posted_by = T2.outlet_code and T2.acct_type = 'OUTLET' " +
                "and T1.trans_date >= '" + startDate +
                "' and T1.trans_date <= '" + endDate +
                "' group by T1.posted_by, T2.customer_name) T1" +
                " order by T1.trans_count desc";

        String q = queryBuilder;
        System.out.println("====================== " + q);

        return this.jdbcTemplate.query(q, rs -> {
            List<DashboardActiveOutlet> results = new ArrayList<>();
            while (rs.next()) {
                results.add(DashboardActiveOutlet.builder()
                        .outletNo(rs.getString("posted_by"))
                        .outletName(rs.getString("customer_name"))
                        .transCount(Integer.valueOf(rs.getInt("trans_count")))
                        .build());
            }
            return results;
        });
    }

    public List<TransactionDetail> findPendingTransactionsWithSuccessFlagY() {
        // Start building the query
        String queryBuilder = "SELECT tr.initiator_phone_no, td.trans_id, td.main_trans_id, td.cr_acct_no, " +
                "td.amount, td.iso_code, td.dr_acct_no, td.trans_type, td.trans_descr, td.posting_dt, " +
                "td.reversal_fg, td.item_no, td.create_date, td.created_by, td.modify_date, td.modified_by, " +
                "td.reversal_message, td.status " +
                "FROM " + this.schemaConfig.getCoreSchema() + ".transaction_ref tr, " +
                this.schemaConfig.getCoreSchema() + ".transaction_details td " +
                "WHERE td.main_trans_id = tr.trans_id " +
                "AND tr.success_flag = 'Y' " +
                "AND td.status = 'Pending' " +
                "AND FLOOR(EXTRACT(EPOCH FROM (current_timestamp - td.posting_dt)) / 60) >= 4 " +
                "LIMIT 300";

        String q = queryBuilder;
        Logger.logInfo("====================== " + q);

        // Execute the query and map the results
        return this.jdbcTemplate.query(q, rs -> {
            List<TransactionDetail> results = new ArrayList<>();
            while (rs.next()) {
                results.add(TransactionDetail.builder()
                        .initiatorPhoneNo(rs.getString("initiator_phone_no"))
                        .id(rs.getLong("trans_id"))
                        .mainTransId(rs.getLong("main_trans_id"))
                        .crAcctNo(rs.getString("cr_acct_no"))
                        .amount(rs.getBigDecimal("amount"))
                        .isoCode(rs.getString("iso_code"))
                        .drAcctNo(rs.getString("dr_acct_no"))
                        .transType(rs.getString("trans_type"))
                        .transDescr(rs.getString("trans_descr"))
                        .postingDt(rs.getTimestamp("posting_dt"))
                        .reversalFg(rs.getString("reversal_fg"))
                        .itemNo(rs.getInt("item_no"))
                        .createDate(rs.getTimestamp("create_date"))
                        .createdBy(rs.getString("created_by"))
                        .modifyDate(rs.getTimestamp("modify_date"))
                        .modifiedBy(rs.getString("modified_by"))
                        .reversalMessage(rs.getString("reversal_message"))
                        .status(rs.getString("status"))
                        .build());
            }
            return results;
        });
    }


    public List<MobileUser> findCustomers(MobileUser request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT ")
                .append("a.id, ")
                .append("a.phone_number, ")
                .append("a.customer_name, ")
                .append("a.date_created, ")
                .append("a.locked_flag, ")
                .append("a.acct_type, ")
                .append("a.use_android_channel, ")
                .append("a.use_ussd_channel, ")
                .append("a.birth_date, ")
                .append("a.physical_address, ")
                .append("a.postal_address, ")
                .append("a.gender, ")
                .append("a.date_created, ")
                .append("b.account, ")
                .append("b.last_activity_date, ")
                .append("b.cur_bal ")
                .append("FROM " + this.schemaConfig.getCoreSchema() + ".mobile_users a ")
                .append("JOIN " + this.schemaConfig.getCoreSchema() + ".mobile_user_accounts b ON a.id = b.user_id ")
                .append("WHERE a.acct_type = 'CUSTOMER'");

        // Example of adding date filtering if needed (customize as per request attributes)
        if (request.getStartDate() != null) {
            queryBuilder.append(" AND a.date_created >= '").append(request.getStartDate()).append("'");
        }
        if (request.getEndDate() != null) {
            queryBuilder.append(" AND a.date_created <= '").append(request.getEndDate()).append("'");
        }

        // Sorting the results by customer name
        queryBuilder.append(" ORDER BY a.customer_name");

        String q = queryBuilder.toString();

        return this.jdbcTemplate.query(q, rs -> {
            List<MobileUser> results = new ArrayList<>();
            while (rs.next()) {
                results.add(MobileUser.builder()
                        .id(rs.getInt("id"))
                        .phoneNumber(rs.getString("phone_number"))
                        .customerName(rs.getString("customer_name"))
                        .dateCreated(rs.getDate("date_created").toLocalDate())
                        .lockedFlag(rs.getBoolean("locked_flag"))
                        .acctType(rs.getString("acct_type"))
                        .useAndroidChannel(rs.getBoolean("use_android_channel"))
                        .useUssdChannel(rs.getBoolean("use_ussd_channel"))
                        .birthDate(rs.getDate("birth_date") == null ? null : rs.getDate("birth_date").toLocalDate())
                        .physicalAddress(rs.getString("physical_address"))
                        .postalAddress(rs.getString("postal_address"))
                        .gender(rs.getString("gender"))
                        .dateCreated(rs.getDate("date_created") != null ? rs.getDate("date_created").toLocalDate() : null)
                        .account(rs.getString("account"))
                        .lastActivityDate(rs.getTimestamp("last_activity_date") == null ? null : rs.getTimestamp("last_activity_date").toLocalDateTime())
                        .curBal(rs.getBigDecimal("cur_bal"))
                        .build());
            }
            return results;
        });
    }


    public List<TransactionVoucher> findTransactionVouchers(TransactionVoucher request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT ")
                .append("voucher_id, ")
                .append("amount, ")
                .append("voucher_code, ")
                .append("source_phone_no, ")
                .append("recipient_phone_no, ")
                .append("narration, ")
                .append("buy_trans_id, ")
                .append("redeem_trans_id, ")
                .append("expiry_date, ")
                .append("create_date, ")
                .append("status ")
                .append("FROM " + this.schemaConfig.getCoreSchema() + ".transaction_voucher ")
                .append("WHERE 1=1");

        // Adding date filtering based on provided start and end dates
        if (request.getStartDate() != null) {
            queryBuilder.append(" AND create_date >= '").append(request.getStartDate()).append("'");
        }
        if (request.getEndDate() != null) {
            queryBuilder.append(" AND create_date <= '").append(request.getEndDate()).append("'");
        }

        String q = queryBuilder.toString();

        return this.jdbcTemplate.query(q, rs -> {
            List<TransactionVoucher> results = new ArrayList<>();
            while (rs.next()) {
                results.add(TransactionVoucher.builder()
                        .voucherId(rs.getLong("voucher_id"))
                        .amount(rs.getBigDecimal("amount"))
                        .voucherCode(rs.getString("voucher_code"))
                        .sourcePhoneNo(rs.getString("source_phone_no"))
                        .recipientPhoneNo(rs.getString("recipient_phone_no"))
                        .narration(rs.getString("narration"))
                        .buyTransId(rs.getLong("buy_trans_id"))
                        .redeemTransId(rs.getLong("redeem_trans_id"))
                        .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
                        .createDate(rs.getDate("create_date").toLocalDate())
                        .status(rs.getString("status"))
                        .build());
            }
            return results;
        });
    }


    public List<User> findUsers(User request) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT ")
                .append("user_id, ")
                .append("user_name, ")
                .append("full_name, ")
                .append("user_role, ")
                .append("email_address, ")
                .append("phone_no, ")
                .append("receive_biller_stmnt, ")
                .append("lock_user, ")
                .append("user_pwd, ")
                .append("status, ")
                .append("created_by, ")
                .append("create_dt, ")
                .append("modify_by, ")
                .append("modify_dt, ")
                .append("pwd_enhanced_flag, ")
                .append("last_logon_date ") // Assuming last_logon_date is a column in the user_ref table
                .append("FROM " + this.schemaConfig.getCoreSchema() + ".user_ref ")
                .append("WHERE 1=1");

        // Adding filters based on provided request parameters
        if (request.getEmployeeId() != null) {
            queryBuilder.append(" AND user_id = ").append(request.getEmployeeId());
        }
        if (request.getUserName() != null) {
            queryBuilder.append(" AND user_name = '").append(request.getUserName()).append("'");
        }
        if (request.getFullName() != null) {
            queryBuilder.append(" AND full_name LIKE '%").append(request.getFullName()).append("%'");
        }
        if (request.getUserRoleId() != null) {
            queryBuilder.append(" AND user_role = ").append(request.getUserRoleId());
        }
        if (request.getStatus() != null) {
            queryBuilder.append(" AND status = '").append(request.getStatus()).append("'");
        }
        String query = queryBuilder.toString();
        return this.jdbcTemplate.query(query, rs -> {
            List<User> results = new ArrayList<>();
            while (rs.next()) {
                results.add(User.builder() // Assuming you're using a builder pattern for User
                        .employeeId(rs.getInt("user_id"))
                        .userName(rs.getString("user_name"))
                        .fullName(rs.getString("full_name"))
                        .userRoleId(rs.getInt("user_role"))
                        .emailAddress(rs.getString("email_address"))
                        .phoneNo(rs.getString("phone_no"))
                        .receiveBillerStmnt(rs.getString("receive_biller_stmnt"))
                        .lockUser(rs.getString("lock_user"))
                        .userPwd(rs.getString("user_pwd"))
                        .status(rs.getString("status"))
                        .createdBy(rs.getString("created_by"))
                        .createDt(rs.getObject("create_dt", LocalDate.class))
                        .modifyBy(rs.getString("modify_by"))
                        .modifyDt(rs.getObject("modify_dt", LocalDate.class))
                        .pwdEnhancedFlag(rs.getString("pwd_enhanced_flag"))
                        .build());
            }
            return results;
        });
    }

    public List<MobileUser> findAgentWithHoldingTax(MobileUser request) {
        String query =
                "SELECT " +
                        "    a.id, " +
                        "    a.customer_name, " +
                        "    a.phone_number, " +
                        "    ( " +
                        "        SELECT full_name " +
                        "        FROM " + this.schemaConfig.getCoreSchema() + ".user_ref t1 " +
                        "        JOIN " + this.schemaConfig.getCoreSchema() + ".mobile_users t2 ON t1.user_id = t2.rsm_id " +
                        "        WHERE t2.entity_code = a.entity_code " +
                        "    ) AS rsm, " +
                        "    a.outlet_code, " +
                        "    b.account, " +
                        "    b.cur_bal, " +
                        "    COALESCE(( " +
                        "        SELECT SUM(amount) " +
                        "        FROM " + this.schemaConfig.getCoreSchema() + ".transaction_ref td " +
                        "        WHERE td.success_flag = 'Y' and td.posted_by = a.outlet_code " +
                        "        AND td.trans_date BETWEEN '" + request.getStartDate() + "' AND '" + request.getEndDate() + "' " +
                        "    ), 0) AS trans_count, " +
                        "    COALESCE(( " +
                        "        SELECT SUM(td.amount) " +
                        "        FROM " + this.schemaConfig.getCoreSchema() + ".transaction_details td, " + this.schemaConfig.getCoreSchema() + ".transaction_ref tr " +
                        "        WHERE td.main_trans_id = tr.trans_id and tr.success_flag = 'Y' And td.created_by = a.outlet_code " +
                        "        AND td.posting_dt >= '" + request.getStartDate() + "' " +
                        "        AND td.posting_dt < '" + request.getEndDate().plusDays(1) + "'" +
                        "        AND td.trans_type = 'WITHHOLD_TAX' " +
                        "    ), 0) AS withholding_tax " +
                        " FROM " +
                        "    mcplive.mobile_users a " +
                        " JOIN " + this.schemaConfig.getCoreSchema() + ".mobile_user_accounts b ON a.id = b.user_id " +
                        " WHERE a.acct_type = 'OUTLET';";

        return this.jdbcTemplate.query(query, rs -> {
            List<MobileUser> results = new ArrayList<>();
            while (rs.next()) {
                results.add(MobileUser.builder() // Assuming you're using a builder pattern for UserData
                        .id(rs.getInt("id"))
                        .customerName(rs.getString("customer_name"))
                        .phoneNumber(rs.getString("phone_number"))
                        .rsmName(rs.getString("rsm"))
                        .outletCode(rs.getString("outlet_code"))
                        .account(rs.getString("account"))
                        .curBal(rs.getBigDecimal("cur_bal"))
                        .transCount(rs.getInt("trans_count"))
                        .witholdingTax(rs.getBigDecimal("withholding_tax"))
                        .build());
            }
            return results;
        });
    }


    public List<TransBatch> findTransBatchByPhone(String phone) {
        // Dynamically build the SQL query
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT sr.description, tb.batch_id, tb.dr_acct_no, tb.service_code, ")
                .append("tb.payment_code, tb.biller_code, tb.initiator_phone, tb.item_uuid, ")
                .append("tb.create_date, tb.status ")
                .append("FROM mcplive.trans_batch tb ")
                .append("JOIN mcplive.service_ref sr ON tb.service_code = sr.service_code ")
                .append("WHERE tb.initiator_phone = '").append(phone).append("'");

        // Convert the query builder to string
        String query = queryBuilder.toString();

        // Log the query for debugging purposes
        Logger.logInfo(query);

        // Execute the query using JdbcTemplate and map the result to TransBatch entities
        return jdbcTemplate.query(query, rs -> {
            List<TransBatch> transBatchList = new ArrayList<>();
            while (rs.next()) {
                TransBatch transBatch = new TransBatch();
                transBatch.setBatchId(rs.getLong("batch_id"));
                transBatch.setDrAcctNo(rs.getString("dr_acct_no"));
                transBatch.setServiceCode(rs.getInt("service_code"));
                transBatch.setPaymentCode(rs.getString("payment_code"));
                transBatch.setBillerCode(rs.getString("biller_code"));
                transBatch.setInitiatorPhone(rs.getString("initiator_phone"));
                transBatch.setItemUuid(rs.getString("item_uuid"));
                transBatch.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
                transBatch.setStatus(rs.getString("status"));
                transBatch.setDescription(rs.getString("description")); // Set description from service_ref

                // Add the TransBatch object to the list
                transBatchList.add(transBatch);
            }
            return transBatchList; // Return the list of TransBatch objects
        });
    }


}
