package com.beleugene.yatranslate.yatranslate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageSelectionDialog extends DialogFragment {

    private static final String LANG_SIDE_KEY = "language_key";
    private static final String LANGUAGE_CODE = "language_code";
    private static final String MAP_CODE_KEY = "code";
    private static final String MAP_NAME_KEY = "names";

    public interface LanguageSelectionListener {
        void onLanguageItemSelected(String langCode, String directionKey);
    }

    private LanguageSelectionListener mListener;

    /**
     * @param translateDirectionKey TranslateDirection.SOURCE_SIDE_KEY, TranslateDirection.TARGET_SIDE_KEY - source or target selection in the dialog
     * @param langCode present value of language code
     * @return new Instance of LanguageSelectionDialog
    */
    public static LanguageSelectionDialog newInstance(String translateDirectionKey, String langCode) {
        LanguageSelectionDialog dialog = new LanguageSelectionDialog();
        Bundle args = new Bundle();
        args.putString(LANG_SIDE_KEY, translateDirectionKey);
        args.putString(LANGUAGE_CODE, langCode);
        dialog.setArguments(args);
        return dialog;
    }

    // is source language or target language
    private String getTranslateDirectionKey() {
        return getArguments().getString(LANG_SIDE_KEY);
    }

    // current language code
    private String getCheckedCode() {
        return getArguments().getString(LANGUAGE_CODE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        List<Map<String, String>> data = new ArrayList<>();

        final int checkedIndex;
        if (isSourceLanguage()) {
            checkedIndex = fillDataFromResources(data);
        } else {
            final String sourceCode = ((MainActivity) getActivity()).getTranslateDirection().getSourceLanguageCode();

            // if for current source language there are list of supported language, than use the list, otherwise use list of language from resource file
            if (TranslateDirection.hasTargetList(sourceCode)) {
                ArrayList<String> targets = TranslateDirection.getTargetList(sourceCode);
                checkedIndex = fillDataFromTargetList(data, targets);
            } else {
                checkedIndex = fillDataFromResources(data);
            }
        }

        final SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), data, android.R.layout.simple_list_item_single_choice, new String[]{MAP_NAME_KEY}, new int[]{android.R.id.text1});

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title

        builder.setTitle(R.string.select_language_title)
                .setSingleChoiceItems(
                        simpleAdapter,
                        checkedIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Map<String, String> map = (Map<String, String>) simpleAdapter.getItem(i);
                                String checkedCode = getCheckedCode();
                                final String selectedCode = map.get(MAP_CODE_KEY);
                                if (!checkedCode.equals(selectedCode)) {
                                    mListener.onLanguageItemSelected(selectedCode, getTranslateDirectionKey());
                                    LanguageSelectionDialog.this.dismiss();
                                }
                            }
                        });

        return builder.create();
    }

    private boolean isSourceLanguage() {
        return TranslateDirection.SOURCE_SIDE_KEY.equals(getTranslateDirectionKey());
    }

    private int fillDataFromTargetList(List<Map<String, String>> data, ArrayList<String> targets) {
        int checkedIndex = -1;
        String checkedLang = getCheckedCode();
        for (String code :
                targets) {
            final HashMap<String, String> map = new HashMap<>();
            map.put(MAP_CODE_KEY, code);
            map.put(MAP_NAME_KEY, TranslateDirection.getLangName(code));
            data.add(map);
            if (code.equals(checkedLang)) {
                checkedIndex = targets.indexOf(code);
            }
        }
        return checkedIndex;
    }

    private int fillDataFromResources(List<Map<String, String>> data) {
        String checkedCode = getCheckedCode();
        int checkedIndex = 0;
        String[] codes = getResources().getStringArray(R.array.language_codes);
        String[] names = getResources().getStringArray(R.array.language_names);

        for (int i = 0; i < codes.length; i++) {
            final HashMap<String, String> map = new HashMap<>();
            map.put(MAP_CODE_KEY, codes[i]);
            map.put(MAP_NAME_KEY, names[i]);
            data.add(map);
            if (checkedCode.equals(codes[i])) {
                checkedIndex = i;
            }
        }
        return checkedIndex;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (LanguageSelectionListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement LanguageSelectionListener");
        }
    }
}
