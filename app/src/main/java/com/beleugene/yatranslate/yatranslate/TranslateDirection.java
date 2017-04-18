package com.beleugene.yatranslate.yatranslate;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** The class describe translate direction (source language -> target language) and contains static helper methods
 */

public class TranslateDirection {
    public static final String DEFAULT_SOURCE_LANGUAGE = "en";
    public static final String DEFAULT_TARGET_LANGUAGE = "ru";
    public static final String SOURCE_SIDE_KEY = "source_key";
    public static final String TARGET_SIDE_KEY = "target_key";

    private String sourceLanguageCode;
    private String targetLanguageCode;

    // Map language code -> language name
    private static Map<String, String> codeToNameMap = new HashMap<>();

    // The list contains valids translate direction received by request from https://translate.yandex.net/api/v1.5/tr.json/getLangs
    private static Map<String, ArrayList<String>> validTranslateDirections = new HashMap<>();

    public TranslateDirection() {
        this(DEFAULT_SOURCE_LANGUAGE, DEFAULT_TARGET_LANGUAGE);
    }

    public TranslateDirection(String sourceLanguageCode, String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
        this.sourceLanguageCode = sourceLanguageCode;
    }

    public void save(Context context){
        SharedPreferences preferences = context.getSharedPreferences("TransPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putString(SOURCE_SIDE_KEY, sourceLanguageCode);
        edit.putString(TARGET_SIDE_KEY, targetLanguageCode);
        edit.apply();
    }

    public void load(Context context){
        SharedPreferences preferences = context.getSharedPreferences("TransPref", Context.MODE_PRIVATE);
        sourceLanguageCode = preferences.getString(SOURCE_SIDE_KEY, DEFAULT_SOURCE_LANGUAGE);
        targetLanguageCode = preferences.getString(TARGET_SIDE_KEY, DEFAULT_TARGET_LANGUAGE);
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    public String getSourceLanguageCode() {
        return sourceLanguageCode;
    }

    public void setSourceLanguageCode(String sourceLanguageCode) {
        this.sourceLanguageCode = sourceLanguageCode;
    }

    public void addTargetsForSource(String sourceLanguageCode, ArrayList<String> targetList, Map<String, String> nameMap) {
        validTranslateDirections.put(sourceLanguageCode, targetList);
        codeToNameMap.putAll(nameMap);
    }

    public static boolean hasTargetList(String sourceLang) {
        return validTranslateDirections.containsKey(sourceLang);
    }

    public static String getLanguageName(Context context, String langCode){
        String[] codes = context.getResources().getStringArray(R.array.language_codes);
        String[] names = context.getResources().getStringArray(R.array.language_names);
        int index = Arrays.asList(codes).indexOf(langCode);
        return (index != -1 && index < names.length ? names[index] : langCode);
    }

    public static ArrayList<String> getTargetList(String sourceCode) {
        return validTranslateDirections.get(sourceCode);
    }

    public static String getLangName(String code) {
        if (codeToNameMap.containsKey(code)) {
            return codeToNameMap.get(code);
        } else {
            return code;
        }
    }

    public static boolean directionIsValid(String sourceCode, String targetCode) {
        if (validTranslateDirections.containsKey(sourceCode)) {
            return validTranslateDirections.get(sourceCode).contains(targetCode);
        }
        // default any direction is valid, if it is not - get error code from server at translating
        return true;
    }

}
