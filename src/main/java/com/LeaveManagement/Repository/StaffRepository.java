package com.LeaveManagement.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.LeaveManagement.Model.Staff;

public interface StaffRepository extends MongoRepository<Staff, String> {

	Optional<Staff> findByUserName(String userName);
	Optional<Staff> findByName(String name);


}
