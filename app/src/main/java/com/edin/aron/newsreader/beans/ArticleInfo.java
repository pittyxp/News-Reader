package com.edin.aron.newsreader.beans;

import java.util.Date;

/**
 * Created by Aron on 20/01/17.
 */
/*
qui non faccio altro che creare la classe ArticleInfo e all'interno creo il metodo ArticleInfo(). Questo metodo non
non farà altro che andare a prendere il titolo e l'url che saranno utilizzati dal NewspPovider all'interno del proprio
costruttore creando gli OGGETTI in maniera elementare perchè gli abbiamo passato nome e url manualmente!!! Tale classe alla
fine è stata cancellata xche non più necessaria!
 */
public class ArticleInfo {

    private int id=0;
    private String title=null;
    private String url=null;
    private Date date=null;
    private boolean bookmark=false;
    private String description=null;

    public ArticleInfo(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getShortUrl()
    {
        return getUrl().substring(0,20)+"...";
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public boolean isBookmark() {
        return bookmark;
    }
    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }
    // chiedere Mario
    public String getDescription() {
        if (description==null) {
            description = description + "Nessuna Descrizione!!!";
            return description;
        } else {
            return description;
        }
    }
}
