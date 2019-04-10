package org.foi.nwtis.hcosic2.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.hcosic2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.NemaKonfiguracije;

/**
 * U klasi ServerSustava prvo se provjeravaju upisane opcije. Učitavaju se
 * postavke iz datoteke. Server kreira i pokreće dretvu za serijalizaciju
 * evidencije (klasa SerijalizatorEvidencije).
 * Otvara se ServerSocket koristeći konstruktor public. Zatim ServerSustava čeka
 * zahtjev korisnika u beskonačnoj petlji. Kada se korisnik spoji na otvorenu
 * vezu, provjerava raspoloživost radnih dretvi, kreira se objekt dretve klase RadnaDretva, veza
 * se predaje objektu i pokreće se izvršavanje dretve. Nakon toga server ponovno
 * čeka na uspostavljanje veze i postupak se nastavlja. Dretve opslužuju zahtjev
 * korisnika. Dretva nakon što obradi pridruženi zahtjev korisnika završava svoj
 * rad i briše se.  
 * @author Hrvoje
 */

public class ServerSustava {
    
    private static boolean pauza;
    private static boolean zaustavljeno;
    private static List<RadnaDretva> dretveSustava = new CopyOnWriteArrayList<RadnaDretva>();
    private static List<RadnaDretva> dretveCekaj = new ArrayList<RadnaDretva>();
    private static int maxBrojDretvi;
    private short brojDretve = 1;
    private OutputStream os;
    private static Evidencija evidencija = new Evidencija();
    
    public static void main(String[] args){
        
        if(args.length != 1){
            System.out.println("Krivi broj argumenata");
            return;
        }
        
        String datoteka = args[0];
        try {
            Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            ServerSustava ss = new ServerSustava();
            ss.pokreniPosluzitelj(konfig);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Metoda koja pokrece server i server se vrti u beskonacnoj petlji i prima
     * komande od korisnika
     * @param konfig konfiguracija sustava
     */
    private void pokreniPosluzitelj(Konfiguracija konfig) {
        this.pauza = false;
        int port = Integer.parseInt(konfig.dajPostavku("port"));
        int maxCekanje = Integer.parseInt(konfig.dajPostavku("maks.broj.zahtjeva.cekanje"));
        maxBrojDretvi = Integer.parseInt(konfig.dajPostavku("maks.broj.radnih.dretvi"));
        boolean krajRada = false;
        
        SerijalizatorEvidencije se = new SerijalizatorEvidencije("hcosic2 - Serijalizator", konfig);
        se.start();
        
        try {
            
            ServerSocket serverSocket = new ServerSocket(port, maxCekanje);
            while (!krajRada){
            Socket socket = serverSocket.accept();
            System.out.println("Korisnik se spojio");
            if(this.dretveSustava.size() != this.maxBrojDretvi){
                RadnaDretva rd = new RadnaDretva("hcosic2 - dretva - "+this.brojDretve, konfig, socket);
                this.dretveSustava.add(rd);
                rd.start();
            } else {
                String poruka = "ERROR 01; Nema raspolozive radne dretve sve dretve su zauzete";
                this.os = socket.getOutputStream();
                this.os.write(poruka.getBytes());
                this.os.flush();
                socket.shutdownOutput();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    /**
     * Metoda koja briše dretvu iz liste aktivnih dretvi
     * @param radnaDretva dretva koju treba izbrisati iz liste
     */
    public static void izbrisiDretvu(RadnaDretva radnaDretva) {
        if (dretveSustava.contains(radnaDretva)) {
            dretveSustava.remove(radnaDretva);
        }
    }
    /**
     * Metoda koja dodaje prosljeđenju dretvu u listu s dretvama koje su u 
     * cekanju
     * @param dretvaCekaj dretva koju treba dodati u listu dretvi koje su u
     * cekanju
     */
    public static void dodajDretvuCekaj(RadnaDretva dretvaCekaj){
        dretveCekaj.add(dretvaCekaj);
    }
    /**
     * Metoda koja dohvaća listu dretvi koje su u čekanju
     * @return vraća listu dretvi koje su u čekanju
     */
    public static List<RadnaDretva> dohvatiDretveCekanja(){
        return dretveCekaj;
    }
    /**
     * Metoda za pauziranje servera
     * @param pauza stanje servera koje želimo postaviti
     */
    public static void postaviPauzuServera(boolean pauza){
        ServerSustava.pauza = pauza;
    }
    /**
     * Metoda koja vraća stanje server odnosno jeli server u pauzi ili ne
     * @return je li server u pauzi ili ne
     */
    public static boolean stanjeServera(){
        return pauza;
    }
    /**
     * Metoda koja zaustavlja rad servera tako da server na prima nikakve
     * komande
     * @param zaustavljeno stanje servera koje želimo postaviti
     */
    public static void zaustaviServer(boolean zaustavljeno){
        ServerSustava.zaustavljeno = zaustavljeno;
    }
    /**
     * Metoda koja vraća je li server zaustavljen ili ne
     * @return vraća stanje servera
     */
    public static boolean stanjeServeraZaustavi(){
        return zaustavljeno;
    }
    /**
     * Metoda koja dohvaća objekt evidencija
     * @return vraća objekt evidencija
     */
    public static Evidencija dohvatiEvidenciju(){
        return evidencija;
    }
}
