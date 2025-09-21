package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.AuthDTO;
import in.ralo.moneymanager.dto.ProfileDTO;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.ProfileRepo;
import in.ralo.moneymanager.service.ProfileService;
import in.ralo.moneymanager.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepo profileRepo;
    private final EmailServiceImpl emailServiceImp;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationUrl;

    @Autowired
    public ProfileServiceImpl(ProfileRepo profileRepo, EmailServiceImpl emailServiceImpl, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.profileRepo = profileRepo;
        this.emailServiceImp = emailServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        // Convert DTO → Entity
        Profile newProfile = toEntity(profileDTO);

        newProfile.setActivationToken(UUID.randomUUID().toString());

        // Save to DB
        Profile savedProfile = profileRepo.save(newProfile);

        String emailActivationLink = activationUrl + "/api/v1.0/activate?activationToken=" + savedProfile.getActivationToken();
        String emailSubject = "Activate your Money Manager Account";
        String emailBody = "Click on the following link to Activate your Account : " + emailActivationLink;
        emailServiceImp.sendMail(savedProfile.getEmail(), emailSubject, emailBody);

        // Convert Entity → DTO
        return toDTO(savedProfile);
    }

    @Override
    public boolean activateProfile(String activationToken) {
        return profileRepo.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepo.save(profile);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean isAccountActive(String email) {
        return profileRepo.findByEmail(email)
                .map(Profile::getIsActive)
                .orElse(false);
    }

    @Override
    public Profile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email " + authentication.getName()));
    }

    @Override
    public ProfileDTO getPublicProfile(String email) {
        Profile currentUser = null;

        if (email == null)
            currentUser = getCurrentProfile();
        else
            currentUser = profileRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email " + email));

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    @Override
    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // helper methods
    public Profile toEntity(ProfileDTO profileDTO) {
        if (profileDTO == null)
            return null;

        return Profile.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(Profile profile) {
        if (profile == null)
            return null;

        return ProfileDTO.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .profileImageUrl(profile.getProfileImageUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}