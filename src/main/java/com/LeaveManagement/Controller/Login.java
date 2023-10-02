package com.LeaveManagement.Controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.LeaveManagement.Model.Session;
import com.LeaveManagement.Model.User;
import com.LeaveManagement.Repository.SessionRepository;
import com.LeaveManagement.Repository.StaffRepository;
import com.LeaveManagement.Repository.UserRepository;
import com.LeaveManagement.Request.ForgotPasswordRequest;
import com.LeaveManagement.Request.LoginRequest;
import com.LeaveManagement.Request.ResetPasswordRequest;
import com.LeaveManagement.Request.SignoutRequest;
import com.LeaveManagement.Request.VerifyOtpRequest;
import com.LeaveManagement.Request.VerifyRequest;
import com.LeaveManagement.Response.JwtResponse;
import com.LeaveManagement.Response.ResetPasswordResponse;
import com.LeaveManagement.Security.Service.UserDetailsImpl;
import com.LeaveManagement.Security.jwt.JwtUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST }, maxAge = 3600, allowedHeaders = "*")
@RestController
@RequestMapping("/api/user")
public class Login {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtUtils jwtUtils;
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	SessionRepository sessionRepository;
	
	@Autowired
	StaffRepository staffRepository;
	
	@Autowired
	UserRepository userRepository;
	
	private static final int MAX_WRONG_PASSWORD_ATTEMPTS = 3;
	private static final long TEMPORARY_BLOCK_DURATION_MINUTES = 5;
	int EXPIRATION_MINUTES = 5;

	@PostMapping("/signin")
	  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

	      // Retrieve the existing session (if any)
	      Session existingSession = sessionRepository.findByUsername(loginRequest.getUsername());
	      
	      Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

	      if (userOptional.isEmpty()) {
	          return ResponseEntity.ok().body("Invalid UserName");
	      }
	      User user = userOptional.get();

	      
	      Authentication authentication;
	      
	      try {
	          authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
	      } catch (BadCredentialsException e) {
	          int wrongPasswordAttemptCount = incrementWrongPasswordAttemptCount(loginRequest.getUsername());
	          System.out.print("count  " + wrongPasswordAttemptCount + "  ");

	          // If the maximum wrong password attempts are reached, temporarily block the user's account
	          if (wrongPasswordAttemptCount >= MAX_WRONG_PASSWORD_ATTEMPTS) {
	              blockUserAccount(loginRequest.getUsername());
	              return ResponseEntity.ok().body("Account temporarily blocked. Please try again later.");
	          }

	          return ResponseEntity.ok().body("Invalid password");
	      }
	  
	      if (existingSession != null && existingSession.getExpireTime().isAfter(LocalDateTime.now())) {
	          if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
	              // Incorrect password or organization mismatch
	              return ResponseEntity.ok().body("Invalid Password");
	          }
	          return ResponseEntity.ok().body("User already signed in. Please check");
	      } else if (existingSession != null ) {
	          // Delete the existing session
	          sessionRepository.delete(existingSession);
	      }

	      // Check if the user is temporarily blocked
	      if (isUserTemporarilyBlocked(loginRequest.getUsername())) {
	          return ResponseEntity.ok().body("Account temporarily blocked. Please try again later.");
	      }
	      // If the password is correct, reset the wrong password attempt count
	      resetWrongPasswordAttemptCount(loginRequest.getUsername());

	      // Create a new session
	      Session session = new Session();
	      session.setUsername(loginRequest.getUsername());
	      session.setSessionId(UUID.randomUUID().toString());
	      session.setCreatedDate(LocalDateTime.now());

	      // Set the expiry time as a future timestamp
	      LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
	      session.setExpireTime(expiryTime);
	      sessionRepository.save(session);

	      // Set the authentication in the SecurityContextHolder
	      SecurityContextHolder.getContext().setAuthentication(authentication);

	      // Retrieve the user type from the UserDetailsImpl
	      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	      Set<String> userType = userDetails.getAuthorities()
	              .stream()
	              .map(GrantedAuthority::getAuthority)
	              .collect(Collectors.toSet());

	      // Generate a new JWT token and cookie
	      ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails, userType);
	      
	      user.setJwt(jwtCookie.getValue());
	      userRepository.save(user);
	      int resetCount = user.getResetCount();

	      // Prepare the response body with session and JWT information
	      Map<String, Object> responseBody = new HashMap<>();
	      responseBody.put("session", session);
	      responseBody.put("jwt", new JwtResponse(jwtCookie.getValue()));
	      responseBody.put("userType", userType);
	      responseBody.put("resetCount",resetCount);
	      
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(responseBody);
	  }

	private Map<String, Integer> wrongPasswordAttemptCountMap = new HashMap<>();
	private Map<String, Boolean> blockedUsersMap = new HashMap<>();

	private int incrementWrongPasswordAttemptCount(String username) {

		String key = "WRONG_PASSWORD_ATTEMPTS_" + username;
		int attempts = getWrongPasswordAttemptCount(username) + 1;
		cacheWrongPasswordAttemptCount(key, attempts);
		return attempts;
	}
	private int getWrongPasswordAttemptCount(String username) {
		String key = "WRONG_PASSWORD_ATTEMPTS_" + username;
		Integer attempts = wrongPasswordAttemptCountMap.getOrDefault(key, 0);
		return attempts != null ? attempts : 0;
	}

	private void cacheWrongPasswordAttemptCount(String key, int attempts) {
		wrongPasswordAttemptCountMap.put(key, attempts);
	}

	private void resetWrongPasswordAttemptCount(String username) {
		String key = "WRONG_PASSWORD_ATTEMPTS_" + username;
		wrongPasswordAttemptCountMap.remove(key);
	}

	private boolean isUserTemporarilyBlocked(String username) {
		String key = "BLOCKED_USER_" + username;
		return blockedUsersMap.containsKey(key);
	}

	private void blockUserAccount(String username) {
		String key = "BLOCKED_USER_" + username;
		blockedUsersMap.put(key, true);
		// Schedule a task to unblock the user after the specified duration
		Executors.newScheduledThreadPool(1).schedule(() -> {
			blockedUsersMap.remove(key);
		}, TEMPORARY_BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
	}

@PostMapping("/verify")
	public ResponseEntity<?> verifyUser(@RequestBody VerifyRequest verifyRequest) {
	    String jwt = verifyRequest.getJwt();

	    // Verify the JWT token
	    boolean isJwtValid = jwtUtils.validateJwtToken(jwt);
	    if (!isJwtValid) {
	        return ResponseEntity.ok().body("Invalid JWT Token");
	    }

	    // Retrieve the username from the JWT token
	    String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwt);
	    if (usernameFromJwt == null) {
	        return ResponseEntity.ok().body("Failed to retrieve user details from JWT token");
	    }

	    return ResponseEntity.ok().body("Login successful");
	}

	@GetMapping("/check-login")
	public ResponseEntity<?> checkLogin(HttpServletRequest request) {
	// Get the JWT token from the request headers
	String jwtToken = jwtUtils.getJwtFromRequest(request);

	// Check if the JWT token is valid
	if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
		// Retrieve the username from the JWT token
		String username = jwtUtils.getUserNameFromJwtToken(jwtToken);

		// Retrieve the session by username from the repository
		Session existingSession = sessionRepository.findByUsername(username);

		// Check if the session exists and is not expired
		if (existingSession != null && existingSession.getExpireTime().isAfter(LocalDateTime.now())) {
			// User is signed in, return a success response
			return ResponseEntity.ok().body("User is signed in");
		} else {
			// User is not signed in or session expired, return an error response
			return ResponseEntity.ok().body("User is not signed in or session expired");
		}
	} else {
		// JWT token is invalid or not provided, return an error response
		return ResponseEntity.ok().body("JWT token is invalid or not provided");
	}
	}
	
	@PostMapping("/signout")
	public ResponseEntity<?> logoutUser(@Valid @RequestBody SignoutRequest signoutRequest, HttpServletRequest request,
			HttpServletResponse response) {

		// Verify the provided username and JWT token from the UserRepository
		Optional<User> optionalUser = userRepository.findByUsername(signoutRequest.getUsername());

		if (optionalUser.isEmpty() || !isJwtValid(optionalUser.get(), signoutRequest.getJwt())) {
			return ResponseEntity.ok().body("Invalid or expired token");
		}

		// Retrieve the existing session from the repository
		Session existingSession = sessionRepository.findByUsername(signoutRequest.getUsername());

		if (existingSession != null ) {
			// Delete the existing session
			sessionRepository.delete(existingSession);
		}

		// Get the JWT token from the request body
		String jwtToken = signoutRequest.getJwt();

		if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
			// Invalidate the JWT token
			jwtUtils.invalidateJwtToken(jwtToken);

			// Remove or set the JWT value to null in the User entity
			User user = optionalUser.get();
			user.setJwt(null); // Set the JWT value to null
			userRepository.save(user);

			// Set the JWT expiration time to the current time to invalidate the token
			Claims claims = Jwts.parser().setSigningKey(jwtUtils.getJwtSecret()).parseClaimsJws(jwtToken).getBody();
			Date expirationDate = claims.getExpiration();
			Date now = new Date();
			if (expirationDate.after(now)) {
				claims.setExpiration(now);
				jwtToken = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, jwtUtils.getJwtSecret())
						.compact();
			}

			ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
			return ResponseEntity.ok().body( "You've been signed out.");
		} else {
			return ResponseEntity.ok().body("Invalid or expired token.");
		}
	}

	// Helper method to check if the provided JWT matches the user's stored JWT
	// (handles null case)
	private boolean isJwtValid(User user, String providedJwt) {
		String storedJwt = user.getJwt();
		return providedJwt != null && storedJwt != null && storedJwt.equals(providedJwt);
	}
	
	private void sendOTPEmail(String email, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Reset Password OTP");
		message.setText("Your OTP is: " + otp);
		mailSender.send(message);
	}

	private String generateOTP() {
		int otpLength = 6;
		int min = (int) Math.pow(10, otpLength - 1);
		int max = (int) Math.pow(10, otpLength) - 1;
		return String.valueOf((int) (Math.random() * (max - min + 1) + min));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
		// Check if the email exists in the database
		User user = userRepository.findByEmail(forgotPasswordRequest.getEmail());
		if (user == null) {
			return ResponseEntity.ok().body("Email not found.");
		}
		// Generate OTP
		String otp = generateOTP();
		user.setResetOtp(otp);
		user.setResetOtpExpiration(LocalDateTime.now().plusMinutes(15)); // Set OTP expiration to 15 minutes from now
		userRepository.save(user);

		// Send OTP to the user's email
		sendOTPEmail(forgotPasswordRequest.getEmail(), otp);
		return ResponseEntity.ok("OTP sent to your registered mail. please check your inbox.");

	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOTP(@Valid @RequestBody VerifyOtpRequest verifyotp) {
		String otp = verifyotp.getOtp();
		String email = verifyotp.getEmail();

		// Find the user by the OTP

		User user = userRepository.findByResetOtpAndEmail(otp, email);

		// Check if the user exists and the OTP has not expired
		if (user == null || LocalDateTime.now().isAfter(user.getResetOtpExpiration())) {
			return ResponseEntity.ok().body("Invalid OTP or OTP has expired.");
		}
		// Reset the OTP and OTP expiration in the user object
		user.setResetOtp(null);
		user.setResetOtpExpiration(null);
		userRepository.save(user);

		return ResponseEntity.ok("OTP Verified Successfully.");
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
		// Check if the new password and confirm new password match
		if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmNewPass())) {
			return ResponseEntity.ok().body("Passwords do not match");
		}

		// Check if the email and new password are provided
		String email = resetPasswordRequest.getEmail();
		String newPassword = resetPasswordRequest.getNewPassword();
		if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
			return ResponseEntity.ok().body("Email and new password are required fields!");
		}

		// Retrieve the user from the repository
		User user = userRepository.findByEmail(email);

		// Check if the user exists
		if (user == null) {
			return ResponseEntity.ok().body("User not found");
		}

		// Update the user password
		user.setPassword(encoder.encode(newPassword));

		// Increase the reset count if password reset was successful
		user.incrementResetCount();

		// Save the user entity
		userRepository.save(user);

		// Get the reset count from the user entity
		int resetCount = user.getResetCount();

		// Create a response object with reset count and success message
		ResetPasswordResponse response = new ResetPasswordResponse("MHC - 0200", "Password reset successful.",
				resetCount);

		return ResponseEntity.ok(response);
	}
	
}