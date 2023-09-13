package com.eduscope.eduscope.tests;

public class QuestionListTrueOrFalse {

    private final String question;
    private String answer;
    private final String revealAnswer;
    private String userSelectedOption;

    //Constructor:
    public QuestionListTrueOrFalse(String question, String answer, String userSelectedOption, String revealAnswer) {
        this.question = question;
        this.answer = answer;
        this.userSelectedOption = userSelectedOption;
        this.revealAnswer = revealAnswer;
    }

    //Getters:
    public String getQuestion() {return question;}
    public String getAnswer() {return answer;}
    public String getUserSelectedOption() {return userSelectedOption;}
    public String getRevealAnswer() {return revealAnswer;}
    //Setters:
    public void setAnswer(String answer) {this.answer = answer;}
    public void setUserSelectedOption(String userSelectedOption) {this.userSelectedOption = userSelectedOption;}
}
