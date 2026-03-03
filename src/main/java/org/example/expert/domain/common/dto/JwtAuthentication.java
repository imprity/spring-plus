package org.example.expert.domain.common.dto;

import java.util.List;

import org.example.expert.domain.user.enums.UserRole;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.util.Assert;

public class JwtAuthentication implements Authentication, CredentialsContainer {
    private String jwtToken;
    private final AuthUser authUserDetails;
    private boolean authenticated;

    public JwtAuthentication(String jwtToken, AuthUser authUserDetails, boolean authenticated) {

        this.jwtToken = jwtToken;
        this.authUserDetails = authUserDetails;
        this.authenticated = authenticated;

        // 비밀번호는 필요 없으므로 지우기
        this.authUserDetails.eraseCredentials();
    }

    @Override
    public String getName() {
        return authUserDetails.getUsername();
    }

    @Override
    public List<UserRole> getAuthorities() {
        return authUserDetails.getAuthorities();
    }

    @Override
    public @Nullable Object getCredentials() {
        return jwtToken;
    }

    @Override
    public @Nullable Object getDetails() {
        return null;
    }

    @Override
    public AuthUser getPrincipal() {
        return this.authUserDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(
                !isAuthenticated,
                "Cannot set this token to trusted"
                        + " - "
                        + "use constructor which takes a GrantedAuthority list instead");

        this.authenticated = false;
    }

    @Override
    public void eraseCredentials() {
        this.jwtToken = null;
        authUserDetails.eraseCredentials();
    }
}
