package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.SiteUserDTO;
import com.example.fivechef.WebChef.entity.SiteUser;
import com.example.fivechef.WebChef.exception.DataNotFoundException;
import com.example.fivechef.WebChef.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SiteUserService {

    private final SiteUserRepository siteUserRepository;

    public Page<SiteUser> list(int page){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return siteUserRepository.findAll(pageable);
    }

    public SiteUser view(Long id){
        Optional<SiteUser> oq = siteUserRepository.findById(id);
        SiteUser siteUser = null;
        if (oq.isPresent()){
            siteUser = oq.get();
        }
        return siteUser;
    }

    public void chugaProc(SiteUserDTO siteUserDTO){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(siteUserDTO.getPassword());

        SiteUser siteUser = new SiteUser();
        siteUser.setId(siteUserDTO.getId());
        siteUser.setUsername(siteUserDTO.getUsername());
        siteUser.setPassword(password);
        siteUser.setEmail(siteUserDTO.getEmail());
        siteUserRepository.save(siteUser);
    }

    public void sujungProc(SiteUserDTO siteUserDTO){
        SiteUser siteUser = new SiteUser();
        siteUser.setId(siteUserDTO.getId());
        siteUser.setUsername(siteUserDTO.getUsername());
        siteUser.setPassword(siteUserDTO.getPassword());
        siteUser.setEmail(siteUserDTO.getEmail());
        siteUserRepository.save(siteUser);
    }

    public void sakjeProc(SiteUserDTO siteUserDTO){
        SiteUser siteUser = new SiteUser();
        siteUser.setId(siteUserDTO.getId());
        siteUser.setUsername(siteUserDTO.getUsername());
        siteUser.setPassword(siteUserDTO.getPassword());
        siteUser.setEmail(siteUserDTO.getEmail());
        siteUserRepository.delete(siteUser);
    }

    public SiteUser getUser(String username){
        Optional<SiteUser> siteUser = siteUserRepository.findByUsername(username);
        if (siteUser.isPresent()){
            return siteUser.get();
        } else{
            throw new DataNotFoundException("siteuser not found");
        }
    }

}
