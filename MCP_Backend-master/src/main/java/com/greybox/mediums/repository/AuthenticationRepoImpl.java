package com.greybox.mediums.repository;

import com.greybox.mediums.config.SchemaConfig;
import com.greybox.mediums.entities.Password;
import com.greybox.mediums.models.AuthRequest;
import com.greybox.mediums.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ug.ac.mak.java.logger.Log;

import java.util.Date;

@Repository
public class AuthenticationRepoImpl implements AuthenticationRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Log logHandler;
    @Autowired
    private SchemaConfig schemaConfig;


    @Override
    public void createPassword(Password request) {
        long currentTime = new Date().getTime();
        jdbcTemplate.update("insert into system_user_pwd (employee_id, encrypted_pwd, status, password_cycle, created_by, create_date) values (?,?,?,?,?,?)",
                request.getEmployeeId(),
                request.getEncryptedPswd(),
                request.getStatus(),
                1,
                request.getCreatedBy(),
                DataUtils.getCurrentDate()
        );
    }

//    @Override
//    public String logAccessTrail(AuthRequest request) {
//        long currentTime = new Date().getTime();
//        String sessionToken = null;
//        if (request.getStatus().equals("success"))
//            sessionToken = UUID.randomUUID().toString();
//
//        jdbcTemplate.update("insert into user_login_trail (employee_id, device_id, session_token, login_dt, create_dt, status) values (?,?,?,?,?,?)",
//                request.getEmployeeId(),
//                request.getDeviceId(),
//                sessionToken,
//                new java.sql.Timestamp(currentTime),
//                new java.sql.Timestamp(currentTime),
//                request.getStatus()
//        );
//        return sessionToken;
//    }

    @Override
    public void logOutUser(AuthRequest request) {
        long currentTime = new Date().getTime();
        jdbcTemplate.update("update user_login_trail set logout_dt = ? where session_token = ? and employee_id = ?",
                new java.sql.Timestamp(currentTime),
                request.getToken(),
                request.getEmployeeId()
        );
    }

    @Override
    public void deactivateCurrentPassword(Integer employeeId) {
        jdbcTemplate.update("update system_user_pwd set status = ?, password_cycle = password_cycle + ? where employee_id = ?",
                "I",
                1,
                employeeId);
    }

    @Override
    public void reactivateOlPassword(Integer employeeId, String encryptedPassword) {
        jdbcTemplate.update("update system_user_pwd set status = ?, password_cycle = ? where encrypted_pwd = ? and employee_id = ?",
                "A",
                1,
                encryptedPassword,
                employeeId
        );
    }
}