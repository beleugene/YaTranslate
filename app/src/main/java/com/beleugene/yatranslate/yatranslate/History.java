package com.beleugene.yatranslate.yatranslate;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class History implements Serializable{
    private static final int MAX_SIZE = 50;
    private static final String FILE_NAME = "history";
    private ArrayList<TranslationItem> list = new ArrayList<>();
    private static History instance;

    public static synchronized History getInstance(Context context){
        if (instance == null) {
            instance = load(context);
            if (instance == null) {
                instance = new History();
            }
        }
        return instance;
    }


    public TranslationItem remove(int position) {
        return list.remove(position);
    }

    public boolean remove(TranslationItem item) {
        return list.remove(item);
    }

    public void add(TranslationItem item) {
        if (list.contains(item)){
            list.remove(item);
        }
        list.add(0, item);
        while (list.size() > MAX_SIZE) {
            list.remove(list.size() - 1);
        }
    }

    public ArrayList<TranslationItem> getList() {
        return list;
    }

    // save history to file
    public void save(Context context){
        String file = FILE_NAME;
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(this);
            os.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // load history from file
    // exception is ignored - use new empty history
    private static History load(Context context){
        History history = null;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            history = (History) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return history;
    }
}
