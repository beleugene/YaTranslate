package com.beleugene.yatranslate.yatranslate;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        LanguageSelectionDialog.LanguageSelectionListener,
        HistoryFragment.OnHistoryFragmentInteractionListener,
        FavoriteFragment.OnFavoriteFragmentInteractionListener,
        View.OnClickListener {


    private String api_key;
    private String yaGetLangUrl;
    private TranslateDirection translateDirection = new TranslateDirection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        translateDirection.load(this);

        yaGetLangUrl = getResources().getString(R.string.yandex_get_lang_url);
        api_key = getResources().getString(R.string.api_key);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        if (fragment == null) {
            fragment = TranslateFragment.newInstance("","", TranslateDirection.DEFAULT_SOURCE_LANGUAGE, TranslateDirection.DEFAULT_TARGET_LANGUAGE);
            getSupportFragmentManager().beginTransaction().add(R.id.content_fragment, fragment).commit();
        }

        findViewById(R.id.yandex_translated).setOnClickListener(this);
        findViewById(R.id.navigation_translate_button).setOnClickListener(this);
        findViewById(R.id.navigation_history_button).setOnClickListener(this);
        findViewById(R.id.navigation_favorite_button).setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        translateDirection.save(this);
        History.getInstance(this).save(this);
        Favorite.getInstance(this).save(this);
    }

    @Override
    public void onLanguageItemSelected(String langCode, String directionKey) {
        if (directionKey.equals(TranslateDirection.SOURCE_SIDE_KEY)) {
            translateDirection.setSourceLanguageCode(langCode);
            if (!TranslateDirection.hasTargetList(langCode)){
                loadLanguageTargetList(langCode);
            }
        } else if (directionKey.equals(TranslateDirection.TARGET_SIDE_KEY)) {
            translateDirection.setTargetLanguageCode(langCode);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        if (fragment instanceof TranslateFragment){
            ((TranslateFragment)fragment).updateLanguageUI(directionKey);
        }
    }

    private void loadLanguageTargetList(final String langCode) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, yaGetLangUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject langs = json.getJSONObject("langs");

                            ArrayList<String> targetList = new ArrayList<>();
                            Map<String, String> nameMap = new HashMap<>();

                            for (Iterator<String> iterator = langs.keys(); iterator.hasNext();) {
                                String code = iterator.next();
                                String name = langs.getString(code);

                                targetList.add(code);
                                nameMap.put(code, name);
                            }
                            translateDirection.addTargetsForSource(langCode, targetList, nameMap);

                        } catch (JSONException e) {
                            // nothing, continue use old data
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) { // I was able to get only the 400 code
                            Toast.makeText(MainActivity.this, R.string.volley_translation_error, Toast.LENGTH_LONG).show();
                        } else {
                            // I was able to get only the 400 code
                            if (statusCode == 401) {
                                Toast.makeText(MainActivity.this, R.string.volley_translation_error_app_key_invalid, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 402) {
                                Toast.makeText(MainActivity.this, R.string.volley_translation_error_app_key_blocked, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, R.string.volley_translation_error, Toast.LENGTH_LONG).show();
                            }
                        }


                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("key", api_key);
                param.put("ui", langCode);
                return param;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public TranslateDirection getTranslateDirection() {
        return translateDirection;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.yandex_translated) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.translated_by_yandex_url)));
            startActivity(intent);
        } else if (id == R.id.navigation_translate_button){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
            if (!(fragment instanceof TranslateFragment)) {
                ArrayList<TranslationItem>  items = History.getInstance(this).getList();
                String sourceText;
                String targetText;
                if (items != null && items.size() > 0) {
                    sourceText = items.get(0).getRequestSentence();
                    targetText = items.get(0).getTranslatedSentence();
                } else {
                    sourceText = "";
                    targetText = "";
                }

                fragment = TranslateFragment.newInstance(sourceText,targetText, TranslateDirection.DEFAULT_SOURCE_LANGUAGE, TranslateDirection.DEFAULT_TARGET_LANGUAGE);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, fragment).commit();
            }
        } else if (id == R.id.navigation_history_button){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
            if (!(fragment instanceof HistoryFragment)) {
                fragment = HistoryFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, fragment).commit();
            }
        } else if (id == R.id.navigation_favorite_button){
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
                if (!(fragment instanceof FavoriteFragment)) {
                    fragment = FavoriteFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, fragment).commit();
                }
        }
    }

    @Override
    public void onTranslationOpen(TranslationItem translation) {
        final String sourceLanguage = translation.getSourceLanguage();
        final String targetLanguage = translation.getTargetLanguage();
        translateDirection.setSourceLanguageCode(sourceLanguage);
        translateDirection.setTargetLanguageCode(targetLanguage);
        TranslateFragment fragment = TranslateFragment.newInstance(translation.getRequestSentence(),translation.getTranslatedSentence(), sourceLanguage, targetLanguage);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment, fragment).commit();
    }
}
