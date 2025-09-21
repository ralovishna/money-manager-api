package in.ralo.moneymanager.controller;

import in.ralo.moneymanager.dto.AuthDTO;
import in.ralo.moneymanager.dto.ProfileDTO;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@RequestBody ProfileDTO profileDTO) {
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String activationToken) {
        boolean isActivated = profileService.activateProfile(activationToken);

        if (isActivated)
            return ResponseEntity.ok("Profile Activated Successfully");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            if (!profileService.isAccountActive(authDTO.getEmail()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of(
                                "message", "Account is not active. Please activate your account first."
                        ));

            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/test")
    public String test() {
        return "Test Successful.";
    }
}