package com.eduscope.eduscope.tests;

public class QuestionList {

    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final String question;
    private String answer;
    private String userSelectedOption;

    //Constructor:
    public QuestionList(String question, String option1, String option2, String option3, String option4, String answer, String userSelectedOption) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.userSelectedOption = userSelectedOption;
    }

    public String getOption(int num) {
        switch (num) {
            case 0:
                return option1;
            case 1:
                return option2;
            case 2:
                return option3;
            case 3:
                return option4;
        }
        return "";
    }
    //Getters:

    public String getQuestion() {return question;}
    public String getAnswer() {return answer;}
    public String getUserSelectedOption() {return userSelectedOption;}
    //Setters:
    public void setAnswer(String answer) {this.answer = answer;}
    public void setUserSelectedOption(String userSelectedOption) {this.userSelectedOption = userSelectedOption;}
}
