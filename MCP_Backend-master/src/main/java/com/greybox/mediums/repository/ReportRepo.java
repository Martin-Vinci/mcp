package com.greybox.mediums.repository;

import com.greybox.mediums.entities.ReportData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepo extends JpaRepository<ReportData, Long>, ReportRepoCustom {

}
