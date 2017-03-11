package com.edin.aron.newsreader.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edin.aron.newsreader.R;
import com.edin.aron.newsreader.beans.ArticleInfo;
import com.edin.aron.newsreader.database.DbManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by Aron on 29/01/17.
 */
/* dentro questa classe proveniendo da ManagerActivity vediamo che abbiamo creato il nostro CursorAdapter e asimilitudine
di quanto già fatto prima abbiamo creato il nostro adapter con la classe Cursor implementando i due metodi obbligatori
newView per dare un layout e bindView. In questo metodo creiamo una TextView di nome txt e la settimamo al nostro layout title
al quale assegneremo questa volta il valore del testo recuperato dal cursore mediante la posizione id di nome "title" e lo
stesso procedimento lo facciamo per il recupero dell'url. Tutta questa logica di recupero però non conviene mantenerla nel
metodo onCreate quindi dall'inizializzazione dell'adapter al collegamento alla ListView la passiamo nel metodo onStart del
ciclo di vita dell'Activity. Questo lo facciamo perchè se dovessimo tornare indietro nell'activity allora dobbiamo controllare
se l'adapter era già stato inizializzato o meno. Se non lo è mai stato allora lo inizializziamo, altrimenti ad aggiornare
il cursore delle notizie solamente tramite il metodo db.getArticles().
 */
public class ArtciclesActivity extends ListActivity {

    private CursorAdapter adapter=null;
    private DbManager db=new DbManager(this);

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter==null)
        {
            adapter=new CursorAdapter(this,db.getArticles(),false) {
                SimpleDateFormat format=new SimpleDateFormat("dd/MM");
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    View v= LayoutInflater.from(context).inflate(R.layout.article_layout,null);
                    return v;
                }
                @Override
                public void bindView(View view, Context context, final Cursor cursor) {
                    ArticleInfo article= (ArticleInfo) adapter.getItem(cursor.getPosition());
                    TextView txt=(TextView) view.findViewById(R.id.title);
                    txt.setText(article.getTitle());
                    txt= (TextView) view.findViewById(R.id.url);
                    txt.setText(article.getShortUrl());
                    //txt= (TextView) view.findViewById(R.id.date);
                    //txt.setText(format.format(article.getDate()));
                    txt= (TextView) view.findViewById(R.id.description);
                    txt.setText(article.getDescription());

                    /* all'interno di questo metodo facciamo gli intent impliciti dell'ImageButton nel caso in cui
                    si voglia condividere la notizia con qualcun'altro. Definiamo quindi il nostro bottone btn riferito
                    alla view e defininiamo il metodo setOnClickListener. All'interno del metodo onClick andremo prima
                    di tutto a trovare la posizione cliccata. Per fare ciò definiamo position che viene trovato passando
                    alla getListView() il risultato preso da getPositionForView() che necessita di una view vera e propria
                    la quale sarà data dal layout contenitore del cast di v.getParent() che è il pulsante sul quale è stata
                    effettuata la pressione con il RelativeLayout.
                    A questo punto recueriamo l'articolo con ArticleInfo di nome ai tramite il nostro adapter mediante getItem
                    ed effettuare la vera e propria condivisione tramite un intent al quale diamo un'azione di send e poi gli
                    inseriamo il text prendendolo da ai e selezionando title ed url con testo a capo (\n) e definiremo anche
                    il tipo con "text/plain" che è un formato mime e attiveremo lo startactivity sull'intent i.
                     */
                    ImageView btn= (ImageView) view.findViewById(R.id.share);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position=getListView().getPositionForView((LinearLayout)v.getParent());
                            ArticleInfo ai = (ArticleInfo) adapter.getItem(position);

                            Intent i=new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT,ai.getTitle()+"\n"+ai.getUrl());
                            i.setType("text/plain");
                            startActivity(i);
                        }
                    });

                    /* Definiamo qui il metodo per la selezione delle notizie preferite che l'utente seleziona per poter inserire il valore 1
                    all'interno del campo bookmark del nostro database articles. Definiamo subito il bottone bookmark che è la nostra stellina
                    Essendo dentro il metodo bindView abbiamo disponibile la variabile cursor che ci permetterà di controllare il valore proveniente
                    dall'elemento bookmark del databasae controllando se è 0 oppure 1. Imposteremo quindi il layout della stellina in base al valore
                    all'interno del database appartenente alla notizia visualizzata se inserita nella lista dei preferiti.
                    Per effettuare la memorizzazione dello 0 o dell'1 allora dobbiamo necessariamentr andare sul nostro DbManager e gestire la
                    memorizzazione di tale valore al suo interno per implementare le funzioni di tale classe inserendo un metodo di nome ad esempio
                    setBookmark che ci permetterà di effettuare tale salvataggio al quale passerempo due valori che saranno un numero intero id per
                    identificare la notizie ed un valore booleano che dirà se vero o falso per dire che la notizia è all'intenro dei bookmarks
                    oppure no. -->
                    Dopo aver creato il metodo che setta a 0 oppure 1 la stellina impostiamo allora un setOnClickListener sulla stellina stessa,
                    implementiamo il metodo onClick e naturalmente avremo bisogno di impostare a final il valore di star in quanto lo manipoleremo
                    all'inerno del metodo onClick. Andremo ad identificare quindi la variabile id che sarà la posizione della notizie grazie
                    all'identificazione della view passata al metodo getPositionForView il quale risultato sarà passato a getListView.
                    A quel punto andremo a recuperare le nostre informazioni sul nostro articolo definendo il nostro oggetto ArticleInfo ai che avrà
                    valore in base al metodo getItem che avrà in ingresso il valore di position appena definita. Questo oggetto avrà tutte le informazioni
                    relative alla notizia dove sarà stata cliccata la stellina ed andremo ad impostare il valore di bookmark che dovremo passare al metodo
                    setBookmark invertendo il valore rispetto al valore attuale con il metodo isBookmark() e quindi il vero valore che dovrà essere passato
                    dovrà essere il contrario del valore appena posseduto da quell'articolo un istante prima. Andremo quindi ad invocare setBookmark
                    passandogli l'id dell'articolo tramite il metodo creto getId() e gli passeremo bookmark.

                    A questo punto dovremo andare ad effettuare la modifica sulla stellina in maniera tale che sia subito visibile con un if ed else ed
                    infine andremo ad aggiornare i dati del cursore con db.getArticles().
                     */
                    final ImageView star= (ImageView) view.findViewById(R.id.bookmark);
                    if (cursor.getInt(cursor.getColumnIndex("bookmark"))==1)
                        star.setImageResource(android.R.drawable.star_big_on);
                    else
                        star.setImageResource(android.R.drawable.star_big_off);
                    star.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position=getListView().getPositionForView(v);
                            ArticleInfo ai = (ArticleInfo) getItem(position);
                            boolean bookmark=!ai.isBookmark();
                            db.setBookmark(ai.getId(),bookmark);
                            if (bookmark)
                                star.setImageResource(android.R.drawable.star_big_on);
                            else
                                star.setImageResource(android.R.drawable.star_big_off);
                            adapter.changeCursor(db.getArticles());
                        }
                    });

                    /* adesso vedremo come poter integrare nella nostra App na funzione che permetterà l'invio dei dati ad un server
                    web remoto accendendo sempre alla rete. Il bottone in questione è l'image button di nome post. Abbiamo definito
                    il bottone per identificare il clic su di esso nel metodo onClick tramite l'identificazione della variabile position,
                    successivamente nel metodo onCLick definiamo anche l'ggetto ai di ArticleInfo che sarà uguale al valore della posizione
                    rilavata passata al nostro adapter per prelevare le infomazioni.
                    Successivamente definiremo una nuova AsyncTask per poter effettuare dei lavori al di fuori dell'interfaccia utente
                    passando 2 dei 3 valori che saranno l'ggetto ArticleInfo e una String. Definiremo dentro AsyncTask i metodi doInBackground
                    che ci permetterà di effettuare le operazioni di salvataggio e onPostExecute per effettuare le operazioni post salvataggio.
                    Nel primo passando l'oggetto ArticleInfo l'oggetto stesso conterrà il titolo e l'url della notizia che vorremo inviare in
                    remoto. Per fare ciò prendiamo il nostro articolo ai conservato nella variabile ai con riferimento all'array di parametri
                    params che sarà sicuramente nella posizione 0. Adesso utilizzeremo la classe HttpURLConnection dato che usiamo le API 21 o sup.
                    Essa è compresa nel framework Android a differenza della classe utilizzata per lo scaricamente che è contenuta del framework Java.
                    Creiamo un oggetto usando la classe URL di nome paginaURL e gli settiamo il nostro indirizzo del server web. A questo punto
                    Creaiamo adesso un oggetto json di nome jsonParam al quale andremo ad inserire "title" ed "url" prendendone i vaolori con l'oggetto
                    ArticleInfo di nome ai definito in precedenza.
                    A questo punto dovremo creare l'oggetto JSON di nome jsonParam tramite la classe JSONObject al quale andremo ad inserire il
                    valore "title" ed "url" tramite il metodo getTitle() e getUrl() passato all'oggetto ArticleInfo di nome ai. Lo definiamo all'interno
                    di un costrutto try poichè dovremo gestire delle eccezioni.
                    Istanziamo l'oggetto wr della classe OutpoutStreamWriter che ci permetterà di creare un oggetto contenente l'url per effettuare la
                    scrittura e gli passeremo successivamente l'oggetto jsonParam in formato string. Poi effettuerem l'invio effettivo dei dati.
                    Tramite il metodo in lettura successivo non faremo altro che andare a leggere i dati per avere una conferma che l'invio era stato
                    effettuato correttamente.
                    Ritorneremo un oggetto java di nome jsonResp che sarà un oggetto java derivante dal parsing effettuato dell'oggetto json appena letto.
                    Successuvamente nel caso di errore ogni eccezione non definita farà scorrere il flusso e a ritornerà un valore nullo.
                    Nel metodo onPostExecute verrà passata la string ottenuta dall'oggetto jsonResp del metodo doInBackground in maniera automatica e verrà
                    valutata semplicemtne controllando se è nulla o meno.
                    */
                    btn = (ImageView) view.findViewById(R.id.post);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position=getListView().getPositionForView((LinearLayout)v.getParent());
                            ArticleInfo ai = (ArticleInfo) adapter.getItem(position);
                            new AsyncTask<ArticleInfo, Void, String>() {
                                @Override
                                protected String doInBackground(ArticleInfo... params)
                                {
                                    ArticleInfo ai = params[0];

                                    URL paginaURL= null;
                                    try {
                                        paginaURL = new URL(getString(R.string.remote_url));
                                    } catch (MalformedURLException e) {
                                        Toast.makeText(ArtciclesActivity.this,"Link ERRATO",Toast.LENGTH_LONG).show();
                                        return null;
                                    }
                                    HttpURLConnection connection = null;
                                    try {
                                        connection = (HttpURLConnection) paginaURL.openConnection();
                                    } catch (IOException e) {
                                        Toast.makeText(ArtciclesActivity.this,"I/O Error",Toast.LENGTH_LONG).show();
                                        return null;
                                    }
                                    try {
                                        JSONObject jsonParam = new JSONObject();
                                        jsonParam.put("title",ai.getTitle());
                                        jsonParam.put("url",ai.getUrl());

                                        connection.setDoOutput(true);

                                        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream()); //SCRITTURA
                                        wr.write(jsonParam.toString());
                                        wr.flush();

                                        InputStream buffer = new BufferedInputStream(connection.getInputStream()); //LETTURA
                                        JSONObject jsonResp = new JSONObject(mostroDati(buffer));
                                        return jsonResp.getString("result");
                                        } catch (MalformedURLException e) {
                                          e.printStackTrace();
                                        } catch (IOException e) {
                                         e.printStackTrace();
                                        } catch (JSONException e) {
                                         e.printStackTrace();
                                        }
                                         return null;
                                }
                                /* QUESTO METODO NEL CORSO è MANCANTE E DEVO VEDERE A COSA SERVE E COME FUNZIONA */
                                private JSONTokener mostroDati(InputStream buffer) {
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    String text=null;
                                    if (s!=null)
                                        text=s;
                                    else
                                        text="Salvataggio FALLITO";
                                    Toast.makeText(ArtciclesActivity.this,text,Toast.LENGTH_LONG).show();
                                }
                            }.execute(ai);
                        }
                    });
                }
/* copiamo ed incolliamo il metodo getItem() per prelevare la riga che si clicca nell'activity una volta che si sono
scaricati tutti gli articoli e si sono mostrati a schermo. Qui però anziche a name dovremo accedere al titolo della
notizia mediante il campo "title". Il risultato non sarà più un oggetto ProviderInfo ma un oggetto ArticleInfo, cioè
una singola notizia. In questo modo qui potremo prelevare da una singola notizia titolo ed url.
Questo ci servirà appunto nella gestione del clic sulla singola notizia. Noi stiamo utilizzando una ListActivity che
contiene già al suo interno una ListView. Per gestire il click dovremo andare ad imposta un OnItemClickListener, quindi
andiamo prima a prelevare un riferimento mediante getListView() ed insieriamo un nuovo item OnItemClickListener {} tramite
la quale viene richiesta subito l'implementazione del metodo necessario onItemClick che per ogni click verrà invocato esso
stesso. All'interno di esso troviamo alcuni riferimenti ed in particolare ci servirà int position che è il riferimento della
posizione con restituzione di un intero. Questo è importante perchè grazie al nostro adapter potremo andare a prendere
le notizie relative all'item che è stato cliccato su quella posizione.
Per poter mostrare il contenuto completo ci servirà una WebView che è un vero è proprio browser che ha necessità in input
di un context (in questo caso prendiamo l'ArticlesActivity) e successivamente andremo ad impostare il WebClient all'interno
e gli andremo a dire che a questo punto vogliamo caricare il contenuto della notizia che risponde all'indirizzo ai.getUrl();
Una volta creata la nostra WebView per poterla visualizzare la dovremo inserire in una finestra di dialogo come fatto in
passato tramite un AlertDialog ed un Builder e andremo ad assegnare la WebView alla nostra finestra di dialogo con setView(
e per poterla chiudere agevolmente imposteremo il bottone Chiudi con un setPositiveButton che implementarà il metodo onClick
al quale andremo a dire solamente dialog.dismiss();.
A questo punto l'unica cosa da fare ancora è che la finestra deve essere mostrata tramite dialog.show()
Non dimentichiamo che qui c'è stato il bisogno di inserire il tema del dialog in aggiunta al video
*/
                @Override
                public Object getItem(int position) {
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Cursor csr=getCursor();
                    csr.moveToPosition(position);
                    String title=csr.getString(csr.getColumnIndex("title"));
                    String url=csr.getString(csr.getColumnIndex("url"));
                    String description=csr.getString(csr.getColumnIndex("description"));
                    int bookmark=csr.getInt(csr.getColumnIndex("bookmark"));
                    int _id=csr.getInt(csr.getColumnIndex("_id"));
                    ArticleInfo pi = new ArticleInfo(title,url,description);
                    /*try {
                        pi.setDate(format.parse(csr.getString(csr.getColumnIndex("date"))));
                    } catch (ParseException e)
                    { }*/
                    if (bookmark==1)
                        pi.setBookmark(true);
                    else
                        pi.setBookmark(false);
                    pi.setId(_id);
                    return pi;
                }
            };
            getListView().setAdapter(adapter);

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArticleInfo ai = (ArticleInfo) adapter.getItem(position);
                    WebView web = new WebView(ArtciclesActivity.this);
                    web.setWebViewClient(new WebViewClient());
                    web.loadUrl(ai.getUrl());
                    AlertDialog.Builder dialog=new AlertDialog.Builder(ArtciclesActivity.this,R.style.AlertDialog_Style);
                    dialog.setView(web);
                    dialog.setPositiveButton("Chiudi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
        }
        else
            adapter.swapCursor(db.getArticles());
    }
    /* all'inrerno di questo metodo imposto il valore del nostro adapter assegnandogli il valore corretto nel caso in cui seleziono
    l'opzione del menu ultimi articoli o tutti gli articoli andandogli ad assegnare i valori corretti prendendoli dai due metodi
    di DbManager e rispettivamente getArticles e getLastArticles.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id)
        {
            case R.id.second_actvity:
                Intent i=new Intent(this,ManagerActivity.class);
                startActivity(i);
                break;
            case R.id.last_articles:
                adapter.changeCursor(db.getLastArticles());
                break;
            case R.id.all_articles:
                adapter.changeCursor(db.getArticles());
                break;
            case R.id.bookmarks:
                adapter.changeCursor(db.getBookmarks());
                break;
            case R.id.cleardb:
                db.deleteArticles();
                adapter.changeCursor(db.getArticles());
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
