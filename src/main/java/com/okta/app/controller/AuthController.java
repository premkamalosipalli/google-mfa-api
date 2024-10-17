package com.okta.app.controller;


import com.okta.app.model.*;
import com.okta.app.service.AuthService;
import com.okta.app.service.TOTPService;
import com.okta.app.util.JWTUtil;
import com.okta.app.util.QRCodeUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private TOTPService totpService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        // Check if user already exists
        if (authService.userExists(authRequest.getUsername())) {
            return ResponseEntity.status(409).body("Username already exists");
        }

        // Log the registration attempt
        System.out.println("Registering user with username: " + authRequest.getUsername());

        // Generate TOTP Secret Key for the user
        String secretKey = totpService.generateSecretKey(); // No need to pass username

        // Instantiate the new User object
        User newUser = new User(authRequest.getUsername(), authRequest.getPassword(), secretKey);

        if(authRequest.isMfaEnabled()){
            newUser.setMfaEnabled(true);
        }
        // Save the user using AuthService
        authService.saveUser(newUser);

        return ResponseEntity.ok("User registered successfully!");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (Exception e) {
            logger.error("Authentication failed for user: " + authRequest.getUsername(), e);
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        final UserDetails userDetails = authService.loadUserByUsername(authRequest.getUsername());

        String token;
        try {
            token = jwtUtil.generateToken(userDetails.getUsername());
        } catch (Exception e) {
            logger.error("Token generation failed for user: " + userDetails.getUsername(), e);
            return ResponseEntity.status(500).body("Could not generate token");
        }

        // Check if MFA is enabled for the user
        User user = authService.getUserByUsername(authRequest.getUsername());
        if (user.isMfaEnabled()) {
            return ResponseEntity.ok("JWT Token: " + token + ", MFA Required");
        } else {
            return ResponseEntity.ok("JWT Token: " + token + ", Login successful");
        }
    }
    @PostMapping("/verifyMFA")
    public ResponseEntity<?> verifyMFA(@RequestBody TOTPRequest mfaRequest) {
        User user = authService.getUserByUsername(mfaRequest.getUsername());

        // Check if user exists and if MFA is enabled
        if (user == null || !user.isMfaEnabled()) {
            return ResponseEntity.status(403).body("MFA is not enabled for this user");
        }

        // Create a GoogleAuthenticator instance
        GoogleAuthenticator gAuth = new GoogleAuthenticator();

        // Validate the TOTP code
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // Use a method to get the TOTP password for comparison
        int generatedTOTP = gAuth.getTotpPassword(user.getSecretKey());

        // Compare the input code with the generated TOTP
        boolean isCodeValid = (Integer.parseInt(mfaRequest.getTotp()) == generatedTOTP);

        if (isCodeValid) {
            // Proceed with login or return success response
            final String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok("JWT Token: " + token + ", Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid MFA code");
        }
    }

    @PostMapping("/setupMFA")
    public ResponseEntity<?> setupMFA(@RequestBody AuthRequest authRequest) {
        String username = authRequest.getUsername();
        String secretKey = totpService.generateSecretKey(); // Generate secret key
        try {
            String qrCodeBase64 = QRCodeUtil.generateQRCode(secretKey, username); // Generate QR code

            // Log the QR code base64 for debugging
            System.out.println("QR Code Base64: " + qrCodeBase64); // Debugging line

            // Save the secret key to the user record in the database
            User user = authService.getUserByUsername(username);
            user.setSecretKey(secretKey);
            authService.saveUser(user); // Assuming you have a method to save user data

            // Send QR code back to the client as part of an object
            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("qrCodeUrl", "data:image/png;base64," + qrCodeBase64); // Wrap the QR code in an object
            }});
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating QR Code");
        }
    }



}
