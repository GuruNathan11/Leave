package com.LeaveManagement.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.LeaveManagement.Model.Leave;

public interface LeaveRepository extends MongoRepository<Leave, String> {

	Optional<Leave> findByUserName(String userName);

	List<Leave> findAllByStartDateAndEndDateBetween(String startDate, String endDate);

//	List<Leave> findAllByStartDateBetweenAndEndDateBetween(String startDate, String endDate);

	List<Leave> findAllByStartDateBetween(String startDate, String endDate);





}
