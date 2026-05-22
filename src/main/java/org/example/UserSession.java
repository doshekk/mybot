package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserSession {
    private final List<Question> testQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private final LocalDateTime startTime;

    public UserSession(List<Question> allQuestions) {
        this.testQuestions = new ArrayList<>(allQuestions);
        Collections.shuffle(this.testQuestions);
        this.startTime = LocalDateTime.now();
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex < testQuestions.size()) {
            return testQuestions.get(currentQuestionIndex);
        }
        return null;
    }

    public void advance() {
        currentQuestionIndex++;
    }

    public void incrementScore() {
        score++;
    }

    public int getScore() {
        return score;
    }

    public boolean isFinished() {
        return currentQuestionIndex >= testQuestions.size();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getCurrentNumber() {
        return currentQuestionIndex + 1;
    }
}