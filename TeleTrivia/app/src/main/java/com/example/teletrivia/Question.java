package com.example.teletrivia;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Question implements Parcelable {
    private String category;
    private String question;
    private String correctAnswer;
    private List<String> allAnswers;

    public Question(String category, String question, String correctAnswer, List<String> allAnswers) {
        this.category = category;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.allAnswers = allAnswers;
    }

    // Constructor para recrear objeto desde Parcel
    protected Question(Parcel in) {
        category = in.readString();
        question = in.readString();
        correctAnswer = in.readString();
        allAnswers = new ArrayList<>();
        in.readStringList(allAnswers);
    }

    // Método que crea instancias de Question desde un Parcel
    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(question);
        dest.writeString(correctAnswer);
        dest.writeStringList(allAnswers);
    }

    public String getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getAllAnswers() {
        return allAnswers;
    }
}