
package org.foi.nwtis.hcosic2.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.hcosic2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;

/**
 * Web application lifecycle listener koji nam služi za dohvaćanje podataka
 * iz konfiguracijske datoteke
 * @author Hrvoje
 */

@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc = null;
    private static BP_Konfiguracija bpk;
    private static String apiKey;
    private static String gmapiKey;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getInitParameter("konfiguracija");
        String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNazivDatoteke = putanja + datoteka;

        BP_Konfiguracija bpk = new BP_Konfiguracija(puniNazivDatoteke);
        sc.setAttribute("BP_Konfig", bpk);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
    /**
     * Funkcija koja dohvaća apiKey i gmapiKey iz konfiguracijske datoteke
     */
    public static void dohvatiPodatke(){
        bpk = (BP_Konfiguracija) getServletContext().getAttribute("BP_Konfig");
        apiKey = bpk.getOWMKey();
        gmapiKey = bpk.getGMGeoKey();
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiKey(String apiKey) {
        SlusacAplikacije.apiKey = apiKey;
    }

    public static String getGmapiKey() {
        return gmapiKey;
    }

    public static void setGmapiKey(String gmapiKey) {
        SlusacAplikacije.gmapiKey = gmapiKey;
    }

    public static ServletContext getServletContext() {
        return sc;
    }

    public static void setServletContext(ServletContext sc) {
        SlusacAplikacije.sc = sc;
    }
}
