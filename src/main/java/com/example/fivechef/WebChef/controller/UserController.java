//package com.example.fivechef.WebChef.controller;
//
//import com.example.fivechef.WebChef.dto.UserDTO;
//import com.example.fivechef.WebChef.entity.SiteUser;
//import com.example.fivechef.WebChef.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@RequiredArgsConstructor
//@Controller
//public class UserController {
//    private final UserService userService;
//
//    @GetMapping("/siteUser/List")
//    public String list(
//            Model model,
//            @RequestParam(value="page", defaultValue = "0") int page
//    ){
//        Page<SiteUser> paging = userService.list(page);
//        model.addAttribute("paging", paging);
//        return "stieUser/list";
//    }
//
//    @GetMapping("/siteUser/view/{id}")
//    public String view(
//            Model model,
//            @PathVariable("id") Long id,
//            UserDTO userDTO
//    ){
//        SiteUser siteUser = userService.view(id);
//        if (siteUser == null){
//            return "redirect:/";
//        }
//        model.addAttribute("siteUser", siteUser);
//        return "siteUser/view";
//    }
//
//    @GetMapping("/siteUser/chuga")
//    public String chuga(
//            Model model,
//            UserDTO userDTO
//    ){
//        return "siteUser/chuga";
//    }
//
//    @GetMapping("/siteUser/sujung/{id}")
//    public String sujung(
//            Model model,
//            @PathVariable("id") Long id
//    ){
//        SiteUser siteUser = userService.view(id);
//        model.addAttribute("siteUser", siteUser);
//        return "siteUser/sakje";
//    }
//
//    @PostMapping("/siteUser/chugaProc")
//    public String chugaProc(
//            @Valid UserDTO userDTO,
//            BindingResult bindingResult
//    ){
//        if(bindingResult.hasErrors()){
//            return "siteUser/chuga";
//        }
//
//        if(!userDTO.getPassword().equals(userDTO.getPasswordChk())){
//            bindingResult.rejectValue("passwordChk", "passwordInCorrect",
//                    "2개의 패스워드가 일치하지 않습니다.");
//            return "siteUser/chuga";
//        }
//
//        try{
//            userService.chugaProc(userDTO);
//        } catch (DataIntegrityViolationException e){
//            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
//            return "siteUser/chuga";
//        } catch (Exception e) {
//            bindingResult.reject("chugaFailed", e.getMessage());
//            return "siteUser/chuga";
//        }
//        return "redirect:/siteUser/list";
//    }
//
//    @PostMapping("/siteUser/sujungProc")
//    public String sujungPorc(
//            UserDTO userDTO
//    )
//    {
//
//        SiteUser siteUser = userService.view(userDTO.getId());
//
//        if(siteUser == null){
//            return "redirect:/";
//        }
//
//        userService.sujungProc(userDTO);
//        return "redirect:/siteUser/view/" + userDTO.getId();
//    }
//
//    @PostMapping("/siteUser/sakjeProc")
//    public String sakjeProc(
//            UserDTO userDTO
//    ){
//        userService.sakjeProc(userDTO);
//        return "redirect:/siteUser/list";
//    }
//
//    @GetMapping("/siteUser/login")
//    public String login() {
//        return "stieUser/login";
//    }
//}
