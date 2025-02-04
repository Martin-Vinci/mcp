package com.greybox.mediums.services.security;

import com.greybox.mediums.entities.Password;
import com.greybox.mediums.repository.AuthenticationRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class PasswordService {
    @Autowired
    private AuthenticationRepoImpl repo;
    public void savePassword(Password request) throws SQLException {
        repo.createPassword(request);
    }
}
