package org.foi.nwtis.hcosic2.zadaca_1;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.hcosic2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.NemaKonfiguracije;

/**
 * Klasa u kojoj se provjeravaju upisane opcije, preporučuje se koristiti dopuštene izraze.
 * Na temelju opcije kreira se objekt potrebne klase AdministratorSustava ili
 * KlijentSustava, te se nastavlja s izvršavanjem tog objekta.
 * @author Hrvoje
 */

public class KorisnikSustava {
    
    boolean administrator = false;
    private static String podaci;

    public static void main(String[] args) {
        
        KorisnikSustava ks = new KorisnikSustava();
        ks.preuzmiPostavke(args);
        
        if(ks.administrator){
            AdministratorSustava as = new AdministratorSustava(null, podaci);
            as.preuzmiPostavke(args);
            as.preuzmiKontrolu();
        } else {
            KlijentSustava kls = new KlijentSustava(podaci);
            kls.preuzmiPostavke(args);
            kls.preuzmiKontrolu();
        }
    }
    /**
     * Metoda za prepoznavanje prijavljenog korisnika odnosno je li sustavu
     * pristupio administrator ili klijent
     * @param args podaci koji se učitavaju prilikom pokretanja programa
     */
    protected void preuzmiPostavke(String[] args) {
        
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            stringBuilder.append(args[i]).append(" ");
        }
        podaci = stringBuilder.toString().trim();
        if(podaci.startsWith("-k")){
            administrator = true;
        }
        else if(podaci.startsWith("-s")){
            administrator = false;
        }
        else{
            System.out.println("Krivo unešeni podaci");
            System.exit(1);
        }   
    }
    
}
