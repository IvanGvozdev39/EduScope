package com.eduscope.eduscope.tests;

import android.graphics.Bitmap;

public class QuestionListGuessByImage {

    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private String answer;
    private final Bitmap question;
    private String userSelectedOption;

    //Constructor:
    public QuestionListGuessByImage(Bitmap question, String option1, String option2, String option3, String option4, String answer, String userSelectedOption) {
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
    public Bitmap getQuestion() {return question;}
    public String getAnswer() {return answer;}
    public String getUserSelectedOption() {return userSelectedOption;}
    //Setters:
    public void setAnswer(String answer) {this.answer = answer;}
    public void setUserSelectedOption(String userSelectedOption) {this.userSelectedOption = userSelectedOption;}
}
