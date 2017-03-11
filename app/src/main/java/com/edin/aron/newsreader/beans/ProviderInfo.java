package com.edin.aron.newsreader.beans;

/**
 * Created by Aron on 24/01/17.
 */
// creo l'oggetto ProviderInfo con dei setter e getter elementari per prelevare quello che l'utente inserisce

public class ProviderInfo {

    private String name=null;
    private String url=null;

    public ProviderInfo(String name, String url)
    {
        this.name=name;
        this.url=url;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name=name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url=url; }

    public String toString() {
        return name;
    }
}
