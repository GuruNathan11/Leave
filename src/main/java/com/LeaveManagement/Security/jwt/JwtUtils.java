package com.LeaveManagement.Security.jwt;

import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.LeaveManagement.Model.User;
import com.LeaveManagement.Repository.UserRepository;
import com.LeaveManagement.Security.Service.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Autowired
  UserRepository userRepository;
  
  @Value("${elon.app.jwtSecret}")
  private String jwtSecret;

  @Value("${elon.app.jwtExpirationMs}")
  private long jwtExpirationMs;

  @Value("${elon.app.jwtCookieName}")
  private String jwtCookie;
  
  private Set<String> invalidatedTokenCache;
  
  @Autowired
  JavaMailSender mailSender;
  
  public JwtUtils(JavaMailSender mailSender) {
	  this.mailSender= mailSender;
	  this.invalidatedTokenCache= ConcurrentHashMap.newKeySet();
  }

  public JwtUtils() {
      this.invalidatedTokenCache = ConcurrentHashMap.newKeySet();
  }

  public boolean isJwtTokenInvalidated(String token) {
      return invalidatedTokenCache.contains(token);
  }

  public void invalidateJwtToken(String token) {
      invalidatedTokenCache.add(token);
  }

  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, jwtCookie);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal, Set<String> userRoles) {
      String jwt = generateTokenFromUsername(userPrincipal.getUsername(), userRoles);
      ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
              .path("/")
              .maxAge(Duration.ofSeconds(1 * 60 * 60))
              .httpOnly(true)
              .secure(false)
              .sameSite("None")
              .build();
      return cookie;
  }
  
  public boolean verifyJwtAndSecretKeyFromUser(String jwt) {
      if (validateJwtToken(jwt)) {
          String username = getUserNameFromJwtToken(jwt);
          if (StringUtils.hasText(username)) {
              User user = userRepository.findByUsername(username).orElse(null);
              if (user != null ) {
                  return true; // JWT is valid and matched with User table
              } 
          } else {
              logger.error("Invalid JWT token: Unable to retrieve username from token");
          }
      } else {
          logger.error("Invalid JWT token");
      }
      return false;
  }

  public ResponseCookie getCleanJwtCookie() {
    ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
        .path("/")
        .maxAge(Duration.ofSeconds(0))
        .httpOnly(true)
        .secure(false)
        .sameSite("None")
        .build();
    return cookie;
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
  
  public String generateTokenFromUsername(String username, Set<String> userRoles) {
      Claims claims = Jwts.claims().setSubject(username);
      claims.put("roles", userRoles); // Add the user roles as a custom claim

      return Jwts.builder()
              .setClaims(claims)
              .setIssuedAt(new Date())
              .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
              .signWith(SignatureAlgorithm.HS512, jwtSecret)
              .compact();
  }
  
  public String getJwtFromRequest(HttpServletRequest request) {
	    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
	    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
	        return header.substring(7); // Extract the JWT token from the "Authorization" header
	    }
	    return null;
	}

public long getJwtExpirationMs() {	
	return jwtExpirationMs;
}

public String getJwtSecret() {
    return jwtSecret;

}
}
