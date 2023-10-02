package com.LeaveManagement.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="Leave")
public class Leave {

	@Id
	private String id;
	private String userName;
	private String startDate;
	private String endDate;
	private LeaveType LeaveType;
	private String reason; 
	private Status status;
	private String Comments;
	
	public enum Status{
		Pending,Approved,Rejected
	}
	
	public enum LeaveType{
		CasualLeave, MedicalLeave
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public LeaveType getLeaveType() {
		return LeaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		LeaveType = leaveType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getComments() {
		return Comments;
	}

	public void setComments(String comments) {
		Comments = comments;
	}

	public void update(Leave leaveRequest) {
		// TODO Auto-generated method stub
		
		this.setId(leaveRequest.getId());
		this.setUserName(leaveRequest.getUserName());
		this.setStartDate(leaveRequest.getStartDate());
		this.setEndDate(leaveRequest.getEndDate());
		this.setLeaveType(leaveRequest.getLeaveType());
		this.setReason(leaveRequest.getReason());
		this.setStatus(leaveRequest.getStatus());
		this.setComments(leaveRequest.getComments());
	}
	
	
}
