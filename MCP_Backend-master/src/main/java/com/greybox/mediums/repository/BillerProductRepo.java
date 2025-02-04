package com.greybox.mediums.repository;

import com.greybox.mediums.entities.BillerProduct;
import com.greybox.mediums.entities.MobileUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface BillerProductRepo extends CrudRepository<BillerProduct, Integer>, BillerProductRepoCustom {
    @Query(value = "select u.* from {h-schema}biller_product u WHERE biller_prod_code = ?1 and u.biller_id = ?2", nativeQuery = true)
    BillerProduct findBillerProduct(String billerProductCode, Integer billerId);
}