package com.edin.aron.newsreader.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.edin.aron.newsreader.beans.ArticleInfo;
import com.edin.aron.newsreader.beans.ProviderInfo;
import com.edin.aron.newsreader.database.DbManager;
import com.edin.aron.newsreader.parsing.RssParser;
import com.edin.aron.newsreader.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ManagerActivity extends ListActivity {

    private CursorAdapter adapter=null;
    private DbManager db=new DbManager(this);
    private ProgressDialog progress=null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* adesso aggiungiamo un elemento alla actionbar che ci permette di ritornare alla activity home andando ad impostare su true
        il valore setDisplayHomeUpEnabled. Ciò permetterà di far comparire l'icona della freccia accanto all'icona dell'app.
        Inoltre dovremo impostare, per fare ciò, che la parent activity della ManagerActivity è ArticlesActivity e lo andremo a fare
        nel file manifest
         */
        getActionBar().setDisplayHomeAsUpEnabled(true);

        adapter= new CursorAdapter(this,db.getProviders(),false)
        {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewgroup)
            {
                View v=LayoutInflater.from(context).inflate(R.layout.provider_layout,null);
                return v;
            }
            @Override
            public void bindView(View view, Context context, Cursor cursor)
            {
                TextView txt=(TextView) view.findViewById(R.id.title);
                txt.setText(cursor.getString(cursor.getColumnIndex("name")));
                TextView txt2= (TextView) view.findViewById(R.id.url);
                txt2.setText(cursor.getString(cursor.getColumnIndex("url")));
            }
            /* aggiungiamo questo metodo che ci permette di prelevare i dati dal cursore ad una relativa posizione
            prima creaiamo un riferimento al cursore e ci spostiamo con moveToPosition e preleviamo da quella posizione gli
            elementi che ci servono fra gli elementi della tabella providers dove ci sarà un url che ci interessa ed il campo name
            e successivamente andremo a popolare un oggetto ProviderInfo di nome pi del package beans che verrà restituito
            e aggiungiamo la voce per scaricare nel layout del file manager_main_menu e poi andiamo nel metodo onOptionsItemSelected
            per aggiungere un nuovo case del nuovo bottone
             */
            @Override
            public Object getItem(int position) {
                Cursor csr=getCursor();
                csr.moveToPosition(position);
                String name=csr.getString(csr.getColumnIndex("name"));
                String url=csr.getString(csr.getColumnIndex("url"));
                ProviderInfo pi=new ProviderInfo(name,url);
                return pi;
            }
        };
        getListView().setAdapter(adapter);
        registerForContextMenu(getListView());
    }

    // chiedere a Mario!!!
    @Override
    public void onPause() {

        super.onPause();
        if(progress != null)
            progress.dismiss();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.context_menu,menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /* qui adesso andremo ad identificare le varie voci del menu contestuale appena creato con il codice sopra. Identificheremo le due
    voci Modifica ed Elimina. Per fare ciò allora istanziamo un int di nome id l'id della voce di nome item del MenuItem che è stata
    selezionata nel menu contestuale. Il modo con il quale vengono gestiti i click è molto simile a quello del menu options dentro
    onOptionsItemSelected. Una cosa diversa che dobbiamo fare è quello di andare a capire su quale riga è stato effettuato il click
    con AdapterContextMenuInfo istanziandone un nuovo oggetto di nome info che avrà item.getMenuInfo() e successivamente potremo prendere
    la posizione della nostra variabile info e ad assegnarla alla variabile position interna a questo metodo.
    A questo punto andiamo a recuperare le informazioni del provider sul quale noi abbiamo richiesto l'operazione tramite il menu contestuale
    creando un nuovo oggetto di nome p che avrà tutte le informazioni di quella voce.
    Con switch identificheremo le operazioni da effettuare in base all'id identificato, cioè in base alla voce cliccata in base ai metodi
    che andremo a creare ad hoc nel DbManager e li invochiamo qui tramite il metodo deleteProvider() e dopo la cancellazione diciamo
    all'adapter di aggiornarsi richiedendo la nuova lista di provider.
    Per quanto riguarda la voce modifica ci viene in aiuto il metodo showDialog che serve per far apparire la finestra di dialogo per
    inserire un nuovo provider quando si clicca sulla voce options menu oppure per modificarne uno. La differenza del comportamento sta
    solo nel valore di input che gli viene passato alla sua invocazione. Se viene passato un url allora il metodo darà per scontato
    che se pi è uguale a null allora stiamo inserendo un provider nuovo mentre se pi è diverso da null verranno caricati i valori nelle
    caselle di testo che dovranno essere modificati grazie ad un secondo if che permette di controllare nel caso di pi diverso da null
    che riuscirà ad effettuare un update del database tramite updateProvider passandogli i valori di cv e del nuovo url.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position=info.position;
        ProviderInfo p = (ProviderInfo) adapter.getItem(position);
        switch(id)
        {
            case R.id.elimina:
                db.deleteProvider(p.getUrl());
                adapter.changeCursor(db.getProviders());
                break;
            case R.id.modifica:
                showDialog(p);
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // int id=item.getItemId(); superfluo perchè uso l'altra madalità statica
        switch(item.getItemId())
        {
            case R.id.new_provider:
                showDialog(null);
                break;
            case R.id.download:
                for(int i=0;i<adapter.getCount();i++)
                {
                    ProviderInfo tmp= (ProviderInfo) adapter.getItem(i);
                    new DownloadTask().execute(tmp.getUrl());
                }
        }
        return super.onOptionsItemSelected(item);
    }
    private void showDialog(final ProviderInfo pi) {
        /* ATTENZIONE CHE HO DOVUTO PASSARE R.style.AlertDialog_Style X DEFINIRE UNO STILE ALL'ALERT DIALOG
        ALTRIMENTI DAVA ERRORE
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_Style);
        final View layout = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setTitle("Nuovo Provider");
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
                /* in caso di inserimento e salvataggio devo leggere quello che l'utente ha scritto ed uso EditText
                facendo riferimento con layout.findViewById e dichiarando però la variabile layout utilizzata sopra
                come final View layout poichè era stata definita all'interno di onOptionsItemSelected ma adesso la stiamo
                utilizzando all'interno del metodo onClick interno al precedente quindi in pratica la variabile layout
                risulterebbe esterna al nostro attuale metodo
                 */
        if (pi != null) {
            EditText title = (EditText) layout.findViewById(R.id.txt_title);
            EditText url = (EditText) layout.findViewById(R.id.txt_url);
            title.setText(pi.getName());
            url.setText(pi.getUrl());
        }
        builder.setPositiveButton("Salva", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 /* a questo punto potremmo creare un nuovo oggetto provider definiti dalla classe Provider info
                 che conterrà al suo interno la stringa inserita dall'utente contenente sia title che url tramite
                 la classe ProviderInfo che ha anche un suo costruttore che ha bisogno di una stringa e tramite
                 getText restituirà un oggetto editable.
                 Lo trasformo allora in stringa con toString. Lo faccio per entrambi url e title. a questo punto
                 manca solo il salvataggio effettivo poichè è solo salvato in maniera dinamica.
                 */
                EditText title = (EditText) layout.findViewById(R.id.txt_title);
                EditText url = (EditText) layout.findViewById(R.id.txt_url);

                ContentValues cv = new ContentValues();
                cv.put("name", title.getText().toString());
                cv.put("url", url.getText().toString());
                if (pi != null)
                    db.updateProvider(cv,pi.getUrl());
                else
                    db.newProvider(cv);

                adapter.swapCursor(db.getProviders());

                dialog.dismiss();
            }
        });
        builder.show();
    }
            /* qui dobbiamo fare in modo di dire al nostro cursore di restituirci tutti gli url che il cursor adapter possiede
            grazie al metodo getCount() che ci dice quante voci contiene l'adapter che è uguale dal numero di elementi prelevati
            dal database.
            Chiediamo a questo punto all'adapter di darci l'elemento tramite il metodo getItem definito prima nella posizione i
            e con l'url contenuto in questo elemento tmp possiamo andare ad invocare il download task e dire da questo punto url
            scarica le notizie
            */ /*
                for (int i=0;i<adapter.getCount();i++)
                {
                    ProviderInfo tmp = (ProviderInfo) adapter.getItem(i);
                    new DownloadTask().execute(tmp.getUrl());
                }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater new_provider_inflater=getMenuInflater();
        new_provider_inflater.inflate(R.menu.manager_main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class DownloadTask extends AsyncTask<String,Integer,String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(ManagerActivity.this,R.string.update,Toast.LENGTH_LONG).show();
            if (progress==null)
            {
                progress=new ProgressDialog(ManagerActivity.this);
                progress.setMax(100);
                progress.setMessage("Download in corso...");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
            progress.setProgress(0);
            progress.show();
            progress.setCancelable(false);
        }
        @Override
        protected String doInBackground(String... params) {
            URL url=null;
            try {
                url=new URL(params[0]);
            } catch (MalformedURLException e) {
                return null;
            }
            StringBuffer buffer=null;
            try {
                BufferedReader reader=new BufferedReader(new InputStreamReader(url.openStream()));
                String tmp=null;
                buffer=new StringBuffer();
                while ((tmp=reader.readLine())!=null)
                {
                    buffer.append(tmp);
                }
            } catch (IOException e) {
                return null;
            }
            /* al termine del ciclo while adesso buffer sarà completo di tutto il flusso rss scaricato da quel determinato url
            per fare la prova dobbiamo passare il contenuto del buffer ad RssParser in particolare al suo metodo parserXML
            che richiede la string rss in input. Tale metodo risulta statico e quindi non è necessario istanziarlo.
            Per fare ciò possiamo fare in questo modo: ci facciamo restituire un oggetto ArticleInfo di nome list passandogli
            il buffer.toString(). A questo punto non dobbiamo far altro che attivare lo scaricamente nel momento in cui viene
            selezionato l'oggetto ProviderInfo con un new DownloadTask
            */
            List<ArticleInfo> list = RssParser.parseXML(buffer.toString());
            /* creo un ciclo for sulla list per inserire ogni elemento ai  della lista dentro ogni elemento db dentro il
             metodo newArticle() di DbManager. Adesso andiamo dentro ArticlesActivity -->
             */
            for (ArticleInfo ai: list)
                db.newArticle(ai);
            return buffer.toString();

        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            progress.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            progress.dismiss();
            int idMsg=0;
            if (res!=null)
                idMsg=R.string.msg_download_ok;
            else
                idMsg=R.string.msg_download_ko;
            Toast.makeText(ManagerActivity.this,idMsg,Toast.LENGTH_LONG).show();

        }

    }
}
