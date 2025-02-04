package com.greybox.mediums.repository;

import com.greybox.mediums.config.SchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

public class MobileUserAccountRepoImpl implements MobileUserAccountRepoCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SchemaConfig schemaConfig;

    @Override
    public void updateAccountBalance(String accountId, BigDecimal currentBalance) {
        String sql ="UPDATE " + schemaConfig.getCoreSchema() +"." +
                "mobile_user_accounts set " +
                " cur_bal = ?, " +
                " balance_update_dt = current_date " +
                " WHERE account = ?";
        Object[] args = new Object[]{
                currentBalance,
                accountId};
        jdbcTemplate.update(sql, args);
    }
}