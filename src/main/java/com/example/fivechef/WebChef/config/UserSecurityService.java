//package com.example.fivechef.WebChef.config;
//
//import com.example.fivechef.WebChef.entity.Role;
//import com.example.fivechef.WebChef.entity.SiteUser;
//import com.example.fivechef.WebChef.repository.SiteUserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@RequiredArgsConstructor
//@Service
//public class UserSecurityService implements UserDetailsService {
//    private final SiteUserRepository siteUserRepository;
//
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Optional<SiteUser> optionalSiteUser = siteUserRepository.findByUsername(username);
//        if (optionalSiteUser.isEmpty()) {
//            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
//        }
//
//        SiteUser siteUser = optionalSiteUser.get();
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        if ("admin".equals(username)) {
//            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
//        } else {
//            authorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
//        }
//        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
//    }
//}