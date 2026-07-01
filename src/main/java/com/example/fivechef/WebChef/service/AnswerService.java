package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.AnswerDTO;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final CommunityService communityService;

    public Answer view(Long id){
        Optional<Answer> optionalAnswer = answerRepository.findById(id);
        Answer answer = null;
        if (optionalAnswer.isPresent()){
            answer = optionalAnswer.get();
        }
        return answer;
    }

    public Answer chugaProc(AnswerDTO answerDTO, User user){
        Community community = communityService.view(answerDTO.getCommunityId());

        Answer answer = new Answer();
        answer.setContent(answerDTO.getContent());
        answer.setCommunity(community);
        answer.setCreateDate(LocalDateTime.now());
        answer.setAuthor(user);

        answerRepository.save(answer);

        return answer;
    }

    public Answer sujungProc(AnswerDTO answerDTO, User user){
        Community community = communityService.view(answerDTO.getCommunityId());

        Answer answer = new Answer();
        answer.setId(answerDTO.getId());
        answer.setContent(answerDTO.getContent());
        answer.setCommunity(community);
        answer.setCreateDate(answerDTO.getCreateDate());
        answer.setModifyDate(LocalDateTime.now());
        answer.setAuthor(user);

        answerRepository.save(answer);

        return answer;
    }

    public void sakjeProc(Answer answer){
        answerRepository.delete(answer);
    }

    public void vote(Answer answer, User user){
        answer.getVoter().add(user);
        answerRepository.save(answer);
    }
}