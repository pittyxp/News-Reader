package com.edin.aron.newsreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.edin.aron.newsreader.beans.ArticleInfo;

import java.text.SimpleDateFormat;

/**
 * Created by Aron on 26/01/17.
 */

public class DbManager {
    private DbHelper helper=null;

    public DbManager(Context context) {
        helper=new DbHelper(context);
    }

    public void newProvider(ContentValues values)
    {
        SQLiteDatabase db=helper.getWritableDatabase();
        db.insert("providers",null,values);
    }
    public void updateProvider(ContentValues values,String url)
    {
        SQLiteDatabase db=helper.getWritableDatabase();
        db.update("providers",values,"url=?",new String[]{url});
    }
    public Cursor getProviders() {
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor csr=db.query("providers",null,null,null,null,null,null);

        return csr;
    }

/* fino ad adesso il nostro DbManager era in grado di leggere e scrivere nel database le informazione solo riguardo a "providers"
adesso implementeremo il nostro DbManager con ulteriori funzioni. Per fare ciò dobbiamo fare la controparte.
Immagginiamo che arrivi un oggetto di tipo ArticleInfo all'interno del nostro DbManager e salviamo questi dati all'interno
di un contenitore ContentValues cv e lo inizializziamo con i dati dell'articolo quali title e url ed infine dall'helper
recuperiamo un riferimento in scrittura al database ed andiamo ad operare un inserimento con il metodo insert() nella
tabella "articles" il contentvalue che abbiamo appena inizializzato con i valori dell'articolo da salvare con questa
invocazionedi newArticle.
Per quanto riguarda la creazione di un metodo per recuperare gli articoli possiamo operare a similitudine come nel metodo
getProviders di sopra con la creazione di getArticles dove potremmo decidere se utilizzare l'adapter che avevamo creato noi
o un CursorAdapter. Tale metodo non farà altro che recuperare tutto il set di articoli e restituirlo. A questo punto ci
spostiamo su ManagerActivity dove con
 List<ArticleInfo> list= RssParser.parseXML(buffer.toString());
 return buffer.toString();
alle notizie scaricate veniva fatto il parsing tramite il flusso stringa del buffer e aggiunte in una list di tipo ArticleInfo
Esse non sono altro che le notizie che devono andare a finire dentro il nostro DbManager all'interno del metodo newArticle()
Per questo sotto quel codice creo un ciclo for -->
*/
    public void newArticle(ArticleInfo ai)
    {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        ContentValues cv=new ContentValues();
        cv.put("title",ai.getTitle());
        cv.put("url",ai.getUrl());
        // cv.put("date",format.format(ai.getDate()));
        cv.put("description",ai.getDescription());
        try
        {
            helper.getWritableDatabase().insert("articles",null,cv);
        }
        catch(SQLiteException e)
        { }
    }

    public Cursor getArticles() {
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor csr=db.query("articles",null,null,null,null,null,"date DESC");

        return csr;
    }
    /* con questo metodo che copio tale e quale da getArticles (questo infatti restituisce tutti gli articoli) ne modifico il comportamento
    e lo chiamo getLastArticles(), lascio il riferimento al database in sola lettura come prima ed imposto il cursore per prelevare le
    stesse voci di getArticles() con la differenza che imporrò il limite pari a 10. A questo punto andiamo in ArticleActivity nella gestione
    del menu dentro onOptionsItemSelected -->
    */
    public Cursor getLastArticles()
    {
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor crs=db.query("articles",null,null,null,null,null,"date DESC","10");

        return crs;
    }

    public Cursor getBookmarks()
    {
        SQLiteDatabase db=helper.getReadableDatabase();

        String q = "SELECT * FROM articles WHERE bookmark = 1 ORDER BY date DESC";
        Cursor csr=db.rawQuery(q,null);
        return csr;
    }

    public void deleteArticles()
    {
        SQLiteDatabase db=helper.getWritableDatabase();
        db.delete("articles",null,null);
    }

    /* questo metodo ci permette di identificare la notizia passandogli un intero ed identificarla se è all'interno dei bookmarks
    o meno grazie al valore booleano passatogli.
    Non faremo nulla di diverso rispetto a quello già fatto in quanto prenderemo un riferimento al nostro database di nome db grazie
    al nostro helper, tale riferimento sarà ovviamente di tipo writable in quanto dovremo andare a scrivere o meno il valore 0 oppure 1
    impostiamo poi un ContentValues che servirà per dire che se il valore di bookmark è stato passato ed il valore di contentvalues sarà
    uguale all'intero 0 oppure 1. A questo punto andremo ad effettuare le modifiche all'interno del database passando al metodo update
    la tabella "articles" che sarà la destinazione delle modifiche, il valore di contentvalues (che sarà 0 oppure 1) e dicendogli dove
    effettuare tali modifiche ("_id=?") nel caso in cui la nostra notizia sia stata portata all'interno del nostro metodo setBookmark.
    Questo metodo per poter essere utilizzato deve essere attivato in base al click effettuato sulla nostra stellina dall'utente e lo
    andremo ad utilizzare all'interno dell'ArticlesActivity -->
     */
    public void setBookmark(int id, boolean bookmark){
        SQLiteDatabase db=helper.getWritableDatabase();
        ContentValues cv=new ContentValues();
        if (bookmark)
            cv.put("bookmark",Integer.valueOf(1));
        else
            cv.put("bookmark",Integer.valueOf(0));
        db.update("articles",cv,"_id=?",new String[]{Integer.toString(id)});
    }

    /* questo metodo ci permette di effettuare la cancellazione della voce clicca nel contextmenu in base all'url
    Gli passiamo un riferimento al database in scrittura ed andremo ad effettuare una cancellazione all'interno del database nella
    tabella providers alla voce url dipendente dalla posizione che gli èstata passata in base all'_id tramite la string url del metodo
    che si trova in ingresso che necessita tale passaggio tramite un array di stringhe.
      */
    public void deleteProvider(String url)
    {
        SQLiteDatabase db=helper.getWritableDatabase();
        db.delete("providers","url=?",new String[]{url});
    }
}
