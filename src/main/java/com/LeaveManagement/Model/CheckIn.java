package com.LeaveManagement.Model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="checkIn_checkOut")
public class CheckIn {
	
	@Id
	private String id;
	private String name;
	private LocalDate date;
	private boolean checkIn;
	private String checkInTime;
	private String checkOutTime;
	private String totalTime;
	
	public String getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public boolean isCheckIn() {
		return checkIn;
	}
	public void setCheckIn(boolean checkIn) {
		this.checkIn = checkIn;
	}
	
	public String getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}
	public String getCheckOutTime() {
		return checkOutTime;
	}
	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
	
	

}
