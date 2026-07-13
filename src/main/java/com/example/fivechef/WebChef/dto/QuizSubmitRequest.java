package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class QuizSubmitRequest {

    // key: questionId, value: 선택한 choiceId
    private Map<Long, Long> answers;
}
