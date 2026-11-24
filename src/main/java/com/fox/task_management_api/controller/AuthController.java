package com.fox.task_management_api.controller;

import com.fox.task_management_api.dto.LoginRequest;
import com.fox.task_management_api.dto.RegisterRequest;
import com.fox.task_management_api.dto.RegisterResponse;
import com.fox.task_management_api.model.User;
import com.fox.task_management_api.repository.UserRepository;
import com.fox.task_management_api.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. The password is securely encoded before being stored."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        User user = new User();
        user.setUsername(request.getUsername());

        // Encode the password before saving it
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        // Return only safe user information
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername()
        );
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful; JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        Optional<User> user =
                userRepository.findByUsername(request.getUsername());

        if (user.isPresent()
                && passwordEncoder.matches(
                request.getPassword(),
                user.get().getPassword()
        )) {

            return jwtService.generateToken(
                    user.get().getUsername()
            );
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }
}
