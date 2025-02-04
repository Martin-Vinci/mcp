package com.greybox.mediums.repository;

import com.greybox.mediums.entities.AccessMenu;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccessMenuRepo extends CrudRepository<AccessMenu, Integer> {

    @Query(value = "select T1.* from  {h-schema}access_menu_ref T1, {h-schema}access_menu_rights T2 where T2.menu_id = T1.menu_id and T2.user_type_id = ?1 order by T1.description", nativeQuery = true)
    List<AccessMenu> findAssignedAccessMenu(Integer memberTypeId);

    @Query(value = "select u.* from {h-schema}access_menu_ref u where u.menu_id not in (select menu_id from {h-schema}access_menu_rights where user_type_id = ?1) order by u.description", nativeQuery = true)
    List<AccessMenu> findUnAssignedAccessMenu(Integer memberTypeId);

    @Query(value = "select u.* from {h-schema}access_menu_ref u order by u.description", nativeQuery = true)
    List<AccessMenu> findAccessMenus();

    @Modifying
    @Query(value = "delete from {h-schema}access_menu_rights where menu_id = ?1 and user_type_id = ?2", nativeQuery = true)
    void deleteMemberAccessType(Integer menuId, Integer memberType);
}