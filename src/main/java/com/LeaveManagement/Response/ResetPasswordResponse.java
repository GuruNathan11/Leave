package com.LeaveManagement.Response;

public class ResetPasswordResponse {

	private String code;
	private String description;
	private int resetCount;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getResetCount() {
		return resetCount;
	}
	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}

	public ResetPasswordResponse(String code, String description, int resetCount) {
		this.code = code;
		this.description = description;
		this.resetCount = resetCount;
	}

}