package in.ralo.moneymanager.service;

import in.ralo.moneymanager.dto.AuthDTO;
import in.ralo.moneymanager.dto.ProfileDTO;
import in.ralo.moneymanager.model.Profile;

import java.util.Map;

public interface ProfileService {
    ProfileDTO registerProfile(ProfileDTO profileDTO);

    boolean activateProfile(String activationLink);

    boolean isAccountActive(String email);

    Profile getCurrentProfile();

    ProfileDTO getPublicProfile(String email);

    Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO);
}