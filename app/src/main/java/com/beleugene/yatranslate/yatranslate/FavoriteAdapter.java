package com.beleugene.yatranslate.yatranslate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class FavoriteAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TranslationItem> translationItems;

    public FavoriteAdapter(Context context) {
        this.context = context;
        translationItems = Favorite.getInstance(context).getList();
    }

    @Override
    public int getCount() {
        return translationItems.size();
    }

    @Override
    public Object getItem(int i) {
        return translationItems.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            if (context != null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.favorite_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.sourceView = (TextView) convertView.findViewById(R.id.source_text);
                viewHolder.targetView = (TextView) convertView.findViewById(R.id.targe_text);
                viewHolder.directionView = (TextView) convertView.findViewById(R.id.translate_direction);
                convertView.setTag(viewHolder);
            }
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder != null) {
            if (getItem(position) instanceof TranslationItem) {
                TranslationItem item = (TranslationItem) getItem(position);
                viewHolder.sourceView.setText(item.getRequestSentence());
                viewHolder.targetView.setText(item.getTranslatedSentence());
                viewHolder.directionView.setText(item.getSourceLanguage() + "-" + item.getTargetLanguage());
            }
        }
        return convertView;
    }

    private class ViewHolder {
        TextView sourceView;
        TextView targetView;
        TextView directionView;
    }
}
