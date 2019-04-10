package org.foi.nwtis.hcosic2.web.slusaci;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.hcosic2.web.DodajParkiraliste;
import org.foi.nwtis.hcosic2.web.PreuzmiMeteoPodatke;

/**
 * Slušač aplikacije koji pokreće pozadinsku dretvu koja izvršava svoje zadatke
 * 
 * @author Hrvoje
 */

public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    private PreuzmiMeteoPodatke dretva;
    private static BP_Konfiguracija bpk;
    private static String gmgeokey;
    private static String owmkey;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getInitParameter("konfiguracija");
        String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNazivDatoteke = putanja + datoteka;

        BP_Konfiguracija bpk = new BP_Konfiguracija(puniNazivDatoteke);
        sc.setAttribute("BP_Konfig", bpk);

        dretva = new PreuzmiMeteoPodatke();
        dretva.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sc.removeAttribute("BP_Konfig");
        if (dretva != null) {
            dretva.interrupt();
        }

    }

    public static ServletContext getServletContext() {
        return sc;
    }

    /**
     * Funkcija koja dohvaća podatek iz konfiguracijske datoteke te na temelju tih
     * podataka izvršava spajanje na bazu
     * @return vraća vezu na bazu
     */
    public static Connection getConnection() {
        Connection con = null;
        bpk = (BP_Konfiguracija) getServletContext().getAttribute("BP_Konfig");
        String url = bpk.getServerDatabase() + bpk.getUserDatabase();
        String korisnik = bpk.getUserUsername();
        String lozinka = bpk.getUserPassword();
        gmgeokey = bpk.getGMGeoKey();
        owmkey = bpk.getOWMKey();

        try {
            Class.forName(bpk.getDriverDatabase());
            con = DriverManager.getConnection(url, korisnik, lozinka);
            System.out.println("Connection completed.");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("SQLException: " + ex.getMessage());
        }
        return con;
    }

    public static String getGmgeokey() {
        return gmgeokey;
    }

    public static void setGmgeokey(String gmgeokey) {
        SlusacAplikacije.gmgeokey = gmgeokey;
    }

    public static String getOwmkey() {
        return owmkey;
    }

    public static void setOwmkey(String owmkey) {
        SlusacAplikacije.owmkey = owmkey;
    }

}
