package com.eduscope.eduscope.login;

public class User {

    public String name, aftername, email, eduinst;
    public int questionsAnswered, rightAnswers, testsCompleted, ttCounter,
    ttAllTopicsCounter, trueOrFalseCounter, examCounter, guessByImageCounter,
    survivalCounter, survivalHighest, counter10s;
    public long timeInTests, timeInApp;

    public User() {

    }

    public User(String name, String aftername, String email, String eduinst, int testsCompleted, int questionsAnswered, int rightAnswers,
                int ttCounter, int ttAllTopicsCounter, int trueOrFalseCounter, int examCounter, int guessByImageCounter, int survivalCounter,
                int survivalHighest, int counter10s, long timeInTests, long timeInApp) {
        this.name = name;
        this.aftername = aftername;
        this.email = email;
        this.eduinst = eduinst;
        this.testsCompleted = testsCompleted;
        this.questionsAnswered = questionsAnswered;
        this.rightAnswers = rightAnswers;
        this.ttCounter = ttCounter;
        this.ttAllTopicsCounter = ttAllTopicsCounter;
        this.trueOrFalseCounter = trueOrFalseCounter;
        this.examCounter = examCounter;
        this.guessByImageCounter = guessByImageCounter;
        this.survivalCounter = survivalCounter;
        this.survivalHighest = survivalHighest;
        this.counter10s = counter10s;
        this.timeInTests = timeInTests;
        this.timeInApp = timeInApp;
    }
}
