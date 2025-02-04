package com.greybox.mediums.security.user;

import com.greybox.mediums.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailData implements UserDetails {

    private final User employee;

    public UserDetailData(User employee) {
        this.employee = employee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ///Set<EmployeeRole> roles = employee.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

//        for (EmployeeRole employeeRole : roles) {
//            authorities.add(new SimpleGrantedAuthority(employeeRole.getName()));
//        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
