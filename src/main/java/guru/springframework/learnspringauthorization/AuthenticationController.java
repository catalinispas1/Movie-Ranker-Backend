package guru.springframework.learnspringauthorization;

import guru.springframework.learnspringauthorization.model.MyUser;
import guru.springframework.learnspringauthorization.model.MyUserDetailService;
import guru.springframework.learnspringauthorization.repository.MyUserRepository;
import guru.springframework.learnspringauthorization.model.UserDetailsResponse;
import guru.springframework.learnspringauthorization.webtoken.JwtService;
import guru.springframework.learnspringauthorization.webtoken.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MyUserDetailService myUserDetailService;
    @Autowired
    private MyUserRepository myUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/authenticate")
    public Map<String, String> authenticateAndGetToken(@RequestBody LoginForm loginForm) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginForm.username(), loginForm.password()
        ));
        if (authentication.isAuthenticated()) {
            return generateTokens(loginForm.username());
        } else {
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @PostMapping("register/user")
    public ResponseEntity<?> createUserAndAuthenticate(@RequestBody MyUser user) {
        try {
            if (user.getUsername().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Username must be at least 2 characters long"));
            }

            if (user.getUsername().length() > 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Username must be maximum 10 characters"));
            }

            if (user.getPassword().length() < 4) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Password must be at least 4 characters long"));
            }

            if (myUserRepository.findByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already exists"));
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            myUserRepository.save(user);

            return ResponseEntity.ok(generateTokens(user.getUsername()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred", "error", e.getMessage()));
        }
    }

    private Map<String, String> generateTokens(String username) {
        UserDetails user = myUserDetailService.loadUserByUsername(username);
        String accessToken = jwtService.generateToken(user, 15); // JWT valabil 30 min
        String refreshToken = jwtService.generateToken(user, 7 * 24 * 60); // Refresh  token va fi valabil 7 zile
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    @GetMapping("user/details")
    public UserDetailsResponse getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        MyUser user = myUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDetailsResponse(user.getId(), user.getUsername());
    }

    @PostMapping("/refresh-token")
    public Map<String, String> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Refresh token is missing");
        }

        String refreshToken = authHeader.substring(7);

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token is invalid or expired");
        }

        // Extragem user-ul din token
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = myUserDetailService.loadUserByUsername(username);

        // Generam un nou token de acces valabil 30 min
        String newAccessToken = jwtService.generateToken(userDetails, 15);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        return tokens;
    }
}
