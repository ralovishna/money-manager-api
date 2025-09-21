package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.ProfileRepo;
import in.ralo.moneymanager.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AppUserDetailsServiceImpl implements AppUserDetailsService {

    private final ProfileRepo profileRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Profile existingProfile = profileRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email " + email));

        return User.builder()
                .username(existingProfile.getEmail())
                .password(existingProfile.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}