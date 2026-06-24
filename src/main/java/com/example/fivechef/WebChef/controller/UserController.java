package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.UserDTO;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.service.UserService;
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
public class UserController {
    private final UserService userService;

    @GetMapping("/user/List")
    public String list(
            Model model,
            @RequestParam(value="page", defaultValue = "0") int page
    ){
        Page<User> paging = userService.list(page);
        model.addAttribute("paging", paging);
        return "user/list";
    }

    @GetMapping("/user/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id,
            UserDTO userDTO
    ){
        User user = userService.view(id);
        if (user == null){
            return "redirect:/";
        }
        model.addAttribute("user", user);
        return "user/view";
    }

    @GetMapping("/user/chuga")
    public String chuga(
            Model model,
            UserDTO userDTO
    ){
        return "user/chuga";
    }

    @GetMapping("/user/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id
    ){
        User user = userService.view(id);
        model.addAttribute("user", user);
        return "user/sakje";
    }

    @PostMapping("/user/chugaProc")
    public String chugaProc(
            @Valid UserDTO userDTO,
            BindingResult bindingResult
    ){
        if(bindingResult.hasErrors()){
            return "user/chuga";
        }

        if(!userDTO.getPassword().equals(userDTO.getPasswordChk())){
            bindingResult.rejectValue("passwordChk", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "user/chuga";
        }

        try{
            userService.chugaProc(userDTO);
        } catch (DataIntegrityViolationException e){
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/chuga";
        } catch (Exception e) {
            bindingResult.reject("chugaFailed", e.getMessage());
            return "user/chuga";
        }
        return "redirect:/user/list";
    }

    @PostMapping("/user/sujungProc")
    public String sujungPorc(
            UserDTO userDTO
    )
    {

        User user = userService.view(userDTO.getId());

        if(user == null){
            return "redirect:/";
        }

        userService.sujungProc(userDTO);
        return "redirect:/User/view/" + userDTO.getId();
    }

    @PostMapping("/user/sakjeProc")
    public String sakjeProc(
            UserDTO userDTO
    ){
        userService.sakjeProc(userDTO);
        return "redirect:/user/list";
    }

    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }
}
