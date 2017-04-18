package com.beleugene.yatranslate.yatranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslateFragment extends Fragment implements View.OnClickListener {

    private static final String YA_API_KEY_KEY = "key";
    private static final String YA_TEXT_KEY = "text";
    private static final String YA_LANG_KEY = "lang";
    private static final String YA_FORMAT_KEY = "format";
    private static final String YA_PLAIN_VALUE = "plain";
    private String api_key;
    private String yaTranslateUrl;

    // the fragment initialization parameters
    private static final String SOURCE_TEXT = "source_text";
    private static final String TARGET_TEXT = "target_text";
    private static final String SOURCE_LANG = "source_lang";
    private static final String TARGET_LANG = "target_lang";

    private String sourceText;
    private String targetText;
    private String sourceLang;
    private String targetLang;

    private Button sourceLanguageButton;
    private Button targetLanguageButton;
    private ImageView favoriteButton;

    public TranslateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sourceText Text for translation.
     * @param targetText Translated tex.
     * @param sourceLang Code of source language
     * @param targetLang Code of target language.
     * @return A new instance of fragment TranslateFragment.
     */
    public static TranslateFragment newInstance(String sourceText, String targetText, String sourceLang, String targetLang) {
        TranslateFragment fragment = new TranslateFragment();
        Bundle args = new Bundle();
        args.putString(SOURCE_TEXT, sourceText);
        args.putString(TARGET_TEXT, targetText);
        args.putString(SOURCE_LANG, sourceLang);
        args.putString(TARGET_LANG, targetLang);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        yaTranslateUrl = getResources().getString(R.string.yandex_translate_url);
        api_key = getResources().getString(R.string.api_key);

        if (getArguments() != null) {
            sourceText = getArguments().getString(SOURCE_TEXT);
            targetText = getArguments().getString(TARGET_TEXT);
            sourceLang = getArguments().getString(SOURCE_LANG);
            targetLang = getArguments().getString(TARGET_LANG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_translate, container, false);

        sourceLanguageButton = (Button) view.findViewById(R.id.from_language_button);
        targetLanguageButton = (Button) view.findViewById(R.id.to_language_button);
        favoriteButton = (ImageView) view.findViewById(R.id.to_favorite_button);


        sourceLanguageButton.setOnClickListener(this);
        targetLanguageButton.setOnClickListener(this);
        view.findViewById(R.id.swap_language_button).setOnClickListener(this);
        favoriteButton.setOnClickListener(this);
        view.findViewById(R.id.translate_button).setOnClickListener(this);
        view.findViewById(R.id.clear_input_button).setOnClickListener(this);

        updateLanguageUI(null);

        final EditText editText = (EditText) view.findViewById(R.id.input_edit_text);
        editText.setText(sourceText);
        final TextView textView = (TextView) view.findViewById(R.id.translated_text_view);
        textView.setText(targetText);

        return view;
    }

    public void updateLanguageUI(String changedDirectionLanguage) {
        final TranslateDirection translateDirection = ((MainActivity) getActivity()).getTranslateDirection();
        sourceLang = translateDirection.getSourceLanguageCode();
        targetLang = translateDirection.getTargetLanguageCode();
        final String sourceLanguageName = TranslateDirection.getLanguageName(getContext(), sourceLang);
        final String targetLanguageName = TranslateDirection.getLanguageName(getContext(), targetLang);
        sourceLanguageButton.setText(sourceLanguageName);
        targetLanguageButton.setText(targetLanguageName);

        // if source language is changed than check direction of translating
        if (changedDirectionLanguage != null && changedDirectionLanguage.equals(TranslateDirection.SOURCE_SIDE_KEY)) {
            if (!TranslateDirection.directionIsValid(sourceLang, targetLang)) {
                Toast.makeText(getContext(), R.string.translate_direction_is_not_valid, Toast.LENGTH_LONG).show();
            }
        }

    }

    private String getSourceLanguageCode() {
        return sourceLang;
    }

    private String getTargetLanguageCode() {
        return targetLang;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//            outState.putString(SOURCE_LANG, sourceLang);
//            outState.putString(TARGET_LANG, targetLang);

    }

    @Override
    public void onDestroy() {
        cancelDownload();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.from_language_button) {
            // open screen of source language selection
            final String langCode = ((MainActivity) getActivity()).getTranslateDirection().getSourceLanguageCode();
            final LanguageSelectionDialog dialog = LanguageSelectionDialog.newInstance(TranslateDirection.SOURCE_SIDE_KEY, langCode);
            dialog.show(getFragmentManager(), "LanguageSelectionTag");

        } else if (view.getId() == R.id.to_language_button) {
            // open screen of target language selection
            final String langCode = ((MainActivity) getActivity()).getTranslateDirection().getTargetLanguageCode();
            final LanguageSelectionDialog dialog = LanguageSelectionDialog.newInstance(TranslateDirection.TARGET_SIDE_KEY, langCode);
            dialog.show(getFragmentManager(), "LanguageSelectionTag");

        } else if (view.getId() == R.id.swap_language_button) {
            String swapLang = sourceLang;
            sourceLang = targetLang;
            targetLang = swapLang;
            ((MainActivity) getActivity()).getTranslateDirection().setSourceLanguageCode(sourceLang);
            ((MainActivity) getActivity()).getTranslateDirection().setTargetLanguageCode(targetLang);
            updateLanguageUI(TranslateDirection.SOURCE_SIDE_KEY);
        } else if (view.getId() == R.id.to_favorite_button) {
            // Add the translation to favorite
            if (targetText != null && !targetText.isEmpty()) {
                TranslationItem item = new TranslationItem(sourceLang, targetLang, sourceText, targetText);
                if (Favorite.getInstance(getContext()).hasTranslate(item)) {
                    Favorite.getInstance(getContext()).remove(item);
                } else {
                    Favorite.getInstance(getContext()).add(item);
                }
                setFavoriteButtonImage(item);
            }
        } else if (view.getId() == R.id.translate_button) {

            final View view1 = getView();
            if (view1 != null) {
                translate(
                        (EditText) view1.findViewById(R.id.input_edit_text),
                        (TextView) view1.findViewById(R.id.translated_text_view)
                );
            }
        } else if (view.getId() == R.id.clear_input_button) {
            final View view1 = getView();
            if (view1 != null) {
                ((EditText) view1.findViewById(R.id.input_edit_text)).setText("");
            }
        }
    }

    public void setFavoriteButtonImage(TranslationItem item) {
        if (Favorite.getInstance(getContext()).hasTranslate(item)) {
            favoriteButton.setImageResource(R.drawable.favorite_btn);
        } else {
            favoriteButton.setImageResource(R.drawable.favorite_gray_btn);
        }
    }

    private void translate(EditText editText, final TextView textView) {
        final String sourceCode = getSourceLanguageCode();
        final String targetCode = getTargetLanguageCode();
        // if there are list of supported languages and the translate direction is not supported, than show error message and return
        if (!TranslateDirection.directionIsValid(sourceCode, targetCode)) {
            Toast.makeText(getContext(), R.string.translate_direction_is_not_valid, Toast.LENGTH_LONG).show();
            return;
        }

        sourceText = editText.getText().toString();
        final String text = sourceText;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                yaTranslateUrl,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray translated = jsonObject.getJSONArray("text");
                    // we use only one text parameter, so we are only interested in first [0] element
                    targetText = translated.get(0).toString();
                    textView.setText(targetText);

                    TranslationItem item = new TranslationItem(sourceCode, targetCode, sourceText, targetText);
                    History.getInstance(getContext()).add(item);
                    setFavoriteButtonImage(item);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), R.string.response_json_error, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) { // I was able to get only the 400, 403 code
                            Toast.makeText(getContext(), R.string.volley_translation_error, Toast.LENGTH_LONG).show();
                        } else {
                            // I was able to get only the 400, 403 code

                            if (statusCode == 401) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_app_key_invalid, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 402) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_app_key_blocked, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 404) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_exceeded_daily_limit, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 413) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_exceeded_max_text_size, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 422) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_can_not_be_translated, Toast.LENGTH_LONG).show();
                            } else if (statusCode == 501) {
                                Toast.makeText(getContext(), R.string.volley_translation_error_translate_direction_invalide, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), R.string.volley_translation_error, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put(YA_API_KEY_KEY, api_key);
                param.put(YA_TEXT_KEY, text);
                param.put(YA_LANG_KEY, sourceCode + "-" + targetCode);
                param.put(YA_FORMAT_KEY, YA_PLAIN_VALUE);
                return param;
            }

        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void cancelDownload() {
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }
}
