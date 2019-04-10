package org.foi.nwtis.hcosic2.ws.serveri;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.jws.WebService;
import javax.jws.WebMethod;
import org.foi.nwtis.hcosic2.rest.klijenti.GMKlijent;
import org.foi.nwtis.hcosic2.rest.klijenti.OWMKlijent;
import org.foi.nwtis.hcosic2.rest.serveri.MeteoREST;
import org.foi.nwtis.hcosic2.web.podaci.Lokacija;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPodaci;
import org.foi.nwtis.hcosic2.web.podaci.Parkiraliste;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * SOAP web servis za meteorološke podatke spremljenih parkirališta. Operacije
 * se temelje na podacima koje se nalaze u tablici METEO u bazi podataka.Izvršava
 * sljedeće radnje: daje popis svih parkirališta i njihovih geo lokacija, 
 * dodaj parkiralište, dohvaća sve meteo podatke za parkiralište u intervalu, 
 * dohvaća zadnje meteo podatke za parkiralište, dohvaća važeće meteo podatke za
 * određeno parkiralište, vraća min i max temperaturu za određeno parkiralište u intervalu
 * 
 * @author Hrvoje
 */

@WebService(serviceName = "GeoMeteoWS")
public class GeoMeteoWS {

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dohvaća sva parkiralista iz tablice parkiralista u bazi podatka
     * @return vraća listu parkiralista
     */
    @WebMethod(operationName = "dajSvaParkiralista")
    public java.util.List<Parkiraliste> dajSvaParkiralista() {
        List<Parkiraliste> listaParkiralista = new ArrayList<>();
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM parkiralista";
            ResultSet rezultat = statement.executeQuery(upit);
            while(rezultat.next()){
                listaParkiralista.add(new Parkiraliste(rezultat.getInt("ID"), rezultat.getString("NAZIV"), rezultat.getString("ADRESA"),
                                        new Lokacija(rezultat.getString("LATITUDE"),rezultat.getString("LONGITUDE"))));
            }
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaParkiralista;
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dodaje parkiraliste u bazu podataka
     * @param parkiraliste parkiraliste koje treba dodati u bazu podataka
     */
    @WebMethod(operationName = "dodajParkiraliste")
    public void dodajParkiraliste(Parkiraliste parkiraliste) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            GMKlijent gmk = new GMKlijent(SlusacAplikacije.getGmgeokey());
            Lokacija lokacija = gmk.getGeoLocation(parkiraliste.getAdresa());
            String upit = "INSERT INTO parkiralista(naziv, adresa, latitude, longitude)"
                    + " VALUES('" + parkiraliste.getNaziv() + "','" + parkiraliste.getAdresa() + "'," + lokacija.getLatitude() + "," + lokacija.getLongitude() + ")";
            statement.execute(upit);
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dohvaća sve meteo podatke iz baze za neko parkiraliste
     * @param id id parkiralista
     * @param intervalOd od kojeg vremena želimo rezultate
     * @param intervalDo do kojeg vremena želimo rezultate
     * @return vraća listu meteo podataka na određeno parkiraliste
     */
    @WebMethod(operationName = "dajSveMeteoPodatke")
    public List<MeteoPodaci> dajSveMeteoPodatke(int id, String intervalOd, String intervalDo) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM meteo WHERE id="+id+" AND preuzeto BETWEEN '"+intervalOd+"' AND '"+ intervalDo+"'";
            ResultSet rez = statement.executeQuery(upit);
            List<MeteoPodaci> lista = new ArrayList<>();
            MeteoPodaci meteo = new MeteoPodaci();
            boolean dohvaceno = false;
            while(rez.next()){
                meteo = new MeteoPodaci(null,null,rez.getFloat("TEMP"),rez.getFloat("TEMPMIN"),rez.getFloat("TEMPMAX"),null,
                        rez.getFloat("VLAGA"),null,rez.getFloat("TLAK"),null,rez.getFloat("VJETAR"),null,rez.getFloat("VJETARSMJER"),
                        null, null, 0, rez.getString("VRIJEMEOPIS"),null, null, null, null, 0, rez.getString("VRIJEME"), null, null);
                lista.add(meteo);
                dohvaceno = true;
            }
            if(dohvaceno) 
                return lista;
            else 
                return null;
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dohvaća zadnje meteo podatke iz tablice meteo za određeno 
     * parkiraliste
     * @param id id parkiralista za koje je potrebno dohvatiti meteo podatke
     * @return vraća meteo podatke za određeno parkiraliste
     */
    @WebMethod(operationName = "dajZadnjeMeteoPodatke")
    public MeteoPodaci dajZadnjeMeteoPodatke(int id) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM meteo WHERE id ="+ id + " ORDER BY idmeteo DESC FETCH FIRST ROW ONLY";
            ResultSet rez = statement.executeQuery(upit);
            boolean dohvaceno = false;
            MeteoPodaci meteo = null;
            if(rez.next()){
                meteo = new MeteoPodaci(null,null,rez.getFloat("TEMP"),rez.getFloat("TEMPMIN"),rez.getFloat("TEMPMAX"),null,
                        rez.getFloat("VLAGA"),null,rez.getFloat("TLAK"),null,rez.getFloat("VJETAR"),null,rez.getFloat("VJETARSMJER"),
                        null, null, 0, rez.getString("VRIJEMEOPIS"),null, null, null, null, 0, rez.getString("VRIJEME"), null, null);
                dohvaceno = true;
            }
            if(dohvaceno)
                return meteo;
            else
                return null;
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dohvaća najnovije podatke sa api-a za određeno parkiraliste
     * @param id id parkiralista za koje treba vratiti meteo podatke
     * @return vraća meteo podatke za parkiraliste
     */
    @WebMethod(operationName = "dajVazeceMeteoPodatke")
    public MeteoPodaci dajVazeceMeteoPodatke(int id) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM parkiralista WHERE id="+id;
            ResultSet rezultat = statement.executeQuery(upit);
            MeteoPodaci mp = new MeteoPodaci();
            if(rezultat.next()){
                String latitude = rezultat.getString("LATITUDE");
                String longitude = rezultat.getString("LONGITUDE");
                OWMKlijent owmk = new OWMKlijent(SlusacAplikacije.getOwmkey());
                mp = owmk.getRealTimeWeather(latitude, longitude);
                return mp;
            }else
                return null;
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja fraća minimalnu i maksimalnu temperaturu za određeno 
     * parkiraliste u nekom intervalu
     * @param id id parkiralista
     * @param intervalOd vrijeme od kojeg želimo min i max temperaturu
     * @param intervalDo vrijeme do kojeg želimo min i max temperaturu
     * @return lista min i max temperatura u određenom intervalu
     */
    @WebMethod(operationName = "dajMinMaxTemp")
    public List<MeteoPodaci> dajMinMaxTemp(int id, String intervalOd, String intervalDo) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM meteo WHERE id="+id+" AND preuzeto BETWEEN '"+intervalOd+"' AND '"+ intervalDo+"'";
            ResultSet rezultat = statement.executeQuery(upit);
            List<MeteoPodaci> lista = new ArrayList<>();
            MeteoPodaci meteo = new MeteoPodaci();
            boolean dohvaceno = false;
            while(rezultat.next()){
                meteo.setTemperatureMin(rezultat.getFloat("TEMPMIN"));
                meteo.setTemperatureMax(rezultat.getFloat("TEMPMAX"));
                lista.add(meteo);
                dohvaceno = true;
            }
            if(dohvaceno) 
                return lista;
            else 
                return null;
        } catch (SQLException ex) {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    /**
     * Funkcija koja dohvaća podatke o parkiralistu iz baze podataka
     * @param id id parkiralista za koje nam trebaju podaci
     * @return vraća podatke o parkiralistu
     */
    @WebMethod(operationName = "dohvatiPodatkeParkiraliste")
    public Parkiraliste dohvatiPodatkeParkiraliste(int id) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM parkiralista WHERE id="+id;
            ResultSet rezultat = statement.executeQuery(upit);
            JsonArrayBuilder jab = Json.createArrayBuilder();
            Parkiraliste parking = new Parkiraliste();
            if(rezultat.next()){
                parking.setNaziv(rezultat.getString("NAZIV"));
                parking.setAdresa(rezultat.getString("ADRESA"));
                return parking;
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(MeteoREST.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
