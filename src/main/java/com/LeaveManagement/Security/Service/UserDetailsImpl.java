package com.LeaveManagement.Security.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.LeaveManagement.Model.User;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private String id;
    private String userName;
    private String email;
    private String password;
    private String userType;

    public UserDetailsImpl(String id, String userName, String email, String password, String userType) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getUserType()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming userType is the role, you can customize this based on your needs
        return Collections.singletonList(new SimpleGrantedAuthority(userType));
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

	
}