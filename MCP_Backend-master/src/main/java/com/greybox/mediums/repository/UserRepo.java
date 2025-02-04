package com.greybox.mediums.repository;

import com.greybox.mediums.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface UserRepo extends CrudRepository<User, Integer> {
    @Query(value = "select u.* from  {h-schema}user_ref u where u.user_name = :user_name order by u.full_name", nativeQuery = true)
    List<User> findUserByLoginId(
            @Param("user_name") String loginId
    );

    @Query(value = "select u.* from  {h-schema}user_ref u where u.email_address = :email_address order by u.full_name", nativeQuery = true)
    User findUserByLoginEmailAddress(
            @Param("email_address") String emailAddress
    );

    @Query(value = "select u.* from {h-schema}user_ref u order by u.full_name", nativeQuery = true)
    List<User> findUsers();

    @Query(value = "select u.* from {h-schema}user_ref u where lower(u.full_name) like lower(concat('%', ?1,'%')) order by u.full_name", nativeQuery = true)
    List<User> findUsersByName(
            String entityName
    );



    @Modifying
    @Query(value = "update  {h-schema}user_ref u set last_logon_date = :last_logon_date where u.user_id = :user_id", nativeQuery = true)
    void updateLoginActivities(
            @Param("last_logon_date") Timestamp timestamp,
            @Param("user_id") Integer userId
    );

    @Modifying
    @Query(value = "update {h-schema}user_ref set user_pwd = :user_pwd, pwd_enhanced_flag = :pwd_enhanced_flag where user_id = :user_id", nativeQuery = true)
    void updatePasswordChangeFlag(
            @Param("user_id") Integer userId,
            @Param("user_pwd") String password,
            @Param("pwd_enhanced_flag") String passwordChangedFlag
    );


}