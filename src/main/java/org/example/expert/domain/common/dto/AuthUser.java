package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.lang.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
public class AuthUser implements UserDetails, CredentialsContainer {
    private final Long id;
    private final String email;
    private String password;
    private final UserRole userRole;
    private final String nickname;

    public AuthUser(
            Long id, 
            String email, 
            UserRole userRole, 
            String nickname
    ) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    public static AuthUser withPassword(
            Long id, 
            String email, 
            String password,
            UserRole userRole, 
            String nickname
    ) {
        AuthUser user = new AuthUser(
                id, email, userRole, nickname
        );
        user.password = password;
        return user;
    }

    @Override
    public List<UserRole> getAuthorities() {
        return List.of(userRole);
    }

    @Override
    public @Nullable String getPassword() {
      return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
