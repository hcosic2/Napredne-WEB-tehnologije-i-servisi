package org.foi.nwtis.hcosic2.web.slusaci;

import static com.sun.faces.facelets.util.Path.context;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.foi.nwtis.hcosic2.web.dretve.ObradaPoruka;

/**
 * Slušač aplikacije je klasa koja je tipa slušač konteksta i pokreče
 * pozadinsku dretvu koja vrši operacije nad porukama
 * @author Hrvoje
 */

@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    ObradaPoruka obrada;
    private static BP_Konfiguracija bpk;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getRealPath("/WEB-INF")
                + File.separator
                + sc.getInitParameter("konfiguracija");

        bpk = new BP_Konfiguracija(datoteka);
        sc.setAttribute("BP_Konfig", bpk);

        Konfiguracija konfig = null;
        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            sc.setAttribute("Mail_Konfig", konfig);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        ObradaPoruka obrada = new ObradaPoruka();
        obrada.setServletContext(sc);
        obrada.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        if (obrada != null) {
            obrada.interrupt();
        }
    }

    public static ServletContext getSc() {
        return sc;
    }
    /**
     * Funkcija koja dohvaća podatke iz konfiguracijskog file-a
     * i na temelju tih podataka vrši spajanje na bazu podataka
     * @return ako je spajanje na bazu uspješno vraća vezu prema bazi
     */
    public static Connection getConnection() {
        Connection con = null;
        String server = bpk.getServerDatabase();
        String bazaPodataka = bpk.getUserDatabase();
        String url = server + bazaPodataka;
        String korisnik = bpk.getUserUsername();
        String lozinka = bpk.getUserPassword();

        try {
            Class.forName(bpk.getDriverDatabase());
            con = DriverManager.getConnection(url, korisnik, lozinka);
            System.out.println("Connection completed.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return con;
    }
}
