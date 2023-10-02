package com.LeaveManagement.Controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LeaveManagement.Model.CheckIn;
import com.LeaveManagement.Model.Staff;
import com.LeaveManagement.Repository.CheckInRepository;
import com.LeaveManagement.Repository.StaffRepository;

@RestController
@RequestMapping("/api/checkIn")
public class CheckInController {

	@Autowired
	CheckInRepository repository;

	@Autowired
	StaffRepository staffRepository;

	@PostMapping("/checkIn")
	public ResponseEntity<?> checkIn(@RequestBody CheckIn checkIn) {

		Optional<Staff> staffOptional = staffRepository.findByName(checkIn.getName());
		if (staffOptional.isEmpty()) {
			return ResponseEntity.ok().body("Staff Not found");
		}

		Optional<CheckIn> checkInOptional = repository.findByNameAndDate(checkIn.getName(), checkIn.getDate());

		if (checkInOptional.isPresent()) {
			CheckIn checkin = checkInOptional.get();
			checkin.setCheckIn(true);
			checkin.setCheckInTime(LocalTime.now().toString());
			checkin.setCheckOutTime(null);
			checkin.setTotalTime(null);
			repository.save(checkIn);
			return ResponseEntity.ok().body(checkin);
		}

		checkIn.setId(StaffController.generateUID());
		checkIn.setCheckIn(true);
		checkIn.setDate(LocalDate.now());
		checkIn.setCheckInTime(LocalTime.now().toString());
		checkIn.setCheckOutTime(null);
		checkIn.setTotalTime(null);

		repository.save(checkIn);

		return ResponseEntity.ok().body(checkIn);
	}

	@PostMapping("/checkOut")
	public ResponseEntity<?> checkOut(@RequestBody CheckIn checkOut) {

		Optional<Staff> staffOptional = staffRepository.findByName(checkOut.getName());
		if (staffOptional.isEmpty()) {
			return ResponseEntity.ok().body("Staff Not found");
		}

		Optional<CheckIn> checkOutOptional = repository.findByNameAndDate(checkOut.getName(), checkOut.getDate());
		
		if (checkOutOptional.isPresent()) {
			CheckIn checkout = checkOutOptional.get();
			if (checkout.isCheckIn()) {
				checkout.setCheckIn(false);
				checkout.setCheckOutTime(LocalTime.now().toString());
				String totalTime = calculateTotalTime(checkout.getCheckInTime(), checkout.getCheckOutTime());
				checkout.setTotalTime(totalTime);
				repository.save(checkout);
				return ResponseEntity.ok().body(checkout);
			}
			return ResponseEntity.ok().body("User Already Checked Out");
		}
		return ResponseEntity.ok().body("User yet to CheckIn");
	}

	private String calculateTotalTime(String startTime, String endTime) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			Date start = sdf.parse(startTime);
			Date end = sdf.parse(endTime);
			long duration = end.getTime() - start.getTime();
			long hours = duration / 3600000;
			long minutes = (duration % 3600000) / 60000;
			long seconds = (duration % 60000) / 1000;
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} catch (Exception e) {
			return "Invalid";
		}
	}

}
