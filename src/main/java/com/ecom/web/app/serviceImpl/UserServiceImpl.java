package com.ecom.web.app.serviceImpl;

import com.ecom.web.app.model.User;
import com.ecom.web.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Service
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<User> user = userRepository.findByEmail(username);
//        if(user.isEmpty()){
//            throw new UsernameNotFoundException("User Not found with email: " + username);
//        }
//        User user1 = user.get();
//       return new User(user1.getEmail(), user1.getPassword(), user1.getRoles());
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User Not found with email: " + username);
        }
        User user1 = user.get();

        // Convert comma-separated roles to GrantedAuthority list
        List<GrantedAuthority> authorities = Arrays.stream(user1.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority(role.trim()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user1.getEmail(),
                user1.getPassword(),
                authorities
        );
    }

    public String addUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "User Added Successfulyy!";
    }

    public String extractJwtFromrequest(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return null;
    }

    public User getUserDetails(String email){
        User dbUser = userRepository.findByEmail(email).orElseThrow(()
                -> new RuntimeException("User Not found"));
        User user = new User();
        user.setId(dbUser.getId());
        user.setName(dbUser.getName());
        user.setEmail(dbUser.getEmail());
        user.setRoles(dbUser.getRoles());
        user.setUserCreatedAt(dbUser.getUserCreatedAt());
        return user;
    }
}
