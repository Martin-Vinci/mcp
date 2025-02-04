package com.greybox.mediums.repository;


import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.TransCenteTrustSummary;
import com.greybox.mediums.entities.TransactionDetail;
import com.greybox.mediums.models.CenteTrustAmountType;
import com.greybox.mediums.utils.DataUtils;
import com.greybox.mediums.utils.Logger;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class TransCenteTrustRepoImpl implements TransCenteTrustRepoCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SchemaConfig schemaConfig;

    @Override
    public void updatePostedTrustTransaction(String transType, String referenceNo, Long batchId, String status, String reason) {
        String sql = "UPDATE " + schemaConfig.getCoreSchema() + ".trans_cente_trust set status = ?, batch_id = ?, failure_reason = ?, crdb_posting_dt = current_timestamp " +
                "WHERE current_date - create_date > 0 " +
                "and reference_no = ? and trans_type = ?";
        Logger.logInfo(sql);
        jdbcTemplate.update(sql,
                status, batchId, reason, referenceNo, transType);
    }

    @Override
    public Long getTransactionRef() {
        StringBuilder queryBuilder = new StringBuilder().append("select nextval('" + schemaConfig.getCoreSchema() + ".cente_trust_posting_seq') as trans_id");
        String q = queryBuilder.toString();
        Logger.logInfo(q);
        return jdbcTemplate.query(q, rs -> {
            while (rs.next()) {
                return rs.getLong("trans_id");
            }
            return null;
        });
    }

    @Override
    public List<TransCenteTrustSummary> findPendingCenteTrustTransactions() {
        StringBuilder queryBuilder = new StringBuilder().append("select sum(amount) amount, reference_no, trans_type  from ")
                .append(schemaConfig.getCoreSchema()).append(".trans_cente_trust T1 where status = 'PENDING' and current_date - create_date > 0" +
                        " group by trans_type, reference_no");
        String q = queryBuilder.toString();
        Logger.logInfo(q);
        return jdbcTemplate.query(q, rs -> {
            List<TransCenteTrustSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(TransCenteTrustSummary.builder()
                        //.drAcctNo(rs.getString("dr_acct_no"))
                        //.crAcctNo(rs.getString("cr_acct_no"))
                        .transDescr(rs.getString("trans_type") + "~" + rs.getString("reference_no"))
                        .referenceNo(rs.getString("reference_no"))
                        .entryType(rs.getString("trans_type"))
                        .amount(rs.getBigDecimal("amount"))
                        //.createDt(DataUtils.getCurrentTimeStamp().toLocalDateTime())
                        .build());
            }
            return results;
        });
    }

    @Override
    public List<CenteTrustAmountType> findPostingAmountTypes(Integer serviceCode) {
        StringBuilder queryBuilder = new StringBuilder().append("select T1.* from ")
                .append(schemaConfig.getCoreSchema()).append(".cente_trust_amount_type T1 where T1.service_code = " + serviceCode);
        String q = queryBuilder.toString();
        Logger.logInfo(q);
        return jdbcTemplate.query(q, rs -> {
            List<CenteTrustAmountType> results = new ArrayList<>();
            while (rs.next()) {
                results.add(CenteTrustAmountType.builder()
                        .amountType(rs.getString("amount_type"))
                        .serviceCode(rs.getInt("service_code"))
                        .build());
            }
            return results;
        });
    }

    @Override
    public void deleteCenteTrustTransactionByTransId(Long transId) {
        String sql = "DELETE FROM " + schemaConfig.getCoreSchema() + ".trans_cente_trust " +
                "WHERE main_trans_id = ? ";
        Logger.logInfo(sql);
        jdbcTemplate.update(sql, transId);
    }


}