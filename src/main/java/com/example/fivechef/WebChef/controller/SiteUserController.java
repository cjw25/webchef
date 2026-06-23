package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.SiteUserDTO;
import com.example.fivechef.WebChef.entity.SiteUser;
import com.example.fivechef.WebChef.service.SiteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class SiteUserController {
    private final SiteUserService siteUserService;

    @GetMapping("/siteUser/List")
    public String list(
            Model model,
            @RequestParam(value="page", defaultValue = "0") int page
    ){
        Page<SiteUser> paging = siteUserService.list(page);
        model.addAttribute("paging", paging);
        return "stieUser/list";
    }

    @GetMapping("/siteUser/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id,
            SiteUserDTO siteUserDTO
    ){
        SiteUser siteUser = siteUserService.view(id);
        if (siteUser == null){
            return "redirect:/";
        }
        model.addAttribute("siteUser", siteUser);
        return "siteUser/view";
    }

    @GetMapping("/siteUser/chuga")
    public String chuga(
            Model model,
            SiteUserDTO siteUserDTO
    ){
        return "siteUser/chuga";
    }

    @GetMapping("/siteUser/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id
    ){
        SiteUser siteUser = siteUserService.view(id);
        model.addAttribute("siteUser", siteUser);
        return "siteUser/sakje";
    }

    @PostMapping("/siteUser/chugaProc")
    public String chugaProc(
            @Valid SiteUserDTO siteUserDTO,
            BindingResult bindingResult
    ){
        if(bindingResult.hasErrors()){
            return "siteUser/chuga";
        }

        if(!siteUserDTO.getPassword().equals(siteUserDTO.getPasswordChk())){
            bindingResult.rejectValue("passwordChk", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "siteUser/chuga";
        }

        try{
            siteUserService.chugaProc(siteUserDTO);
        } catch (DataIntegrityViolationException e){
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "siteUser/chuga";
        } catch (Exception e) {
            bindingResult.reject("chugaFailed", e.getMessage());
            return "siteUser/chuga";
        }
        return "redirect:/siteUser/list";
    }

    @PostMapping("/siteUser/sujungProc")
    public String sujungPorc(
            SiteUserDTO siteUserDTO
    )
    {

        SiteUser siteUser = siteUserService.view(siteUserDTO.getId());

        if(siteUser == null){
            return "redirect:/";
        }

        siteUserService.sujungProc(siteUserDTO);
        return "redirect:/siteUser/view/" + siteUserDTO.getId();
    }

    @PostMapping("/siteUser/sakjeProc")
    public String sakjeProc(
            SiteUserDTO siteUserDTO
    ){
        siteUserService.sakjeProc(siteUserDTO);
        return "redirect:/siteUser/list";
    }

    @GetMapping("/siteUser/login")
    public String login() {
        return "stieUser/login";
    }
}
