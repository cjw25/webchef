package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AnswerDTO;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.service.AnswerService;
import com.example.fivechef.WebChef.service.CommunityService;
import com.example.fivechef.WebChef.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class AnswerController {
    private final AnswerService answerService;
    private final CommunityService communityService;
    private final UserService userService;

    @GetMapping("/answer/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id,
            AnswerDTO answerDTO,
            Principal principal
    ) {
        Answer answer = answerService.view(id);

        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        answerDTO.setId(answer.getId());
        answerDTO.setContent(answer.getContent());

        return "answer/sujung";
    }

    @GetMapping("/answer/sakje/{id}")
    public String sakje(
        Model model,
        @PathVariable("id") Long id,
        AnswerDTO answerDTO,
        Principal principal
    ){
       Answer answer = answerService.view(id);

       if(answer == null){
           return "redirect:/";
       }

       if(!answer.getAuthor().getUsername().equals(principal.getName())){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
       }

       answerDTO.setId(id);
       answerDTO.setContent(answer.getContent());
       answerDTO.setCommunityId(answer.getCommunity().getId());
       return "answer/sakje";
        }

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/answer/chugaProc")
        public String chugaProc(
                Model model,
                @Valid AnswerDTO answerDTO,
                BindingResult bindingResult,
                Principal principal
        ){
            Community community = this.communityService.view(answerDTO.getCommunityId());
            User user = userService.getUser(principal.getName());

            if (bindingResult.hasErrors()){
                model.addAttribute("community", community);
                return "community/view";
            }
            Answer answer = answerService.chugaProc(answerDTO, user);
            return "redirect:/community/view" + answerDTO.getCommunityId() + "#answer_" + answer.getId();
        }

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/answer/sujungProc")
        public String sujungProc(
                Model model,
                @Valid AnswerDTO answerDTO,
                BindingResult bindingResult,
                Principal principal
        ){
            if(bindingResult.hasErrors()){
                return "answer/sujung";
            }
            Answer answer = answerService.view(answerDTO.getId());
            if (answer == null){

            }

            if(!answer.getAuthor().getUsername().equals(principal.getName())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }

            answerDTO.setCreateDate(answer.getCreateDate());
            answerDTO.setCommunityId(answer.getCommunity().getId());

            User user = userService.getUser(principal.getName());

            answerService.sujungProc(answerDTO, user);
            return "redirect:/community/view" + answerDTO.getCommunityId()+ "#answer_" + answerDTO.getId();
        }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/sakjeProc")
    public String sakjeProc(
            Model model,
            @Valid AnswerDTO answerDTO,
            BindingResult bindingResult,
            Principal principal
    ){
        if(bindingResult.hasErrors()){
            return "answer/sakje";
        }
        Answer answer = answerService.view(answerDTO.getId());
        if (answer == null){
            return "redirect:/";
        }

        if(!answer.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        answerDTO.setCommunityId(answer.getCommunity().getId());

        User user = userService.getUser(principal.getName());

        answerService.sujungProc(answerDTO, user);
        return "redirect:/community/view" + answerDTO.getCommunityId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/vote/{id}")
    public String vote(
            @PathVariable("id") Long id,
            Principal principal
    ){
        Answer answer = answerService.view(id);
        if (answer == null) {
            return "redirect:/";
        }

        User user = userService.getUser(principal.getName());
        answerService.vote(answer, user);
        return "redirect:/community/view" + answer.getCommunity().getId();
    }


}
}
