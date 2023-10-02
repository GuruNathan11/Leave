package com.LeaveManagement.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.LeaveManagement.Model.User;

public interface UserRepository extends MongoRepository<User , String> {

	Optional<User> findByUsername(String username);
	User findByEmail(String email);
	void deleteByUsername(String username);
	Object findByPassword(String password);
	User findByResetOtpAndEmail(String otp, String email);
}
