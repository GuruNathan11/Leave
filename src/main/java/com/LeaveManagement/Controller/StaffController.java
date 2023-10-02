package com.LeaveManagement.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LeaveManagement.Model.Staff;
import com.LeaveManagement.Model.User;
import com.LeaveManagement.Repository.StaffRepository;
import com.LeaveManagement.Repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

	@Autowired
	StaffRepository repository;
	
	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	@GetMapping
	public ResponseEntity<?> welcome()
	{
		return ResponseEntity.ok().body("Welcome to Elon Native System");
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody Staff staff){
		
		Staff staff1 = new Staff(generateUID(),staff.getEmpId(),staff.getName(),staff.getContactNo(),staff.getAge(),staff.getEmail(),staff.getGender(),staff.getDateOfBirth(),
								staff.getUserType(),staff.getMedicalLeaveCount(),staff.getCasualLeaveCount(),staff.getUserName(),staff.getPassword());
		
		String name= staff1.getName().substring(0,4);
		String birthDate= staff1.getDateOfBirth();
		String birthYear= birthDate.substring(0,4);
		
		String username=name+birthYear;
		String password = name+"@"+birthYear;
		staff1.setUserName(username);
		staff1.setPassword(encoder.encode(password));
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(staff.getEmail());
		user.setPassword(encoder.encode(password));
		user.setUserType(staff.getUserType().toString());

		userRepository.save(user);
		
		confirmationMail(staff.getEmail(), staff.getName(),staff1.getUserName(),password);

		
		return ResponseEntity.ok().body(repository.save(staff1));
	}
	
	@GetMapping("/getByUserName/{userName}")
	public ResponseEntity<?> getByUserName(@PathVariable String userName)
	{
		Optional<Staff> staff = repository.findByUserName(userName);
		
		if(staff.isEmpty())
		{
			ResponseEntity.ok().body("No user found for given Name");
		}
		
		return ResponseEntity.ok().body(staff);
	}
	
	@GetMapping("/getAll")
	public ResponseEntity<?> getAllUser()
	{
		List<Staff> staff = repository.findAll();
		
		if(staff.isEmpty())
		{
			ResponseEntity.ok().body("No Data found");
		}
		
		return ResponseEntity.ok().body(staff);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateUser(@RequestBody Staff staff, @PathVariable String id)
	{
		Optional<Staff> staffOptional = repository.findById(id);
		
		if(staffOptional.isPresent()) {
			Staff staffToUpdate = staffOptional.get();
			staffToUpdate.update(staff);
			staffToUpdate.setId(id);
			Staff updatedUser = repository.save(staffToUpdate);
			return ResponseEntity.ok().body(updatedUser);
		}
		else
		{
			return ResponseEntity.ok().body("User Not Found for given Id");
		}
	}
	
	private void confirmationMail(String email, String name,String userName,String password) {
		// TODO Auto-generated method stub
		try {
		
			String subject="User Registration";
			String content= "Dear "+name+",<br>"+ "Your User Details Registered Successfully <br>"
					+ "Your assigned Login Credentials<br>"
					+ "UserName: "+userName+"<br>Password:"+password ;
			
			
			MimeMessage message= mailSender.createMimeMessage();
			MimeMessageHelper helper= new MimeMessageHelper(message,true);
		
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(content,true);
			mailSender.send(message);
		
		}catch(Exception e){
			System.out.println("Invalid Mail");
			e.printStackTrace();
		}
		
	}
	public static String generateUID() {
		// Generate a random alphanumeric ID with 10 digits
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			int index = (int) (Math.random() * characters.length());
			sb.append(characters.charAt(index));
		}
		return sb.toString();
	}
}
