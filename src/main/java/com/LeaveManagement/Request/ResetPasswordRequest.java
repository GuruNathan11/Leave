package com.LeaveManagement.Request;

public class ResetPasswordRequest {
    
    private String newPassword;
    private String confirmNewPass;
	private String email;
	
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getConfirmNewPass() {
		return confirmNewPass;
	}
	public void setConfirmNewPass(String confirmNewPass) {
		this.confirmNewPass = confirmNewPass;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}
