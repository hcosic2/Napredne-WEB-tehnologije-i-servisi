package org.foi.nwtis.hcosic2.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;

/**
 * Klasa ServerSustava koristi se za:
 * Objekt klase spaja se na server i šalje komandu(e) u zahtjevu putem mrežne
 * utičnice/socket-a. Primljeni odgovori se ispisuju na ekranu korisnika. 
 * Za svaku vrstu opcija kreira se posebna metoda koja odrađuje njenu 
 * funkcionalnost.
 * @author Hrvoje
 */

public class AdministratorSustava extends KorisnikSustava {

    private String korisnik;
    private String lozinka;
    private String adresa;
    private String komanda;
    private int port;
    private Konfiguracija konfig;
    private Matcher matcher;
    private String podaci;
    private String akcija;

    public AdministratorSustava(Konfiguracija konfig, String podaci) {
        super();
        this.konfig = konfig;
        this.podaci = podaci;
    }

    /**
     * Metoda koja preuzima kontrolu nad sustavom odnosno kreira objekt socket i
     * objekt tipa administrator
     */
    public void preuzmiKontrolu() {
        try {
            if (!provjeriPodatkeDatoteka(this.podaci)) {
                if(!provjeriPodatke(this.podaci)){
                    System.out.println("ERROR; Krivo unešeni podaci");
                    System.exit(1);
                }
            }
            ucitajPodatke();
            Socket socket = new Socket(adresa, port);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            os.write(akcija.getBytes());
            os.flush();
            socket.shutdownOutput();

            StringBuffer sb = new StringBuffer();
            while (true) {
                int bajt = is.read();
                if (bajt == -1) {
                    break;
                }
                sb.append((char) bajt);
            }

            System.out.println("Odgovori: " + sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za provjeru primljenih podataka
     *
     * @param podaci koji su prosljeđeni programu
     * @return valjanost podataka
     */
    private boolean provjeriPodatke(String podaci) {
        String regex = "^-k ([^\\s]+) -l ([^\\s]+) -s ([^\\s]+) -p ([^\\s]+) --(.*)";
        Pattern uzorak = Pattern.compile(regex);
        matcher = uzorak.matcher(podaci);
        return matcher.matches();

    }
    /**
     * Metoda koja provjerava primljene podatke za komande IOT i EVIDENCIJA
     * @param podaci parametri koji su unešeni kod pokretanja programa
     * @return 
     */
    private boolean provjeriPodatkeDatoteka(String podaci){
        String regex = "^-k ([^\\s]+) -l ([^\\s]+) -s ([^\\s]+) -p ([^\\s]+) --(iot|evidencija) (.*)";
        Pattern uzorak = Pattern.compile(regex);
        matcher = uzorak.matcher(podaci);
        return matcher.matches();
        
    }
/**
 * Metoda za spremanje zaprimljenih podataka u varijable
 */
private void ucitajPodatke() {
        this.korisnik = this.matcher.group(1);
        this.lozinka = this.matcher.group(2);
        this.adresa = this.matcher.group(3);
        this.port = Integer.parseInt(this.matcher.group(4));
        this.komanda = this.matcher.group(5);
        
        akcija = "KORISNIK "+korisnik+"; LOZINKA "+lozinka+"; "+ komanda.toUpperCase()+";";
    }
    
}
