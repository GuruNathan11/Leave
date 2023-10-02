package com.LeaveManagement.Repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.LeaveManagement.Model.CheckIn;

public interface CheckInRepository extends MongoRepository<CheckIn, String>{


	Optional<CheckIn> findByNameAndDate(String name, LocalDate date);

}
