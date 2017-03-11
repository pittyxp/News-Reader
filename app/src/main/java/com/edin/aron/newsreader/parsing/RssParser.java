package com.edin.aron.newsreader.parsing;

import com.edin.aron.newsreader.beans.ArticleInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Aron on 29/01/17.
 */
/* Questa classe contiene un solo metodo pubblico e statico denominato parseXML che riceve un url sotto forma di stringa (String rss)
in formato xml scaricato dalla rete e ne fa il parsing restituendo una lista di oggetti ArticleInfo contenente tanti oggetti quanti
quelli che saranno contenuti nel file xml ognuno rappresentante un articolo.

 */
public class RssParser {

    public static List<ArticleInfo> parseXML(String rss)
    {
        List<ArticleInfo> res=new ArrayList<ArticleInfo>();
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        try {
            builder=factory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {

        }
        try {
            Document doc=builder.parse(new InputSource(new StringReader(rss)));
            doc.normalize();
            NodeList list=doc.getElementsByTagName("item");
            for (int i=0;i<list.getLength();i++)
            {
                Node n=list.item(i);
                if (n.getNodeType()==Node.ELEMENT_NODE)
                {
                    Element e=(Element) n;
                    String title=e.getElementsByTagName("title").item(0).getTextContent();
                    String url=e.getElementsByTagName("guid").item(0).getTextContent();
                    // String date=e.getElementsByTagName("pubDate").item(0).getTextContent();
                    String description=e.getElementsByTagName("description").item(0).getTextContent();
                    ArticleInfo ai = new ArticleInfo(title,url,description);
                    /* try {
                        SimpleDateFormat format= new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
                        Date dt = format.parse(date);
                        ai.setDate(dt);
                    } catch (ParseException e1) {
                    }*/
                    res.add(ai);
                }
            }
        } catch (SAXException e) {
        } catch (IOException e) {
        }
    return res;
    }
}
