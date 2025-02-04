package com.greybox.mediums.repository;


import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.TransCenteTrustSummary;
import com.greybox.mediums.models.CenteTrustAmountType;
import com.greybox.mediums.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class TransactionRefRepoImpl implements TransactionRefCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SchemaConfig schemaConfig;

    @Override
    public void updateTxnDetailStatus(long txnId, String reversalFlag, String remarks,
                                      String status, String cbsTxnId) {
        String sql = "UPDATE " + schemaConfig.getCoreSchema() + ".transaction_details " +
                "SET reversal_fg = ?, " +
                "reversal_message = ?, " +
                "status = ?, " +
                "cbs_txn_id = COALESCE(?, cbs_txn_id) " +
                "WHERE trans_id = ?";

        // Create a StringBuilder to collect log messages
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Executing SQL query: ").append(sql)
                .append(" with parameters: txnId=").append(txnId)
                .append(", reversalFlag=").append(reversalFlag)
                .append(", remarks=").append(remarks)
                .append(", status=").append(status)
                .append(", cbsTxnId=").append(cbsTxnId);

        // Initialize success flag
        boolean success = false;

        try {
            // Perform the update operation
            jdbcTemplate.update(sql, reversalFlag, remarks, status, cbsTxnId, txnId);
            success = true;
        } catch (Exception e) {
            // Add error details to log message
            logMessage.append("\nError executing SQL query: ").append(sql)
                    .append(". Parameters: txnId=").append(txnId)
                    .append(", reversalFlag=").append(reversalFlag)
                    .append(", remarks=").append(remarks)
                    .append(", status=").append(status)
                    .append(", cbsTxnId=").append(cbsTxnId)
                    .append("\nException: ").append(e.getMessage());
            Logger.logError(e);
        } finally {
            // Log the collected messages
            if (success) {
                // Log success message
                logMessage.append("\nTransaction detail updated successfully for txnId: ").append(txnId);
            }
            // Log the final message (either success or failure)
            Logger.logError(logMessage.toString());
        }
    }


    @Override
    public void updateTransactionStatus(Long txnId, String successFlag, String utilPosted, String reversalFlag, String reason, String txnMode) {
        String sql = "UPDATE " + schemaConfig.getCoreSchema() + ".transaction_ref " +
                "SET reversal_flag = ?, " +
                "reversal_reason = ?, " +
                "util_posted = ?, " +
                "success_flag = ?, " +
                "misc_value_1 = ? " +
                "WHERE trans_id = ?";
        jdbcTemplate.update(sql, reversalFlag, reason, utilPosted, successFlag, txnMode, txnId);
    }

    @Override
    public void updateAllPendingTxnStatuses(long txnId, String reversalFlag, String remarks, String status) {
        String sql = "UPDATE " + schemaConfig.getCoreSchema() + ".transaction_details " +
                "SET reversal_fg = ?, " +
                "reversal_message = ?, " +
                "status = ? " +
                "WHERE main_trans_id = ? AND status = 'Pending'";

        // Create a StringBuilder to collect log messages
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Executing SQL query: ").append(sql)
                .append(" with parameters: txnId=").append(txnId)
                .append(", reversalFlag=").append(reversalFlag)
                .append(", remarks=").append(remarks)
                .append(", status=").append(status);

        // Initialize success flag
        boolean success = false;

        try {
            // Perform the update operation
            jdbcTemplate.update(sql, reversalFlag, remarks, status, txnId);
            success = true;
        } catch (Exception e) {
            // Add error details to log message
            logMessage.append("\nError on updateAllPendingTxnStatuses => executing SQL query: ").append(sql)
                    .append(". Parameters: txnId=").append(txnId)
                    .append(", reversalFlag=").append(reversalFlag)
                    .append(", remarks=").append(remarks)
                    .append(", status=").append(status)
                    .append("\nException: ").append(e.getMessage());
            Logger.logError(e);
        } finally {
            // Log the collected messages
            if (success) {
                // Log success message
                logMessage.append("\nTransaction statuses updated successfully for txnId: ").append(txnId);
            }
            // Log the final message (either success or failure)
            Logger.logError(logMessage.toString());
        }
    }

}