package com.greybox.mediums.repository;

import com.greybox.mediums.entities.Password;
import com.greybox.mediums.models.AuthRequest;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepo {
    void createPassword(Password request);
    void logOutUser(AuthRequest request);
    void deactivateCurrentPassword(Integer employeeId);
    void reactivateOlPassword(Integer employeeId, String encryptedPassword);
}
