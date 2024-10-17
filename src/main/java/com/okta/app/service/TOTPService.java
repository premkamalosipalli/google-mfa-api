package com.okta.app.service;


import com.okta.app.model.User;
import com.okta.app.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TOTPService {

    @Autowired
    private UserRepository userRepository;

    public String generateSecretKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();  // Return the generated key without looking up the user
    }

    public boolean verifyTOTP(String username, int totp) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getSecretKey() == null) {
            throw new RuntimeException("User not found or secret key is not generated.");
        }
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.authorize(user.getSecretKey(), totp);
    }
}
