package org.foi.nwtis.hcosic2.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.hcosic2.rest.klijenti.OWMKlijent;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPodaci;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Pozadinska dretva u kojoj se preuzimaju u pravilnim intervalima meteorološki podaci
 * putem REST web servisa openweathermap.org za izabrani skup parkirališta i 
 * pohranjuju se u tablicu u bazi podataka (METEO)
 * 
 * @author Hrvoje
 */

public class PreuzmiMeteoPodatke extends Thread {

    private int spavanje;
    private boolean radi = true;
    private int broj = 0;
    private ServletContext sc = null;
    private Connection veza;
    private Statement statement;

    public PreuzmiMeteoPodatke() {

    }

    @Override
    public void interrupt() {
        this.radi = false;
        try {
            veza.close();
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void run() {
        try {
            veza = SlusacAplikacije.getConnection();
            statement = this.veza.createStatement();
            while (radi) {
                broj++;
                System.out.println("Pozadinska dretva iteracija: " + broj);
                dohvatiPodatke();
                sleep(spavanje * 1000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void start() {
        sc = SlusacAplikacije.getServletContext();
        BP_Konfiguracija bpk = (BP_Konfiguracija) sc.getAttribute("BP_Konfig");
        spavanje = bpk.getIntervalThread();
        super.start();
    }

    /**
     * Dohvaća sva parkiralista iz tablice parkiralista u bazi i za ta parkiralista
     * povlaci meteo podatke te ih upisuje u bazu u tablicu meteo
     */
    private void dohvatiPodatke() {
        try {
            String upit = "SELECT * FROM parkiralista";
            ResultSet rezultat = this.statement.executeQuery(upit);
            OWMKlijent owmk = new OWMKlijent(SlusacAplikacije.getOwmkey());
            PreparedStatement upit2 = veza.prepareStatement("INSERT INTO METEO(id, adresaStanice, latitude, longitude, "
                    + "vrijeme, vrijemeOpis, temp, tempMin, tempMax, vlaga, tlak, vjetar, vjetarSmjer) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            while (rezultat.next()) {
                upisiMeteoPodatke(upit2, rezultat.getString("LATITUDE"), rezultat.getString("LONGITUDE"),
                        owmk, Integer.parseInt(rezultat.getString("ID")), rezultat.getString("ADRESA"));
            }
            rezultat.close();
            upit2.close();
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja priprema podatke za upit koje je potrebno dodati u tablicu
     * meteo u bazi podataka
     * @param upit2 upit kojem treba pridružiti postavljene vrijednosti
     * @param latitude latitude parkiralista za koje se unose podaci u tablicu meteo
     * @param longitude longitude parkiralista za koje se unose podaci u tablicu meteo
     * @param owmk instanca klase owmk
     * @param id id parkiralista
     * @param adresa  adresa parkiralista
     */
    public void upisiMeteoPodatke(PreparedStatement upit2, String latitude, String longitude, OWMKlijent owmk, int id, String adresa) {
        try {
            MeteoPodaci mp = owmk.getRealTimeWeather(latitude, longitude);
            String vrijemeOpis = mp.getWeatherValue();
            String vrijeme = mp.getCloudsName();
            String vrijemeOpisSkraceno = vrijemeOpis.substring(0, Math.min(vrijemeOpis.length(), 25));
            String vrijemeSkraceno = vrijeme.substring(0, Math.min(vrijeme.length(), 25));
            upit2.setInt(1, id);
            upit2.setString(2, adresa);
            upit2.setString(3, latitude);
            upit2.setString(4, longitude);
            upit2.setString(5, vrijemeSkraceno);
            upit2.setString(6, vrijemeOpisSkraceno);
            upit2.setFloat(7, mp.getTemperatureValue());
            upit2.setFloat(8, mp.getTemperatureMin());
            upit2.setFloat(9, mp.getTemperatureMax());
            upit2.setFloat(10, mp.getHumidityValue());
            upit2.setFloat(11, mp.getPressureValue());
            upit2.setFloat(12, mp.getWindSpeedValue());
            upit2.setFloat(13, mp.getWindDirectionValue());
            upit2.execute();
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
