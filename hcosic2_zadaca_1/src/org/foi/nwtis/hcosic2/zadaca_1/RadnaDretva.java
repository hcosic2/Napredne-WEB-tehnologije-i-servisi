package org.foi.nwtis.hcosic2.zadaca_1;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;

/**
 * U klasi RadnaDretva kreira se konstruktor klase u koji se prenose podaci konfiguracije i metode
 * za prijenos potrebnih podataka. Dretva iz dobivene veze na mrežnoj
 * utičnici/socket-u preuzima tokove za ulazne i izlazne podatke prema korisniku.
 * Dretva preuzima podatke koje šalje korisnik putem ulaznog toka podataka,
 * provjerava korektnost komandi iz zahtjeva. Na kraju dretva šalje podatke korisniku putem izlaznog toka podataka.
 * Za svaku vrstu komande kreira se posebna metoda koja odrađuje njenu funkcionalnost.
 * @author Hrvoje
 */

class RadnaDretva extends Thread {

    public final String nazivDretve;
    private final Konfiguracija konfig;
    private final Socket socket;
    private String komanda;
    private Matcher matcher;
    private Matcher matcher2;
    private String regexAdmin;
    private String regexKlijentSpavanje;
    private OutputStream os;
    private InputStream is;
    private Map<String, String> admin;
    IOT iot;
    List<IOT> listaIota = new ArrayList<IOT>();
    private List<RadnaDretva> listaCekanja = new ArrayList<RadnaDretva>();

    public RadnaDretva(String nazivDretve, Konfiguracija konfig, Socket socket) {
        super(nazivDretve);
        this.socket = socket;
        this.konfig = konfig;
        this.nazivDretve = nazivDretve;
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public synchronized void run() {
        try {
            regexAdmin = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (PAUZA|KRENI|ZAUSTAVI|STANJE|EVIDENCIJA|IOT);$";
            regexKlijentSpavanje = "^CEKAJ ([^\\s]+);$";
            this.is = this.socket.getInputStream();
            this.os = this.socket.getOutputStream();

            StringBuffer sb = new StringBuffer();
            while (true) {
                int bajt = is.read();
                if (bajt == -1) {
                    break;
                }
                sb.append((char) bajt);
            }
            System.out.println("Dretva: " + nazivDretve + "\nKomanda: " + sb);

            Pattern uzorak = Pattern.compile(regexAdmin);
            matcher = uzorak.matcher(sb);
            if (!ServerSustava.stanjeServeraZaustavi()) {
                if (matcher.matches()) {
                    if (validacijaKorisnika(matcher.group(1), matcher.group(2))) {
                        switch (matcher.group(3)) {
                            case "PAUZA":
                                pisiKorisniku(adminPauza());
                                break;
                            case "KRENI":
                                pisiKorisniku(adminKreni());
                                break;
                            case "STANJE":
                                pisiKorisniku(adminStanje());
                                break;
                            case "ZAUSTAVI":
                                pisiKorisniku(adminZaustavi());
                                break;
                            case "IOT":
                                pisiKorisniku(adminIot());
                                break;
                            case "EVIDENCIJA":
                                pisiKorisniku(adminEvidencija());
                                break;
                        }
                    } else {
                        pisiKorisniku("ERROR10; Korisnik nije administrator ili lozinka ne odgovara");
                    }
                } else {
                    if (ServerSustava.stanjeServera()) {
                        pisiKorisniku("Server je u stanju PAUZA");
                    } else {
                        Pattern uzorak2 = Pattern.compile(regexKlijentSpavanje);
                        matcher = uzorak2.matcher(sb);
                        if (matcher.matches()) {
                            ServerSustava.dodajDretvuCekaj(this);
                            try {
                                Long cekanje = Long.parseLong(matcher.group(1));
                                this.sleep((long) cekanje * 1000);
                                pisiKorisniku("OK");
                            } catch (InterruptedException ex) {
                                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
                                pisiKorisniku("ERROR 22; Dretva je prekinuta nije uspjela odraditi cekanje");
                            }
                        } else {
                            String regexIot = "IOT (.*);";
                            if (sb.toString().contains("IOT")) {
                                Pattern pattern = Pattern.compile(regexIot);
                                matcher = pattern.matcher(sb.toString());
                                String json = "";
                                if (matcher.matches()) {
                                    json = matcher.group(1);
                                }
                                iot = new IOT();
                                Gson gson = new Gson();
                                iot = gson.fromJson(json, IOT.class);
                                listaIota.add(iot);
                                boolean idPostoji = false;
                                for (int i = 0; i < listaIota.size(); i++) {
                                    if (listaIota.get(i).id == iot.id) {
                                        listaIota.set(i, iot);
                                        pisiKorisniku("OK 21;");
                                        idPostoji = true;
                                    }
                                }
                                if (!idPostoji) {
                                    listaIota.add(iot);
                                    pisiKorisniku("OK 20;");
                                }

                                //TODOO logika za iot naredbu
                            } else {
                                pisiKorisniku("ERROR 02; Komanda ne postoji");
                            }
                        }
                    }
                }
                socket.shutdownOutput();
            } else {
                pisiKorisniku("Server je zatvoren");
            }
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        zatvoriDretvu();
    }

    @Override
    public synchronized void start() {
        super.start();
    }
    /**
     * Metoda koja služi za zatvaranje dretve odnosno brisanje dretve iz liste
     * svih dretvi
     */
    private void zatvoriDretvu() {
        try {
            if (this.is != null) {
                this.is.close();
            }

            if (this.os != null) {
                this.os.close();
            }

            this.socket.close();
            ServerSustava.izbrisiDretvu(this);
        } catch (IOException ex) {
            System.out.println("ERROR; greška");
            System.exit(1);
        }
    }

    /**
     * Metoda koja upravlja komandom "PAUZA"
     *
     * @return Vraća određenu poruku zavisno o slučaju
     */
    public String adminPauza() {
        ServerSustava.dohvatiEvidenciju().dodajUkupanBrojZahtjeva();
        if (ServerSustava.stanjeServera()) {
            return ("ERROR11; Server je vec u stanju PAUZA");
        } else {
            ServerSustava.postaviPauzuServera(true);
            return ("OK");
        }
    }

    /**
     * Metoda za slanje odgovora korisniku
     *
     * @param poruka poruka koju želimo poslati korisniku
     */
    private void pisiKorisniku(String poruka) {
        try {
            this.os = this.socket.getOutputStream();
            this.os.write(poruka.getBytes());
            this.os.flush();
        } catch (IOException ex) {
            System.out.println("ERROR 01; Pogreška kod slanja odgovora korisniku");
            System.exit(1);
        }
    }

    /**
     * Metoda koja provjerava korisničke podatke
     *
     * @param korIme korisničko ime
     * @param korLozinka lozinka korisnika
     * @return vraća uspješnost provjere
     */
    private boolean validacijaKorisnika(String korIme, String korLozinka) {
        Properties props = konfig.dajSvePostavke();
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            String linija = (String) e.nextElement();
            String lozinka = props.getProperty(linija);
            if (linija.contains(korIme)) {
                return korLozinka.equals(lozinka);
            }
        }
        return false;
    }

    /**
     * Metoda koja upravlja administratorskom komandom KRENI
     *
     * @return vraća poruku koju je potrebno poslati korisniku
     */
    private String adminKreni() {
        if (ServerSustava.stanjeServera()) {
            ServerSustava.postaviPauzuServera(false);
            return "OK";
        } else {
            return "ERROR 12; Server nije u stanju pauze";
        }
    }

    /**
     * Metoda koja upravlja administratorskom komandom STANJE
     *
     * @return vraća poruku koju treba proslijediti administratoru
     */
    private String adminStanje() {
        if (ServerSustava.stanjeServera()) {
            return "OK; 1";
        } else {
            return "OK; 0";
        }
    }

    /**
     * Metoda koja upravlja administratorskom komandom IOT datoeka
     *
     * @return vraća odgovor koji je potreban poslati administratoru
     */
    private String adminIot() {
        File file = new File("iot.txt");
        return "OK; " + konfig.dajPostavku("skup.kodova.znakova") + " kod; " + "DUZINA " + file.length();
    }

    /**
     * Metoda za upravljanje administratorskom komandom ZAUSTAVI
     *
     * @return vraća odgovor koji je potrebno proslijediti administratoru
     */
    private String adminZaustavi() {
        for (RadnaDretva dretva : ServerSustava.dohvatiDretveCekanja()) {
            dretva.interrupt();
        }
        ServerSustava.zaustaviServer(true);
        return "OK;";
    }

    /**
     * Metoda koja upravlja administratorskom komandom EVIDENCIJA
     *
     * @return vraća podatke koji se prosljeđuju administratoru
     */
    private String adminEvidencija() throws IOException {
        File file = new File("NWTiS_hcosic2_evidencija_rada.bin");
        return "OK; " + konfig.dajPostavku("skup.kodova.znakova") + " kod; " + "DUZINA " + file.length()+"<CRLF>";
    }
}
