package com.greybox.mediums.repository.mobile_loan;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.mobile_loan.LnCreditApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import ug.ac.mak.java.logger.Log;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CreditAppRepoImpl implements CreditAppRepoCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Log logHandler;
    @Autowired
    private SchemaConfig schemaConfig;

    @Override
    public List<LnCreditApp> findCreditAppl(LnCreditApp request) {
        StringBuilder queryBuilder = new StringBuilder().append("select A.*, B.first_name ||' '|| B.last_name customer_name from " + schemaConfig.getCoreSchema() + ".mbl_credit_app A, " + schemaConfig.getCoreSchema() + ".mbl_customer B where A.cust_id = B.cust_id ");
        if (request.getStatus() != null) {
            queryBuilder.append(" and A.status = '").append(request.getStatus()).append("' ");
        }
        if (request.getCustId() != null) {
            queryBuilder.append(" and A.cust_id = ").append(request.getStatus());
        }
        queryBuilder.append(" order by B.first_name desc");
        String q = queryBuilder.toString();

        return jdbcTemplate.query(q, (ResultSetExtractor<List<LnCreditApp>>) rs -> {
            List<LnCreditApp> results = new ArrayList<>();
            while (rs.next()) {
                results.add(LnCreditApp.builder()
                        .id(rs.getInt("credit_app_id"))
                        .creditType(rs.getString("credit_type"))
                        .startDate(rs.getDate("start_date").toLocalDate())
                        .endDate(rs.getDate("end_date").toLocalDate())
                        .repayTerm(rs.getInt("repay_term"))
                        .repayPeriod(rs.getString("repay_period"))
                        .nextPmtAmt(rs.getBigDecimal("nxt_pmt_amt"))
                        .status(rs.getString("status"))
                        .custId(rs.getInt("cust_id"))
                        .createdBy(rs.getString("created_by"))
                        .createDate(rs.getDate("create_date").toLocalDate())
                        .applAmt(rs.getBigDecimal("appl_amt"))
                        .customerName(rs.getString("customer_name"))
                        .rowVersion(rs.getInt("row_version"))
                        .loanPurpose(rs.getString("loan_purpose"))
                        .build());
            }
            return results;
        });
    }
}