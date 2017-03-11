package com.edin.aron.newsreader.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.edin.aron.newsreader.beans.ArticleInfo;
import com.edin.aron.newsreader.R;

import java.util.List;

/**
 * Created by Aron on 30/01/17.
 */

public class  ArticlesAdapter extends BaseAdapter
{
    private List<ArticleInfo> news=null;

    public ArticlesAdapter(List<ArticleInfo> news)
    {
        this.news=news;
    }

    @Override
    public int getCount()
    {
        return news.size();
    }

    @Override
    public Object getItem(int position)
    {
        return news.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return news.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater= (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView==null)
        {
            convertView=inflater.inflate(R.layout.article_layout,null);
        }
        ArticleInfo article= (ArticleInfo) getItem(position);
        TextView txt= (TextView) convertView.findViewById(R.id.title);
        txt.setText(article.getTitle());
        return convertView;
    }
}