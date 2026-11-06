package com.example.demo.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public CustomUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return jdbc.query("SELECT id,email,password_hash,role,enabled,account_non_locked FROM users WHERE email = ?",
            rs -> {
                if (!rs.next()) throw new UsernameNotFoundException("Invalid credentials");
                String hash = rs.getString("password_hash");
                if (hash == null) throw new UsernameNotFoundException("Invalid credentials");
                return new CustomUserDetails(rs.getLong("id"), rs.getString("email"), hash,
                    rs.getString("role"), rs.getBoolean("enabled"), rs.getBoolean("account_non_locked"));
            }, email);
    }
}
