
package org.foi.nwtis.hcosic2.web.zrna;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.foi.nwtis.hcosic2.ejb.eb.Dnevnik;
import org.foi.nwtis.hcosic2.ejb.eb.Meteo;
import org.foi.nwtis.hcosic2.ejb.eb.Parkiralista;
import org.foi.nwtis.hcosic2.ejb.sb.DnevnikFacade;
import org.foi.nwtis.hcosic2.ejb.sb.MeteoFacade;
import org.foi.nwtis.hcosic2.ejb.sb.ParkiralistaFacade;
import org.foi.nwtis.hcosic2.web.podaci.Izbornik;
import org.foi.nwtis.hcosic2.ejb.sb.MeteoKlijentZrno;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.web.podaci.Lokacija;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPodaci;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPrognoza;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Klasa Pregled koja služi za dodavanje parkirališta u bazu podataka, ažuriranje
 * zapisa u bazi podataka, preuzimanje i vraćanje parkirališta iz izbornika te
 * dohvaćanje meteo prognoze od 5 dana za odabrano parkiralište.
 * @author Hrvoje
 */

@Named(value = "pregled")
@SessionScoped
public class Pregled implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    @EJB
    private MeteoKlijentZrno meteoKlijentZrno;

    @EJB
    private ParkiralistaFacade parkiralistaFacade;

    @Inject
    private HttpServletRequest httpRequest;

    private Integer id;
    private String naziv;
    private String adresa;
    private List<Izbornik> popisParking = new ArrayList<>();
    private List<String> popisParkingOdabrano = new ArrayList<>();
    private List<Izbornik> popisParkingMeteo = new ArrayList<>();
    private List<String> popisParkingMeteoOdabrana = new ArrayList<>();
    private List<MeteoPrognoza> popisMeteoPodaci = new ArrayList<>();
    private Parkiralista parking;
    private String poruka;
    private String prognozaGumb = "Prognoze";
    private boolean upisi = false;
    private long pocetak;
    private boolean meteo = false;
    ExternalContext kontekst = FacesContext.getCurrentInstance().getExternalContext();

    public Pregled() {
        SlusacAplikacije.dohvatiPodatke();
    }

    /**
     * Funkcija koja sortira zapise u izbornicima prema abeceti počevši od prvog
     * slova apecede
     * @param popisIzbornika lista izbornika koju je potrebno sortirati
     */
    public void sortiranje(List<Izbornik> popisIzbornika) {
        Collator collator = Collator.getInstance(new Locale("hr", "HR"));
        Collections.sort(popisIzbornika, new Comparator<Izbornik>() {
            @Override
            public int compare(Izbornik popisIzbornika1, Izbornik popisIzbornika2) {
                return collator.compare(popisIzbornika1.getLabela(), popisIzbornika2.getLabela());
            }
        });
    }

    /**
     * Funkcija koja komunicira s klasom DnevnikFacade te dodaje nove zapise 
     * u shemu baze odnosno bazu podataka
     * @param trajanje trajanje izvršene akcije
     * @param status status izvršene akcije
     */
    public void zapisDnevnik(long trajanje, int status) {
        try {
            Dnevnik dnevnik = new Dnevnik("hcosic2", httpRequest.getRequestURL().toString(),
                    InetAddress.getLocalHost().getHostAddress(), new Date(), (int) (trajanje / 1000000), status);
            dnevnikFacade.create(dnevnik);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Pregled.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Funkcija koja dodaje novo parkiraliste u bazu podataka
     * @return 
     */
    public String dodajParkiraliste() {
        poruka = "";
        pocetak = System.nanoTime();
        boolean postoji = false;
        for (Parkiralista p2 : parkiralistaFacade.findAll()) {
            if (p2.getNaziv().equals(naziv) && p2.getAdresa().equals(adresa)) {
                zapisDnevnik(System.nanoTime() - pocetak, 0);
                postoji = true;
            }
        }
        if (!postoji) {
            if (id != null && !naziv.trim().isEmpty() && !adresa.trim().isEmpty()) {
                kreirajNovoParkiraliste();
                zapisDnevnik(System.nanoTime() - pocetak, 1);
            } else {
                poruka = "Morate popuniti sva polja za unos podataka!";
                zapisDnevnik(System.nanoTime() - pocetak, 0);
            }
        } else {
            poruka = "Navedeno parkiralište već postoji u bazi!";
        }
        return "";
    }

    /**
     * Funkcija koja kreira novo parkiraliste
     */
    public void kreirajNovoParkiraliste() {
        Parkiralista p = new Parkiralista();
        Lokacija lokacija = new Lokacija();
        meteoKlijentZrno.postaviKorisnickePodatke(SlusacAplikacije.getApiKey(), SlusacAplikacije.getGmapiKey());
        lokacija = meteoKlijentZrno.dajLokaciju(adresa);
        p.setId(id);
        p.setNaziv(naziv);
        p.setAdresa(adresa);
        p.setLatitude(Float.parseFloat(lokacija.getLatitude()));
        p.setLongitude(Float.parseFloat(lokacija.getLongitude()));
        parkiralistaFacade.create(p);
        Izbornik i = new Izbornik(p.getNaziv(), p.getId().toString());
        popisParking.add(i);
        poruka = "";
        upisi = false;
        id = null;
        naziv = "";
        adresa = "";
    }

    /**
     * Funkcija koja izvršava naredbu upiši parkiralište ona zapravo ažurira
     * podatke u bazi podataka za odabrano parkiralište
     * @return 
     */
    public String upisiParkiraliste() {
        poruka = "";
        pocetak = System.nanoTime();
        boolean azurirano = false;
        for (Izbornik i : popisParking) {
            if (id != null && i.getVrijednost().equals(id.toString()) && !naziv.trim().isEmpty() && !adresa.trim().isEmpty()) {
                azurirano = true;
                Lokacija lokacija = new Lokacija();
                meteoKlijentZrno.postaviKorisnickePodatke(SlusacAplikacije.getApiKey(), SlusacAplikacije.getGmapiKey());
                lokacija = meteoKlijentZrno.dajLokaciju(adresa);
                Parkiralista p = new Parkiralista(id, naziv, adresa, Float.parseFloat(lokacija.getLatitude()), Float.parseFloat(lokacija.getLongitude()));
                parkiralistaFacade.edit(p);
                upisi = false;
                poruka = "";
                id = null;
                naziv = "";
                adresa = "";
                zapisDnevnik(System.nanoTime() - pocetak, 1);
            } else if (!azurirano) {
                poruka = "Navedeno parkiralište ne postoji u bazi ili niste popunili sva polja za unos podataka!";
                zapisDnevnik(System.nanoTime() - pocetak, 0);
            }
        }
        return "";
    }

    /**
     * Funkcija koja izvršava naredbu azuriraj parkiralište ona podatke o odabranom
     * parkiralištu upisuje u "inputText" na formi odnosno frontendu
     * @return 
     */
    public String azurirajParkiraliste() {
        poruka = "";
        pocetak = System.nanoTime();
        if (popisParking.size() > 0) {
            upisi = true;
            for (Izbornik i : popisParking) {
                if (popisParkingOdabrano.contains(i.getVrijednost())) {
                    id = Integer.parseInt(i.getVrijednost());
                    parking = parkiralistaFacade.find(id);
                    naziv = parking.getNaziv();
                    adresa = parking.getAdresa();
                    poruka = "";
                    zapisDnevnik(System.nanoTime() - pocetak, 1);
                }
            }
        } else {
            poruka = "Trebate preuzeti i odabrati parkiralište za ažuriranje!";
            zapisDnevnik(System.nanoTime() - pocetak, 0);
        }
        return "";
    }

    /**
     * Funkcija koja izvršava naredbu preuzmi odnosno prebacuje parkiralište
     * iz glavnog izbornika u izbornik odabranih parkirališta
     * @return 
     */
    public String preuzmiParkiralista() {
        poruka = "";
        pocetak = System.nanoTime();
        for (Izbornik i : popisParking) {
            if (popisParkingOdabrano.contains(i.getVrijednost())
                    && !popisParkingMeteo.contains(i)) {
                popisParkingMeteo.add(i);
            }
        }
        for (Izbornik i : popisParkingMeteo) {
            if (popisParking.contains(i)) {
                popisParking.remove(i);
            }
        }
        upisi = false;
        id = null;
        naziv = "";
        adresa = "";
        sortiranje(popisParking);
        sortiranje(popisParkingMeteo);
        zapisDnevnik(System.nanoTime() - pocetak, 1);
        return "";
    }

    /**
     * Funkcija koja izvršava naredbu vrati vraža parkiralište iz izbornika
     * odabrana parkirališta u izbornik svih parkirališta
     * @return 
     */
    public String vratiParkiralista() {
        poruka = "";
        pocetak = System.nanoTime();
        for (Izbornik i : popisParkingMeteo) {
            if (popisParkingMeteoOdabrana.contains(i.getVrijednost())
                    && !popisParking.contains(i)) {
                popisParking.add(i);
            }
        }
        for (Izbornik i : popisParking) {
            if (popisParkingMeteo.contains(i)) {
                popisParkingMeteo.remove(i);
            }
        }
        upisi = false;
        id = null;
        naziv = "";
        adresa = "";
        zapisDnevnik(System.nanoTime() - pocetak, 1);
        return "";
    }

    /**
     * Funkcijaj koja preuzima 5-dnevnu meteorološku prognozu za odabrano
     * parkiralište
     * @return 
     */
    public String preuzmiMeteoPodatke() {
        poruka = "";
        meteo = true;
        pocetak = System.nanoTime();
        if (prognozaGumb == "Prognoze") {
            meteoKlijentZrno.postaviKorisnickePodatke(SlusacAplikacije.getApiKey(), SlusacAplikacije.getGmapiKey());
            for (Izbornik i : popisParkingMeteo) {
                if (popisParkingMeteoOdabrana.contains(i.getVrijednost())) {
                    dohvatiMeteoPodatke(i);
                }
            }
        } else {
            popisMeteoPodaci.clear();
            prognozaGumb = "Prognoze";
            zapisDnevnik(System.nanoTime() - pocetak, 1);
            meteo = false;
        }
        upisi = false;
        return "";
    }
    
    /**
     * Funkcija koja dohvaća Meteo podatke za odabrano parkiralište iz izbornika
     * @param i izbornik odabranih parkirališta
     */
    public void dohvatiMeteoPodatke(Izbornik i) {
        id = Integer.parseInt(i.getVrijednost());
        parking = parkiralistaFacade.find(id);
        naziv = parking.getNaziv();
        adresa = parking.getAdresa();
        MeteoPrognoza[] mp = meteoKlijentZrno.dajMeteoPrognoze(id, adresa);
        for (MeteoPrognoza mprognoza : mp) {
            popisMeteoPodaci.add(mprognoza);
        }
        prognozaGumb = "Zatvori prognoze";
        zapisDnevnik(System.nanoTime() - pocetak, 1);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
    
    /**
     * Funkcija ili "geter" koja ujedno dohvaća sva parkirališta iz baze podataka
     * @return 
     */
    public List<Izbornik> getPopisParking() {
        popisParking.clear();
        for (Parkiralista p : parkiralistaFacade.findAll()) {
            Izbornik i = new Izbornik(p.getNaziv(),
                    Integer.toString(p.getId()));
            if (!popisParking.contains(i) && !popisParkingMeteo.contains(i)) {
                popisParking.add(i);
            }
        }
        sortiranje(popisParking);
        return popisParking;
    }

    public void setPopisParking(List<Izbornik> popisParking) {
        this.popisParking = popisParking;
    }

    public List<String> getPopisParkingOdabrano() {
        return popisParkingOdabrano;
    }

    public void setPopisParkingOdabrano(List<String> popisParkingOdabrano) {
        this.popisParkingOdabrano = popisParkingOdabrano;
    }

    public List<Izbornik> getPopisParkingMeto() {
        return popisParkingMeteo;
    }

    public void setPopisParkingMeto(List<Izbornik> popisParkingMeto) {
        this.popisParkingMeteo = popisParkingMeto;
    }

    public List<String> getPopisParkingMeteoOdabrana() {
        return popisParkingMeteoOdabrana;
    }

    public void setPopisParkingMeteoOdabrana(List<String> popisParkingMeteoOdabrana) {
        this.popisParkingMeteoOdabrana = popisParkingMeteoOdabrana;
    }

    public List<MeteoPrognoza> getPopisMeteoPodaci() {
        return popisMeteoPodaci;
    }

    public void setPopisMeteoPodaci(List<MeteoPrognoza> popisMeteoPodaci) {
        this.popisMeteoPodaci = popisMeteoPodaci;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public String getPrognozaGumb() {
        return prognozaGumb;
    }

    public void setPrognozaGumb(String prognozaGumb) {
        this.prognozaGumb = prognozaGumb;
    }

    public boolean isUpisi() {
        return upisi;
    }

    public void setUpisi(boolean upisi) {
        this.upisi = upisi;
    }

    public boolean isMeteo() {
        return meteo;
    }

    public void setMeteo(boolean meteo) {
        this.meteo = meteo;
    }

}
