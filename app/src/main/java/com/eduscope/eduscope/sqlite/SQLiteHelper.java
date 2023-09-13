package com.eduscope.eduscope.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eduscope.eduscope.tests.QuestionList;
import com.eduscope.eduscope.tests.QuestionListGuessByImage;
import com.eduscope.eduscope.tests.QuestionListTrueOrFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Eduscope.db";
    private static final int DATABASE_VERSION = 27;

    private static final String RANDOM_FACT_TABLE_NAME = "RandomFact";
    private static final String RANDOM_FACT_COLUMN_ID = "id";
    private static final String RANDOM_FACT_COLUMN_TEXT = "text";
    private static final String RANDOM_FACT_COLUMN_IMAGE = "image";
    private static final String RANDOM_FACT_COLUMN_SUBJECT = "subject";

    private static final String TOPIC_TRAINING_TABLE_NAME = "TopicTrainingQuestions";
    private static final String TOPIC_TRAINING_COLUMN_ID = "id";
    private static final String TOPIC_TRAINING_COLUMN_QUESTION = "question";
    private static final String TOPIC_TRAINING_COLUMN_OPTION1 = "option1";
    private static final String TOPIC_TRAINING_COLUMN_OPTION2 = "option2";
    private static final String TOPIC_TRAINING_COLUMN_OPTION3 = "option3";
    private static final String TOPIC_TRAINING_COLUMN_OPTION4 = "option4";
    private static final String TOPIC_TRAINING_COLUMN_ANSWER = "answer";
    private static final String TOPIC_TRAINING_COLUMN_QUESTION_SET = "questionSet";
    private static final String TOPIC_TRAINING_COLUMN_SUBJECT = "subject";

    private static final String ALL_TOPICS_TRAINING_TABLE_NAME = "AllTopicsTrainingQuestions";

    private static final String TRUE_OR_FALSE_TABLE_NAME = "TrueOrFalseQuestions";
    private static final String TRUE_OR_FALSE_COLUMN_REVEAL_ANSWER = "revealAnswer";

    private static final String GUESS_BY_IMAGE_TABLE_NAME = "GuessByImageQuestions";

    private static final String SUMMARY_TABLE_NAME = "Summary";
    private static final String SUMMARY_COLUMN_TITLE = "title";
    private static final String SUMMARY_COLUMN_CONTENT = "content";

    private static final String SCIENTISTS_TABLE_NAME = "Scientists";

    private static final String SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME = "SummarySpanSearchWords";
    private static final String SUMMARY_SPAN_SEARCH_WORDS_COLUMN_WORD = "searchQuery";

    private static final String REMINDERS_TABLE_NAME = "Reminders";
    private static final String REMINDERS_COLUMN_ALARM_TIME = "alarmTime";


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_RANDOM_FACT = "CREATE TABLE " + RANDOM_FACT_TABLE_NAME + "("
                + RANDOM_FACT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RANDOM_FACT_COLUMN_TEXT + " TEXT,"
                + RANDOM_FACT_COLUMN_IMAGE + " BLOB UNIQUE,"
                + RANDOM_FACT_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_TOPIC_TRAINING = "CREATE TABLE " + TOPIC_TRAINING_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TOPIC_TRAINING_COLUMN_QUESTION + " TEXT UNIQUE,"
                + TOPIC_TRAINING_COLUMN_OPTION1 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION2 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION3 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION4 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_ANSWER + " TEXT,"
                + TOPIC_TRAINING_COLUMN_QUESTION_SET + " INTEGER,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_ALL_TOPICS_TRAINING = "CREATE TABLE " + ALL_TOPICS_TRAINING_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TOPIC_TRAINING_COLUMN_QUESTION + " TEXT UNIQUE,"
                + TOPIC_TRAINING_COLUMN_OPTION1 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION2 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION3 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION4 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_ANSWER + " TEXT,"
                + TOPIC_TRAINING_COLUMN_QUESTION_SET + " INTEGER,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_TRUE_OR_FALSE = "CREATE TABLE " + TRUE_OR_FALSE_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TOPIC_TRAINING_COLUMN_QUESTION + " TEXT UNIQUE,"
                + TOPIC_TRAINING_COLUMN_ANSWER + " TEXT,"
                + TRUE_OR_FALSE_COLUMN_REVEAL_ANSWER + " TEXT,"
                + TOPIC_TRAINING_COLUMN_QUESTION_SET + " INTEGER,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_GUESS_BY_IMAGE = "CREATE TABLE " + GUESS_BY_IMAGE_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TOPIC_TRAINING_COLUMN_QUESTION + " BLOB UNIQUE,"
                + TOPIC_TRAINING_COLUMN_OPTION1 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION2 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION3 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_OPTION4 + " TEXT,"
                + TOPIC_TRAINING_COLUMN_ANSWER + " TEXT,"
                + TOPIC_TRAINING_COLUMN_QUESTION_SET + " INTEGER,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_SUMMARY = "CREATE TABLE " + SUMMARY_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUMMARY_COLUMN_TITLE + " TEXT UNIQUE,"
                + SUMMARY_COLUMN_CONTENT + " TEXT,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_SCIENTISTS = "CREATE TABLE " + SCIENTISTS_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RANDOM_FACT_COLUMN_IMAGE + " BLOB,"
                + SUMMARY_COLUMN_TITLE + " TEXT UNIQUE,"
                + SUMMARY_COLUMN_CONTENT + " TEXT,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_SUMMARY_SPAN_SEARCH_WORDS = "CREATE TABLE " + SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUMMARY_SPAN_SEARCH_WORDS_COLUMN_WORD + " TEXT UNIQUE,"
                + TOPIC_TRAINING_COLUMN_SUBJECT + " TEXT"
                + ")";
        String CREATE_TABLE_REMINDERS = "CREATE TABLE " + REMINDERS_TABLE_NAME + "("
                + TOPIC_TRAINING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUMMARY_COLUMN_TITLE + " TEXT,"
                + SUMMARY_COLUMN_CONTENT + " TEXT,"
                + REMINDERS_COLUMN_ALARM_TIME + " LONG UNIQUE"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_RANDOM_FACT);
        sqLiteDatabase.execSQL(CREATE_TABLE_TOPIC_TRAINING);
        sqLiteDatabase.execSQL(CREATE_TABLE_ALL_TOPICS_TRAINING);
        sqLiteDatabase.execSQL(CREATE_TABLE_TRUE_OR_FALSE);
        sqLiteDatabase.execSQL(CREATE_TABLE_GUESS_BY_IMAGE);
        sqLiteDatabase.execSQL(CREATE_TABLE_SUMMARY);
        sqLiteDatabase.execSQL(CREATE_TABLE_SCIENTISTS);
        sqLiteDatabase.execSQL(CREATE_TABLE_SUMMARY_SPAN_SEARCH_WORDS);
        sqLiteDatabase.execSQL(CREATE_TABLE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RANDOM_FACT_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TOPIC_TRAINING_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ALL_TOPICS_TRAINING_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TRUE_OR_FALSE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GUESS_BY_IMAGE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUMMARY_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SCIENTISTS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + REMINDERS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public void deleteRowsBySubject(String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] tableNames = {RANDOM_FACT_TABLE_NAME, TOPIC_TRAINING_TABLE_NAME, ALL_TOPICS_TRAINING_TABLE_NAME,
                TRUE_OR_FALSE_TABLE_NAME, GUESS_BY_IMAGE_TABLE_NAME, SUMMARY_TABLE_NAME,
                SCIENTISTS_TABLE_NAME, SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME, REMINDERS_TABLE_NAME};
        for (String tableName : tableNames) {
            db.delete(tableName, RANDOM_FACT_COLUMN_SUBJECT + "=?", new String[]{subject});
        }
        db.close();
    }


    public void insertDataRandomFact(String text, byte[] image, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RANDOM_FACT_COLUMN_TEXT, text);
        values.put(RANDOM_FACT_COLUMN_IMAGE, image);
        values.put(RANDOM_FACT_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(RANDOM_FACT_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        db.delete(RANDOM_FACT_TABLE_NAME, null, null);
        db.close();
    }


    public void insertDataTopicTraining(String question, String option1, String option2, String option3, String option4, String answer, int questionSet, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TOPIC_TRAINING_COLUMN_QUESTION, question);
        values.put(TOPIC_TRAINING_COLUMN_OPTION1, option1);
        values.put(TOPIC_TRAINING_COLUMN_OPTION2, option2);
        values.put(TOPIC_TRAINING_COLUMN_OPTION3, option3);
        values.put(TOPIC_TRAINING_COLUMN_OPTION4, option4);
        values.put(TOPIC_TRAINING_COLUMN_ANSWER, answer);
        values.put(TOPIC_TRAINING_COLUMN_QUESTION_SET, questionSet);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(TOPIC_TRAINING_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        db.delete(TOPIC_TRAINING_TABLE_NAME, null, null);
        db.close();
    }


    public void insertDataAllTopicsTraining(String question, String option1, String option2, String option3, String option4, String answer, int questionSet, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TOPIC_TRAINING_COLUMN_QUESTION, question);
        values.put(TOPIC_TRAINING_COLUMN_OPTION1, option1);
        values.put(TOPIC_TRAINING_COLUMN_OPTION2, option2);
        values.put(TOPIC_TRAINING_COLUMN_OPTION3, option3);
        values.put(TOPIC_TRAINING_COLUMN_OPTION4, option4);
        values.put(TOPIC_TRAINING_COLUMN_ANSWER, answer);
        values.put(TOPIC_TRAINING_COLUMN_QUESTION_SET, questionSet);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(ALL_TOPICS_TRAINING_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        db.delete(ALL_TOPICS_TRAINING_TABLE_NAME, null, null);
        db.close();
    }


    public void insertDataTrueOrFalse(String question, String answer, String revealAnswer, int questionSet, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TOPIC_TRAINING_COLUMN_QUESTION, question);
        values.put(TOPIC_TRAINING_COLUMN_ANSWER, answer);
        values.put(TRUE_OR_FALSE_COLUMN_REVEAL_ANSWER, revealAnswer);
        values.put(TOPIC_TRAINING_COLUMN_QUESTION_SET, questionSet);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(TRUE_OR_FALSE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }


    public void insertDataGuessByImage(byte[] question, String option1, String option2, String option3, String option4, String answer, int questionSet, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TOPIC_TRAINING_COLUMN_QUESTION, question);
        values.put(TOPIC_TRAINING_COLUMN_OPTION1, option1);
        values.put(TOPIC_TRAINING_COLUMN_OPTION2, option2);
        values.put(TOPIC_TRAINING_COLUMN_OPTION3, option3);
        values.put(TOPIC_TRAINING_COLUMN_OPTION4, option4);
        values.put(TOPIC_TRAINING_COLUMN_ANSWER, answer);
        values.put(TOPIC_TRAINING_COLUMN_QUESTION_SET, questionSet);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(GUESS_BY_IMAGE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        db.delete(TOPIC_TRAINING_TABLE_NAME, null, null);
        db.close();
    }


    public void insertDataSummary(String title, String content, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUMMARY_COLUMN_TITLE, title);
        values.put(SUMMARY_COLUMN_CONTENT, content);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(SUMMARY_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }


    public void insertDataScientists(byte[] image, String title, String content, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RANDOM_FACT_COLUMN_IMAGE, image);
        values.put(SUMMARY_COLUMN_TITLE, title);
        values.put(SUMMARY_COLUMN_CONTENT, content);
        values.put(TOPIC_TRAINING_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(SCIENTISTS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }


    public void insertDataSummarySpanSearchWords(String searchQuery, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUMMARY_SPAN_SEARCH_WORDS_COLUMN_WORD, searchQuery);
        values.put(RANDOM_FACT_COLUMN_SUBJECT, subject);
        db.insertWithOnConflict(SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }


    public void insertReminders(String title, String content, long alarmTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUMMARY_COLUMN_TITLE, title);
        values.put(SUMMARY_COLUMN_CONTENT, content);
        values.put(REMINDERS_COLUMN_ALARM_TIME, alarmTime);
        db.insertWithOnConflict(REMINDERS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    //RandomFact
    public Map<byte[], String> selectRandomFact(String subject) {
        Map<byte[], String> result = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"image", "text"};
        String selection = "subject = ?";
        String[] selectionArgs = {subject};
        Cursor cursor = db.query("RandomFact", projection, selection, selectionArgs, null, null, null);
        int imageColumnIndex = cursor.getColumnIndexOrThrow("image");
        int textColumnIndex = cursor.getColumnIndexOrThrow("text");
        while (cursor.moveToNext()) {
            byte[] image = cursor.getBlob(imageColumnIndex);
            String text = cursor.getString(textColumnIndex);
            result.put(image, text);
        }
        cursor.close();
        db.close();
        return result;
    }


    //Scientists
    public String[] selectScientistsTitles(String subject) {
        List<String> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM Scientists ORDER BY title ASC";
        Cursor cursor = db.rawQuery(query, null);
        int columnIndex = cursor.getColumnIndexOrThrow("title");
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(columnIndex);
                resultList.add(title);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return resultList.toArray(new String[resultList.size()]);
    }

    public Map<byte[], String> selectScientistsContent(String subject, int position) {
        Map<byte[], String> resultMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM Scientists ORDER BY title ASC";
        Cursor cursor = db.rawQuery(query, null);
        int imageIndex = cursor.getColumnIndexOrThrow("image");
        int contentIndex = cursor.getColumnIndexOrThrow("content");
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                if (i == position) {
                    String content = cursor.getString(contentIndex);
                    byte[] image = cursor.getBlob(imageIndex);
                    resultMap.put(image, content);
                }
                i++;
            } while (cursor.moveToNext());
        }

        return resultMap;
    }


    //Summary
    public String[] selectSummaryTitles(String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"title"};
        String selection = "subject = ?";
        String[] selectionArgs = {subject};
        String sortOrder = "CAST(substr(title, 1, INSTR(title, '.') - 1) AS INTEGER)";
        Cursor cursor = db.query("Summary", projection, selection, selectionArgs, null, null, sortOrder);
        List<String> titlesList = new ArrayList<String>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            titlesList.add(title);
        }
        cursor.close();
        db.close();
        return titlesList.toArray(new String[titlesList.size()]);
    }


    public String[] selectSummaryContent(String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"content"};
        String selection = "subject = ?";
        String[] selectionArgs = {subject};
        String sortOrder = "CAST(substr(title, 1, INSTR(content, '.') - 1) AS INTEGER)";
        Cursor cursor = db.query("Summary", projection, selection, selectionArgs, null, null, sortOrder);
        List<String> contentList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            contentList.add(title);
        }
        cursor.close();
        db.close();
        return contentList.toArray(new String[contentList.size()]);
    }


    //SummarySpanSearchWords
    public String[] selectSummarySpanSearchWords(String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"searchQuery"};
        String selection = "subject = ?";
        String[] selectionArgs = {subject};
        Cursor cursor = db.query("SummarySpanSearchWords", projection, selection, selectionArgs, null, null, null);
        List<String> searchQueryList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String searchQuery = cursor.getString(cursor.getColumnIndexOrThrow("searchQuery"));
            searchQueryList.add(searchQuery);
        }
        cursor.close();
        db.close();
        return searchQueryList.toArray(new String[searchQueryList.size()]);

    }


    public int countAllTopicsTrainingQuestionSets(String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        int maxQuestionSet = 0;
        String query = "SELECT MAX(CAST(questionSet AS INTEGER)) FROM AllTopicsTrainingQuestions WHERE subject = ?";
        Cursor cursor = db.rawQuery(query, new String[] { subject });
        if (cursor.moveToFirst()) {
            maxQuestionSet = cursor.getInt(0);
            // do something with the max question set value
        }
        cursor.close();
        db.close();
        return maxQuestionSet;
    }


    //TopicTraining
    public List<QuestionList> selectTopicTrainingQuestions(String subject, String spinnerChoice) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Определяем, какой questionSet нам нужен
        int questionSetNum = Integer.parseInt(spinnerChoice.substring(0, spinnerChoice.indexOf(".")));
        List<QuestionList> questionListList = new ArrayList<>();
        String query = "SELECT question, option1, option2, option3, option4, answer FROM TopicTrainingQuestions WHERE subject = ? AND questionSet = ?";
        Cursor cursor = db.rawQuery(query, new String[] {subject, String.valueOf(questionSetNum)});
        while (cursor.moveToNext()) {
            String question = cursor.getString(cursor.getColumnIndexOrThrow("question"));
            String option1 = cursor.getString(cursor.getColumnIndexOrThrow("option1"));
            String option2 = cursor.getString(cursor.getColumnIndexOrThrow("option2"));
            String option3 = cursor.getString(cursor.getColumnIndexOrThrow("option3"));
            String option4 = cursor.getString(cursor.getColumnIndexOrThrow("option4"));
            String answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
            questionListList.add(new QuestionList(question, option1, option2, option3, option4, answer, ""));
        }
        cursor.close();
        db.close();
        return questionListList;
    }


    //AllTopicsTraining
    public List<QuestionList> selectAllTopicsTrainingQuestions(String subject, String spinnerChoiceAllTopics) {
        SQLiteDatabase db = this.getReadableDatabase();
        int questionSetNum = Integer.parseInt(spinnerChoiceAllTopics.substring((spinnerChoiceAllTopics.indexOf(" ") + 1)));
        List<QuestionList> questionListList = new ArrayList<>();
        String query = "SELECT question, option1, option2, option3, option4, answer FROM AllTopicsTrainingQuestions WHERE subject = ? AND questionSet = ?";
        Cursor cursor = db.rawQuery(query, new String[] {subject, String.valueOf(questionSetNum)});
        while (cursor.moveToNext()) {
            String question = cursor.getString(cursor.getColumnIndexOrThrow("question"));
            String option1 = cursor.getString(cursor.getColumnIndexOrThrow("option1"));
            String option2 = cursor.getString(cursor.getColumnIndexOrThrow("option2"));
            String option3 = cursor.getString(cursor.getColumnIndexOrThrow("option3"));
            String option4 = cursor.getString(cursor.getColumnIndexOrThrow("option4"));
            String answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
            questionListList.add(new QuestionList(question, option1, option2, option3, option4, answer, ""));
        }
        cursor.close();
        db.close();
        return questionListList;
    }


    //TrueOrFalse
    public List<QuestionListTrueOrFalse> selectTrueOrFalseQuestions(String subject, int setsCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<QuestionListTrueOrFalse> questionListTrueOrFalseList = new ArrayList<>();
        String query = "SELECT question, answer, revealAnswer FROM TrueOrFalseQuestions WHERE subject = ? AND questionSet = ?";
        Cursor cursor = db.rawQuery(query, new String[] {subject, String.valueOf(setsCompleted + 1)});
        while (cursor.moveToNext()) {
            String question = cursor.getString(cursor.getColumnIndexOrThrow("question"));
            String answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
            String revealAnswer = cursor.getString(cursor.getColumnIndexOrThrow("revealAnswer"));
            questionListTrueOrFalseList.add(new QuestionListTrueOrFalse(question, answer, "", revealAnswer));
        }
        cursor.close();
        db.close();
        return questionListTrueOrFalseList;
    }


    //GuessByImage
    public List<QuestionListGuessByImage> selectGuessByImage(String subject, int setsCompleted) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<QuestionListGuessByImage> questionListGuessByImageList = new ArrayList<>();
        String query = "SELECT question, option1, option2, option3, option4, answer FROM GuessByImageQuestions WHERE subject = ? AND questionSet = ?";
        Cursor cursor = db.rawQuery(query, new String[] {subject, String.valueOf(setsCompleted + 1)});
        while (cursor.moveToNext()) {
            byte[] questionByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow("question"));
            Bitmap question = BitmapFactory.decodeByteArray(questionByteArray, 0, questionByteArray.length);
            String option1 = cursor.getString(cursor.getColumnIndexOrThrow("option1"));
            String option2 = cursor.getString(cursor.getColumnIndexOrThrow("option2"));
            String option3 = cursor.getString(cursor.getColumnIndexOrThrow("option3"));
            String option4 = cursor.getString(cursor.getColumnIndexOrThrow("option4"));
            String answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
            questionListGuessByImageList.add(new QuestionListGuessByImage(question, option1, option2, option3, option4, answer, ""));
        }
        cursor.close();
        db.close();
        return questionListGuessByImageList;
    }


    public List<String> selectRemindersTitles() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> result = new ArrayList<>();
        String[] projection = {"title"};
        String sortOrder = "alarmTime ASC";
        Cursor cursor = db.query(REMINDERS_TABLE_NAME, projection, null, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            result.add(title);
        }
        cursor.close();
        db.close();
        return result;
    }

    public List<String> selectRemindersContents() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> result = new ArrayList<>();
        String[] projection = {"content"};
        String sortOrder = "alarmTime ASC";
        Cursor cursor = db.query(REMINDERS_TABLE_NAME, projection, null, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            result.add(content);
        }
        cursor.close();
        db.close();
        return result;
    }

    public List<Long> selectRemindersAlarmTimes() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Long> result = new ArrayList<>();
        String[] projection = {"alarmTime"};
        String sortOrder = "alarmTime ASC";
        Cursor cursor = db.query(REMINDERS_TABLE_NAME, projection, null, null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            long alarmTime = cursor.getLong(cursor.getColumnIndexOrThrow("alarmTime"));
            result.add(alarmTime);
        }
        cursor.close();
        db.close();
        return result;
    }



    public void tableCleanup() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SUMMARY_SPAN_SEARCH_WORDS_TABLE_NAME, null, null);
        db.delete(SCIENTISTS_TABLE_NAME, null, null);
        db.delete(SUMMARY_TABLE_NAME, null, null);
        db.delete(GUESS_BY_IMAGE_TABLE_NAME, null, null);
        db.delete(RANDOM_FACT_TABLE_NAME, null, null);
        db.delete(TRUE_OR_FALSE_TABLE_NAME, null, null);
        db.delete(ALL_TOPICS_TRAINING_TABLE_NAME, null, null);
        db.delete(TOPIC_TRAINING_TABLE_NAME, null, null);
        db.delete(REMINDERS_TABLE_NAME, null, null);
        db.close();
    }


    /*public void insertDataAllTopicsTraining(int entryArraysLength, Map.Entry<String, Object>[] entryArrayQuestion, Map.Entry<String, Object>[] entryArrayOption1,
                                            Map.Entry<String, Object>[] entryArrayOption2, Map.Entry<String, Object>[] entryArrayOption3,
                                            Map.Entry<String, Object>[] entryArrayOption4, Map.Entry<String, Object>[] entryArrayAnswer,
                                            Map.Entry<String, Integer>[] entryArrayQuestionSet, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < entryArraysLength; i++) {
            ContentValues values = new ContentValues();
            values.put(ALL_TOPICS_TRAINING_COLUMN_QUESTION, (String) entryArrayQuestion[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_OPTION1, (String) entryArrayOption1[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_OPTION2, (String) entryArrayOption2[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_OPTION3, (String) entryArrayOption3[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_OPTION4, (String) entryArrayOption4[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_ANSWER, (String) entryArrayAnswer[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_QUESTION_SET, entryArrayQuestionSet[i].getValue());
            values.put(ALL_TOPICS_TRAINING_COLUMN_SUBJECT, subject);
            db.insertWithOnConflict(ALL_TOPICS_TRAINING_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.endTransaction();
        db.close();

    }*/
}
