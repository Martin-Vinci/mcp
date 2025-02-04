package com.greybox.mediums.repository;

import com.greybox.mediums.entities.EfrisCommodity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EfrisCommodityRepo extends CrudRepository<EfrisCommodity, Integer> {
    @Query(value = "SELECT commodity_id, commodity_category_code, commodity_category_level, commodity_category_name, parent_code FROM {h-schema}efris_commodities u where u.parent_code = ?1 order by u.commodity_category_name", nativeQuery = true)
    List<EfrisCommodity> findCommodityByParent(String parentCode);
}