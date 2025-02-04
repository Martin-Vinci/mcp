package com.greybox.mediums.repository;

import com.greybox.mediums.entities.AccessMenuRight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessMenuRightRepo extends CrudRepository<AccessMenuRight, Integer> {
}