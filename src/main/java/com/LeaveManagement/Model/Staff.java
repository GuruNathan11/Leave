package com.LeaveManagement.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="Staff")
public class Staff {
	
	@Id
	private String id;
	private String empId; 
	private String name;
	private String email;
	private String contactNo;
	private String age;
	private String gender;
	private String dateOfBirth;
	private UserType userType;
	private int medicalLeaveCount;
	private int casualLeaveCount;
	private String userName;
	private String password;
	
	public enum UserType{
		Admin,HR,Staff
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

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public int getMedicalLeaveCount() {
		return medicalLeaveCount;
	}

	public void setMedicalLeaveCount(int medicalLeaveCount) {
		this.medicalLeaveCount = medicalLeaveCount;
	}

	public int getCasualLeaveCount() {
		return casualLeaveCount;
	}

	public void setCasualLeaveCount(int casualLeaveCount) {
		this.casualLeaveCount = casualLeaveCount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Staff(String id,String empId, String name,String contactNo,String age, String email, String gender, String dateOfBirth,
			UserType userType, int medicalLeaveCount,int casualLeaveCount, String userName,String password) {
		
		super();
		
		this.id = id;
		this.empId=empId;
		this.name = name;
		this.email = email;
		this.contactNo=contactNo;
		this.age=age;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.userType = userType;
		this.medicalLeaveCount=medicalLeaveCount;
		this.casualLeaveCount=casualLeaveCount;
		this.userName = userName;
		this.password = password;
	}
	public Staff build(String id, String empId,String name,String contactNo,String age, String email, String gender,
			String dateOfBirth, UserType userType,int medicalLeaveCount,int casualLeaveCount, String userName,String password) {
		return new Staff (id,empId,name,contactNo,age,email,gender,dateOfBirth,userType,medicalLeaveCount,casualLeaveCount,userName,password);
	}
	
	public void update(Staff staff)
	{
		this.setEmpId(staff.getEmpId());
		this.setName(staff.getName());
		this.setEmail(staff.getEmail());
		this.setContactNo(staff.getContactNo());
		this.setAge(staff.getAge());
		this.setGender(staff.getGender());
		this.setDateOfBirth(staff.getDateOfBirth());
		this.setUserType(staff.getUserType());
	}
	

}
