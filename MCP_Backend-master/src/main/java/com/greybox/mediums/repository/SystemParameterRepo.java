package com.greybox.mediums.repository;

import com.greybox.mediums.entities.SystemParameter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SystemParameterRepo extends CrudRepository<SystemParameter, Long> {
    @Query(value = "select u.* from {h-schema}system_parameter u order by u.param_cd", nativeQuery = true)
    List<SystemParameter> findParameters();

    @Query(value = "select u.* from {h-schema}system_parameter u where u.param_cd = :param_cd order by u.param_cd", nativeQuery = true)
    SystemParameter findParameterByCode(@Param("param_cd") String parameterCode);


}