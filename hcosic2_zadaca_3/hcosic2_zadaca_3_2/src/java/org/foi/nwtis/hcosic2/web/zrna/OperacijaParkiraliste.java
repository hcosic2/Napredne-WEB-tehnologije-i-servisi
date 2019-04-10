package org.foi.nwtis.hcosic2.web.zrna;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.foi.nwtis.hcosic2.rest.klijenti.MeteoRESTKlijent;
import org.foi.nwtis.hcosic2.rest.klijenti.MeteoRESTKlijentId;
import org.foi.nwtis.hcosic2.ws.klijenti.MeteoWSKlijent;
import org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS;
import org.foi.nwtis.hcosic2.ws.serveri.Parkiraliste;
import org.foi.nwtis.hcosic2.ws.serveri.MeteoPodaci;

/**
 * Korisnički dio u kojem se mogu obavljati operacije: unositi pojedinačna
 * parkirališta za koje će se preuzimati metorološki podaci, preuzeti parkirališta
 * za koje se prikupljaju meteorološki podaci u prvom projektu te se prikazuju
 * u obliku padajućeg izbornika s mogućim odabirom više elemenata. 
 * 
 * @author Hrvoje
 */

@Named(value = "operacijaParkiraliste")
@RequestScoped
public class OperacijaParkiraliste {

    private String naziv;
    private String adresa;
    private List<Parkiraliste> parkiralista;
    private List<Integer> odabranaParkiralista;
    private List<MeteoPodaci> meteo;
    private List<Parkiraliste> listaParkiralista = new ArrayList<>();
    private List<MeteoPodaci> listaMeteoPodataka = new ArrayList<>();
    private int id;
    private String poruka;

    public OperacijaParkiraliste() {
        prikaziParkiralista();
    }

    /**
     * Funkcija koja šalje zahtjev SOAP servisu za upis parkiralista u bazu
     * @return 
     */
    public String upisiSOAP() {
        if (naziv.equals("") || adresa.equals("")) {
            poruka = "Morate upisati naziv i adresu";
        } else {
            Parkiraliste parkiraliste = new Parkiraliste();
            System.out.println(naziv);
            parkiraliste.setNaziv(naziv);
            parkiraliste.setAdresa(adresa);
            MeteoWSKlijent.dodajParkiraliste(parkiraliste);
        }
        return "";
    }

    /**
     * Funkcija koja šalje zahtjev SOAP servisu za upis parkiralista u bazu
     * @return 
     */
    public String upisiREST() {
        if (naziv.equals("") || adresa.equals("")) {
            poruka = "Morate upisati naziv i adresu";
        } else {
            MeteoRESTKlijent mrsk = new MeteoRESTKlijent();
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("naziv", naziv);
            job.add("adresa", adresa);
            String zahtjev = job.build().toString();
            String odgovor = mrsk.postJson(zahtjev, String.class);
            System.out.println(odgovor);
        }
        return "";
    }

    /**
     * Funkcija koja šalje zahtjev SOAP servisu za dohvaćanje svih parkirališta
     * iz baze podataka
     */
    public void prikaziParkiralista() {
        listaParkiralista = MeteoWSKlijent.dajSvaParkiralista();
    }

    /**
     * Funkcija koja šalje zahtjev REST servisu za podatke o parkiralištu iz baze
     * podataka koji se dohvaćaju pomoću prosljeđenog id-a
     */
    public void preuzmiREST() {
        if (odabranaParkiralista.isEmpty()) {
            poruka = "Morate odabrati parkiraliste u izborniku";
        } else {
            MeteoRESTKlijentId mrskId = new MeteoRESTKlijentId(odabranaParkiralista.get(0).toString());
            String odgovor = mrskId.getJson(String.class);
            JsonReader reader = Json.createReader(new StringReader(odgovor));
            JsonObject jo = reader.readObject();
            String json2 = jo.getString("odgovor");
            JsonReader reader2 = Json.createReader(new StringReader(json2));
            JsonObject jo2 = reader2.readObject();
            naziv = jo2.getString("naziv");
            adresa = jo2.getString("adresa");
            System.out.println(odgovor);
        }
    }

    /**
     * Funkcija koja šalje zahtjev REST servisu za ažuriranje podataka za određeno
     * parkiralište u bazi podataka šalje id parkirališta koje želi update-ati
     * i podake koje želi upisati
     */
    public void azurirajREST() {
        if (odabranaParkiralista.isEmpty()) {
            poruka = "Morate odabrati parkiraliste u izborniku";
        } else {
            MeteoRESTKlijentId mrskId = new MeteoRESTKlijentId(odabranaParkiralista.get(0).toString());
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("naziv", naziv);
            job.add("adresa", adresa);
            String odgovor = mrskId.putJson(job.build().toString(), String.class);
            System.out.println(odgovor);
        }
    }

    /**
     * Funkcija koja šalje zahtjev REST servisu za brisanje podataka iz baze za
     * određeno parkiralište šalje id parkirališta koje želi obrisati
     */
    public void brisiREST() {
        if (odabranaParkiralista.isEmpty()) {
            poruka = "Morate odabrati parkiraliste u izborniku";
        } else {
            MeteoRESTKlijentId mrskId = new MeteoRESTKlijentId(odabranaParkiralista.get(0).toString());
            String odgovor = mrskId.deleteJson(String.class);
            System.out.println(odgovor);
            prikaziParkiralista();
        }
    }

    /**
     * Funkcija koja šalje zahtjev SOAP servisu za dohvaćanje svih meteo podataka
     * za određeno parkiralište u određenom intervalu šalje id parkirališta i interval
     * prema uputi profesora interval je fiksiran jer na frontendu nije predivđeno
     * upisivanje intervala
     */
    public void preuzmiMeteo() {
        if (odabranaParkiralista.isEmpty()) {
            poruka = "Morate odabrati parkiraliste u izborniku";
        } else {
            listaMeteoPodataka = MeteoWSKlijent.dajSveMeteoPodatke(odabranaParkiralista.get(0),
                    "2018-05-07 21:33:56.288", "2018-05-07 21:35:49.499");
            id = odabranaParkiralista.get(0);
        }
    }

    /**
     * Funkcija koja šalje zahtjev SOAP servisu za preuzimanjem podataka o 
     * parkiralištu iz baze podatka šalje id parkirališa za koje su potrebni podaci
     */
    public void preuzmiSOAP() {
        if (odabranaParkiralista.isEmpty()) {
            poruka = "Morate odabrati parkiraliste u izborniku";
        } else {
            Parkiraliste parkiraliste = new Parkiraliste();
            parkiraliste = MeteoWSKlijent.dohvatiPodatkeParkiraliste(odabranaParkiralista.get(0));
            System.out.println(odabranaParkiralista.get(0));
            System.out.println("parkiraliste.getNaziv()");
            naziv = parkiraliste.getNaziv();
            adresa = parkiraliste.getAdresa();
        }
    }

    public List<Parkiraliste> getLista() {
        return listaParkiralista;
    }

    public void setLista(List<Parkiraliste> lista) {
        this.listaParkiralista = lista;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public List<Parkiraliste> getParkiralista() {
        return parkiralista;
    }

    public void setParkiralista(List<Parkiraliste> parkiralista) {
        this.parkiralista = parkiralista;
    }

    public List<Integer> getOdabranaParkiralista() {
        return odabranaParkiralista;
    }

    public void setOdabranaParkiralista(List<Integer> odabranaParkiralista) {
        this.odabranaParkiralista = odabranaParkiralista;
    }

    public List<MeteoPodaci> getMeteo() {
        return meteo;
    }

    public void setMeteo(List<MeteoPodaci> meteo) {
        this.meteo = meteo;
    }

    public List<MeteoPodaci> getListaMeteoPodataka() {
        return listaMeteoPodataka;
    }

    public void setListaMeteoPodataka(List<MeteoPodaci> listaMeteoPodataka) {
        this.listaMeteoPodataka = listaMeteoPodataka;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

}
