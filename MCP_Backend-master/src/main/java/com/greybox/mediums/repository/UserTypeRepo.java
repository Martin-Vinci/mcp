package com.greybox.mediums.repository;

import com.greybox.mediums.entities.UserType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTypeRepo extends CrudRepository<UserType, Integer> {
    @Query(value = "select u.* from {h-schema}user_type_ref u order by u.description", nativeQuery = true)
    List<UserType> findUserTypes();

    @Query(value = "select u.* from {h-schema}user_type_ref u where u.user_type_id = ?1", nativeQuery = true)
    UserType findUserTypeByTypeId(Integer memberTypeId);
}