package com.beleugene.yatranslate.yatranslate;

import java.io.Serializable;

/** The class is container for translation item
 *  sourceLanguage - source language
 *  targetLanguage - target language
 *  requestSentence - original text
 *  translatedSentence - translated text
 */

public class TranslationItem implements Serializable{
    private String sourceLanguage;
    private String targetLanguage;
    private String requestSentence;
    private String translatedSentence;

    public TranslationItem(String sourceLanguage, String targetLanguage, String requestSentence, String translatedSentence) {
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.requestSentence = requestSentence;
        this.translatedSentence = translatedSentence;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getRequestSentence() {
        return requestSentence;
    }

    public String getTranslatedSentence() {
        return translatedSentence;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)){
            return true;
        }
        if (obj == null || (this.getClass() != obj.getClass())) {
            return false;
        }
        TranslationItem item = (TranslationItem) obj;
        if (sourceLanguage != null && sourceLanguage.equals(item.sourceLanguage)
                && targetLanguage != null && targetLanguage.equals(item.targetLanguage)
                && requestSentence != null && requestSentence.equals(item.requestSentence)
                && translatedSentence != null && translatedSentence.equals(item.translatedSentence)
                ) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        code = (sourceLanguage != null ? sourceLanguage.hashCode() : 0);
        code = 31 * code + (targetLanguage != null ? targetLanguage.hashCode() : 0);
        code = 31 * code + (requestSentence != null ? requestSentence.hashCode() : 0);
        code = 31 * code + (translatedSentence != null ? translatedSentence.hashCode() : 0);
        return code;
    }
}
