package com.LeaveManagement.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.LeaveManagement.Model.Session;

public interface SessionRepository extends MongoRepository<Session, String> {

    Optional<Session> findBySessionId(String sessionId);

    void deleteByExpireTimeBefore(LocalDateTime expireTime);

	List<Session> findByExpireTimeBefore(LocalDateTime currentTime);

	Session findByUsername(String username);


}