package com.kendar.finance.service;

import com.kendar.finance.data.UsersRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SimpleAuthenticationProvider implements AuthenticationProvider {
    private final UsersRepository usersRepository;

    public SimpleAuthenticationProvider(UsersRepository usersRepository){

        this.usersRepository = usersRepository;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        var authorities = new ArrayList<GrantedAuthority>();
        if(name.equalsIgnoreCase("admin") && password.equalsIgnoreCase("password")){
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
            authorities.add(new SimpleGrantedAuthority("USER"));
        } else if(userExists(name,password)){
            authorities.add(new SimpleGrantedAuthority("USER"));
        } else{
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
                name, password, new ArrayList<>());
    }

    private boolean userExists(String name, String password) {
        return usersRepository.findByLoginPassword(name,password)!=null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
