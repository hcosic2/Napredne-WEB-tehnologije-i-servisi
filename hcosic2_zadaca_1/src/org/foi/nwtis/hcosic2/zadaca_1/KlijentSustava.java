package org.foi.nwtis.hcosic2.zadaca_1;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.hcosic2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.NemaKonfiguracije;

/**
 * Klasa koja služi za provjeru upisane opcije, preporučuje se koristiti dopuštene izraze.
 * Spaja se na server i nakon toga šalje komandu serveru putem mrežne
 * utičnice/socket-a i traži izvršavanja određene akcije. Primljeni odgovor
 * se ispisuju na ekranu korisnika.
 * @author Hrvoje
 */

public class KlijentSustava extends KorisnikSustava {
    
    private String adresa;
    private int port;
    private int spavanje;
    private Matcher matcher;
    private String podaci;
    private String komanda;
    private String file;
    IOT iot;

    public KlijentSustava(String podaci) {
        this.podaci = podaci;
    }
    /**
     * Metoda koja preuzima kontrolu nad sustavom odnosno kreira objekt socket
     * i objekt tipa administrator
     */
    public void preuzmiKontrolu(){
        try {
            if(!provjeriPodatkeSpavanje(this.podaci)){
                if(!provjeriPodatkeIot(this.podaci)){
                    System.out.println("ERROR; Krivo unešeni podaci");
                    System.exit(1);
                } else{
                    ucitajPodatke(false);
                }
            } else{
                ucitajPodatke(true);
            }
            Socket socket = new Socket(adresa, port);
            //System.out.println(komanda);
            
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            
            os.write((komanda + ";").getBytes());
            os.flush();
            socket.shutdownOutput();
            
            StringBuffer sb = new StringBuffer();
            while (true){
                int bajt = is.read();
                if(bajt == -1){
                    break;
                }
                sb.append((char) bajt);
            }
            
            System.out.println("Odgovor: "+sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Metoda za provjeru primljenih podataka za komandu klijenga CEKAJ n
     * @param podaci koji su prosljeđeni programu odnosno uneseni parametri
     * @return valjanost podataka
     */
    private boolean provjeriPodatkeSpavanje(String podaci){
        String regexSpavanje = "-s ([^\\s]+) -p ([^\\s]+) --spavanje ([0-9]+)";
        Pattern uzorak = Pattern.compile(regexSpavanje);
        matcher = uzorak.matcher(podaci);
        if(matcher.matches()){
            return matcher.matches();
        }
        return false;
    }
    /**
     * Metoda za provjeru primljenih podataka za komandu klijenga IOT datoteka
     * @param podaci koji su prosljeđeni programu odnosno uneseni parametri
     * @return 
     */
    private boolean provjeriPodatkeIot(String podaci){
        String regexIot = "-s ([^\\s]+) -p ([^\\s]+) ([a-zA-Z]+.txt)";
        Pattern uzorak = Pattern.compile(regexIot);
        matcher = uzorak.matcher(podaci);
        if(matcher.matches()){
            return matcher.matches();
        }
        return false;
    }
    /**
     * Metoda za spremanje zaprimljenih podataka u varijable te kreiranje
     * komande za klijenta
     */
    private void ucitajPodatke(boolean akcija) {
        if(akcija){
            this.adresa = this.matcher.group(1);
            this.port = Integer.parseInt(this.matcher.group(2));
            this.spavanje = Integer.parseInt(this.matcher.group(3));
            komanda = "CEKAJ "+spavanje;
        } else{
            komanda = "IOT";
            List<String> lista = new ArrayList<String>();
            this.adresa = this.matcher.group(1);
            this.port = Integer.parseInt(this.matcher.group(2));
            this.file = this.matcher.group(3);
            lista = procitajIotDatoteku(this.matcher.group(3).toString());
            String json = "";
                Gson gson = new Gson();
                json += gson.toJson(iot);
            komanda = komanda +" "+ json;
        }
    }
    /**
     * Metoda koja čita iz datoteke iot.txt
     * @param datoteka ime datoteke
     * @return vraća listu podataka koji su zapisani u datoteci
     */
    private List<String> procitajIotDatoteku(String datoteka){
        iot = new IOT();
        List<String> lista = new ArrayList<String>();
        try {
                Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
                Properties p = konf.dajSvePostavke();
                for (Object k : p.keySet()) {
                    if(k.toString().contains("id")){
                        String v = konf.dajPostavku((String) k);
                        iot.id= Integer.parseInt(v);
                    } else {
                        String v = konf.dajPostavku((String) k);
                        iot.lista.add(k.toString()+" "+v);
                    }
                    //lista.add(k.toString()+v);
                }
            } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
                Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        return lista;
    }
    
}
