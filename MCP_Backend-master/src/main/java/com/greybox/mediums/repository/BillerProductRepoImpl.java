package com.greybox.mediums.repository;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.BillerProduct;
import com.greybox.mediums.models.ISWBillerItem;
import com.greybox.mediums.utils.Logger;
import com.greybox.mediums.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BillerProductRepoImpl implements BillerProductRepoCustom {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchemaConfig schemaConfig;


    public List<BillerProduct> findMobileBillerProductsByBiller(BillerProduct request) {
        StringBuilder queryBuilder = (new StringBuilder()).append("Select T2.description category_descr, T1.* from ").append(this.schemaConfig.getCoreSchema()).append(".biller_product T1, " + this.schemaConfig.getCoreSchema() + ".biller_prod_category T2, " + this.schemaConfig.getCoreSchema() + ".biller_ref T3 Where T1.biller_prod_cat_id = T2.biller_prod_cat_id  and T1.biller_id = T2.biller_id and T1.biller_id = T3.biller_ref_id ");

        if (request.getBillerId() != null)
            queryBuilder.append("  and T1.biller_id = ").append(request.getBillerId());
        if (request.getBillerProdCatId() != null)
            queryBuilder.append(" and T2.biller_prod_cat_id = ").append(request.getBillerProdCatId());
        if (request.getBillerCode() != null)
            queryBuilder.append(" and T3.biller_code = '").append(request.getBillerCode()).append("'");
        if (request.getBillerProductId() != null)
            queryBuilder.append(" and T1.biller_prod_id = ").append(request.getBillerProductId());
        if (request.getStatus() != null)
            queryBuilder.append("  and T1.status = '").append(request.getStatus()).append("'");
        queryBuilder.append(" order by T1.biller_prod_id");
        String q = queryBuilder.toString();
        return this.jdbcTemplate.query(q, rs -> {
            List<BillerProduct> results = new ArrayList<>();

            while (rs.next()) {
                String description = rs.getString("description").trim() + " UGX " + StringUtil.toAmountDelimiter(rs.getBigDecimal("amount"));
                results.add(BillerProduct.builder()
                        .billerProductId(rs.getInt("biller_prod_id"))
                        .billerProdCatId(rs.getInt("biller_prod_cat_id"))
                        .billerId(rs.getInt("biller_id"))
                        .description(description)
                        .description2(rs.getString("description").trim())
                        .billerProdCode(rs.getString("biller_prod_code"))
                        .amount(rs.getBigDecimal("amount"))
                        .status(rs.getString("status"))
                        .categoryDescr(rs.getString("category_descr"))
                        .createdBy(rs.getString("created_by"))
                        .createDate(rs.getDate("create_date").toLocalDate())
                        .build());
            }

            return results;
        });
    }

    public List<BillerProduct> findBillerProductsByBillerId(BillerProduct request) {
        StringBuilder queryBuilder = (new StringBuilder()).append("Select T2.description category_descr, T1.* from ").append(this.schemaConfig.getCoreSchema()).append(".biller_product T1, " + this.schemaConfig.getCoreSchema() + ".biller_prod_category T2, " + this.schemaConfig.getCoreSchema() + ".biller_ref T3 Where T1.biller_prod_cat_id = T2.biller_prod_cat_id  and T1.biller_id = T2.biller_id and T1.biller_id = T3.biller_ref_id ");

        if (request.getBillerId() != null)
            queryBuilder.append("  and T1.biller_id = ").append(request.getBillerId());
        if (request.getBillerProdCatId() != null)
            queryBuilder.append(" and T2.biller_prod_cat_id = ").append(request.getBillerProdCatId());
        if (request.getBillerCode() != null)
            queryBuilder.append(" and T3.biller_code = '").append(request.getBillerCode()).append("'");
        if (request.getBillerProductId() != null)
            queryBuilder.append(" and T1.biller_prod_id = ").append(request.getBillerProductId());
        if (request.getStatus() != null)
            queryBuilder.append("  and T1.status = '").append(request.getStatus()).append("'");
        queryBuilder.append(" order by T1.biller_prod_id");
        String q = queryBuilder.toString();
        return this.jdbcTemplate.query(q, rs -> {
            List<BillerProduct> results = new ArrayList<>();

            while (rs.next()) {
                String description = rs.getString("description").trim() + " UGX " + StringUtil.toAmountDelimiter(rs.getBigDecimal("amount"));
                if (request.getChannelSource() != null)
                    description = rs.getString("description").trim();

                results.add(BillerProduct.builder()
                        .billerProductId(rs.getInt("biller_prod_id"))
                        .billerProdCatId(rs.getInt("biller_prod_cat_id"))
                        .billerId(rs.getInt("biller_id"))
                        .description(description)
                        .description2(rs.getString("description").trim())
                        .billerProdCode(rs.getString("biller_prod_code"))
                        .amount(rs.getBigDecimal("amount"))
                        .status(rs.getString("status"))
                        .categoryDescr(rs.getString("category_descr"))
                        .createdBy(rs.getString("created_by"))
                        .createDate(rs.getDate("create_date").toLocalDate())
                        .build());
            }

            return results;
        });
    }

    @Override
    public ISWBillerItem findISWPaymentItem(String billerCode, String itemId) {
        StringBuilder queryBuilder = new StringBuilder().append("select T1.biller_prod_code, T2.acct_no,  T2.biller_code from ")
                .append(schemaConfig.getCoreSchema()).append(".biller_product T1, " + schemaConfig.getCoreSchema() + ".biller_ref T2 " +
                        " Where T1.biller_id = T2.biller_ref_id ");
        queryBuilder.append(" and T2.biller_code = '").append(billerCode).append("'");
        queryBuilder.append("  and T1.biller_prod_code = '").append(itemId).append("'");
        String q = queryBuilder.toString();
        Logger.logInfo(q);
        return jdbcTemplate.query(q, rs -> {
            while (rs.next()) {
                return ISWBillerItem.builder()
                        .billerCode(rs.getString("biller_code"))
                        .billerId(rs.getString("acct_no"))
                        .itemId(rs.getString("biller_prod_code"))
                        .build();
            }
            return null;
        });
    }

    public void updateBillerProductPrice(String billerProductCode, Integer billerId, BigDecimal amount, String description) {
        String query = "UPDATE " + this.schemaConfig.getCoreSchema() + ".biller_product set amount =" + amount + ", last_update_date = current_timestamp WHERE biller_prod_code = '" + billerProductCode + "' and biller_id = " + billerId;
        System.out.println("===============>>> " + query);
        this.jdbcTemplate.update(query);
    }

    public void updateBillerProductPrice(Integer billerId) {
        this.jdbcTemplate.update("UPDATE " + this.schemaConfig.getCoreSchema() + ".biller_ref set last_update_date = current_timestamp WHERE biller_ref_id = ?", new Object[]{billerId});
    }
}
