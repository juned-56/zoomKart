package com.ecom.web.app.controller;

import com.ecom.web.app.dto.UpdateUserDto;
import com.ecom.web.app.jwtService.JwtService;
import com.ecom.web.app.model.AuthRequest;
import com.ecom.web.app.model.User;
import com.ecom.web.app.repository.UserRepository;
import com.ecom.web.app.securityConfig.SecurityConfig;
import com.ecom.web.app.serviceImpl.UserServiceImpl;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

public class UserController {

    private UserServiceImpl userService;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserServiceImpl userService, JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String welcome(){
        return "<html><h1>Welcome to the ZoomKart Home page</h1></html>";
    }

    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request){
        String token = userService.extractJwtFromrequest(request);
        if(token == null){
            return ResponseEntity.status(401).body("Missing or Invalid Token");
        }
        String email = jwtService.extractUsername(token);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userService.getUserDetails(email);
        return ResponseEntity.ok(user);

    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UpdateUserDto dto, HttpServletRequest request){
        String token = userService.extractJwtFromrequest(request);
        if(token == null){
            return ResponseEntity.status(401).body("Missing or Invalid Token");
        }
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if(dto.getName() != null && !dto.getName().isEmpty()){
            user.setName(dto.getName());
        }
        if(dto.getEmail() != null && !dto.getEmail().isEmpty()){
            user.setEmail(dto.getEmail());
        }
        if(dto.getPassword() != null && !dto.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(authRequest.getUsername());
        }else{
            throw new UsernameNotFoundException("Invalid User Request!");
        }
    }
}
