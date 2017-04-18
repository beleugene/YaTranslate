package com.beleugene.yatranslate.yatranslate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TranslationItem> historyItems;
    private ArrayList<TranslationItem> favoriteItems;

    public HistoryAdapter(Context context) {
        this.context = context;
        historyItems = History.getInstance(context).getList();
        favoriteItems = Favorite.getInstance(context).getList();
    }

    @Override
    public int getCount() {
        return historyItems.size();
    }

    @Override
    public Object getItem(int i) {
        return historyItems.get(i);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.history_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.sourceView = (TextView) convertView.findViewById(R.id.source_text);
                viewHolder.targetView = (TextView) convertView.findViewById(R.id.targe_text);
                viewHolder.directionView = (TextView) convertView.findViewById(R.id.translate_direction);
                viewHolder.favoriteButtonImageView = (ImageView) convertView.findViewById(R.id.to_favorite_button);
                convertView.setTag(viewHolder);
            }
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder != null) {
            if (getItem(position) instanceof TranslationItem) {
                final TranslationItem item = (TranslationItem) getItem(position);
                viewHolder.sourceView.setText(item.getRequestSentence());
                viewHolder.targetView.setText(item.getTranslatedSentence());
                viewHolder.directionView.setText(item.getSourceLanguage() + "-" + item.getTargetLanguage());
                final ImageView favoriteButtonImageView = viewHolder.favoriteButtonImageView;
                if (favoriteItems.contains(item)){
                    favoriteButtonImageView.setImageResource(R.drawable.favorite_btn);
                } else {
                    favoriteButtonImageView.setImageResource(R.drawable.favorite_gray_btn);
                }
                favoriteButtonImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (favoriteItems.contains(item)) {
                            Favorite.getInstance(context).remove(item);
                            favoriteButtonImageView.setImageResource(R.drawable.favorite_gray_btn);
                        } else {
                            favoriteButtonImageView.setImageResource(R.drawable.favorite_btn);
                            Favorite.getInstance(context).add(item);
                        }
                    }
                });

            }
        }

        return convertView;
    }

    private class ViewHolder {
        TextView sourceView;
        TextView targetView;
        TextView directionView;
        ImageView favoriteButtonImageView;
    }
}
