package com.LeaveManagement.Request;

import javax.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    
    @NotBlank
    private String username;

    @NotBlank
    private String password;
    
    
   public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}