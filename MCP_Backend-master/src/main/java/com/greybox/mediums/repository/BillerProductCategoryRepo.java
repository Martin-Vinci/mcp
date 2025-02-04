package com.greybox.mediums.repository;

import com.greybox.mediums.entities.Biller;
import com.greybox.mediums.entities.BillerProductCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillerProductCategoryRepo extends CrudRepository<BillerProductCategory, Integer> {
    @Query(value = "select u.* from  {h-schema}biller_prod_category u where u.biller_id = ?1 order by u.biller_prod_cat_id", nativeQuery = true)
    List<BillerProductCategory> findBillerProductCategoryById(Integer billerId);

    @Query(value = "select T1.* from {h-schema}biller_prod_category T1, {h-schema}biller_ref T2 where T1.biller_id = T2.biller_ref_id and T2.biller_code = ?1 order by T1.biller_prod_cat_id", nativeQuery = true)
    List<BillerProductCategory> findProductCategoryByBillerCode(String billerCode);
}