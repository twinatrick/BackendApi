package com.example.BackendApi.Security;

import com.example.BackendApi.DataAccess.IUserDataAccess;
import com.example.BackendApi.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserDataAccess userDataAccess;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = userDataAccess.findByEmail(username);
        if (users == null || users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new CustomUserDetails(users.getFirst());
    }
}
