package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.QuizCreateRequest;
import com.example.fivechef.WebChef.dto.QuizResponse;
import com.example.fivechef.WebChef.dto.QuizResultResponse;
import com.example.fivechef.WebChef.dto.QuizSubmitRequest;
import com.example.fivechef.WebChef.entity.*;
import com.example.fivechef.WebChef.repository.QuizAttemptRepository;
import com.example.fivechef.WebChef.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseSessionService courseSessionService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Quiz getQuizEntity(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Optional<QuizResponse> getQuizBySessionId(Long sessionId) {
        return quizRepository.findBySessionId(sessionId)
                .map(QuizResponse::new);
    }

    @Transactional
    public void createQuiz(QuizCreateRequest request) {
        validateCreateRequest(request);

        CourseSession session = courseSessionService.getSessionEntity(request.getSessionId());

        if (quizRepository.findBySessionId(session.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 이 차시에는 퀴즈가 등록되어 있습니다.");
        }

        Quiz quiz = new Quiz();
        quiz.setSession(session);
        quiz.setTitle(request.getTitle().trim());

        for (int qi = 0; qi < request.getQuestions().size(); qi++) {
            QuizCreateRequest.QuestionRequest qReq = request.getQuestions().get(qi);

            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setContent(qReq.getContent().trim());
            question.setSortOrder(qi);

            boolean hasCorrect = false;

            for (int ci = 0; ci < qReq.getChoices().size(); ci++) {
                QuizCreateRequest.ChoiceRequest cReq = qReq.getChoices().get(ci);

                QuizChoice choice = new QuizChoice();
                choice.setQuestion(question);
                choice.setContent(cReq.getContent().trim());
                choice.setCorrect(cReq.isCorrect());
                choice.setSortOrder(ci);

                if (cReq.isCorrect()) {
                    hasCorrect = true;
                }

                question.getChoices().add(choice);
            }

            if (!hasCorrect) {
                throw new IllegalArgumentException("모든 문제는 정답이 하나 이상 있어야 합니다.");
            }

            quiz.getQuestions().add(question);
        }

        quizRepository.save(quiz);
    }

    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, QuizSubmitRequest request, String username) {
        Quiz quiz = getQuizEntity(quizId);
        User student = userService.getLoginUserEntity(username);

        Map<Long, Long> answers = request.getAnswers() == null ? new HashMap<>() : request.getAnswers();

        int total = quiz.getQuestions().size();
        int score = 0;

        for (QuizQuestion question : quiz.getQuestions()) {
            Long selectedChoiceId = answers.get(question.getId());

            if (selectedChoiceId == null) {
                continue;
            }

            boolean correct = question.getChoices().stream()
                    .anyMatch(choice -> choice.getId().equals(selectedChoiceId) && choice.isCorrect());

            if (correct) {
                score++;
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setScore(score);
        attempt.setTotalCount(total);

        quizAttemptRepository.save(attempt);

        return new QuizResultResponse(score, total);
    }

    private void validateCreateRequest(QuizCreateRequest request) {
        if (request == null || request.getSessionId() == null) {
            throw new IllegalArgumentException("차시 정보가 없습니다.");
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException("퀴즈 제목을 입력해주세요.");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("문제를 하나 이상 등록해주세요.");
        }

        for (QuizCreateRequest.QuestionRequest q : request.getQuestions()) {
            if (isBlank(q.getContent())) {
                throw new IllegalArgumentException("문제 내용을 입력해주세요.");
            }

            if (q.getChoices() == null || q.getChoices().size() < 2) {
                throw new IllegalArgumentException("보기는 2개 이상이어야 합니다.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
