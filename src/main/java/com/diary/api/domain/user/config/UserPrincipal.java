package com.diary.api.domain.user.config;

import com.diary.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    public Long getId() {
        return user.getId();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // username == email
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인 시 비밀번호 없음
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한 없음
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
