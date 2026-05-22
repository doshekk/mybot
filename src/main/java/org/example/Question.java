package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    private final String text;
    private final List<String> options;
    private final String correctAnswer;

    public Question(String text, String correctAnswer, String... wrongAnswers) {
        this.text = text;
        this.correctAnswer = correctAnswer;
        this.options = new ArrayList<>();
        this.options.add(correctAnswer);
        for (String wrongAnswer : wrongAnswers) {
            this.options.add(wrongAnswer);
        }
    }

    public String getText() {
        return text;
    }

    public List<String> getShuffledOptions() {
        List<String> shuffled = new ArrayList<>(options);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public boolean isCorrect(String answer) {
        return correctAnswer.equals(answer);
    }
}
