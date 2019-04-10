package org.foi.nwtis.hcosic2.web.dretve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.ServletContext;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.hcosic2.web.zrna.PregledPoruka;

/**
 * Pozadinska dretva koja provjerava na James mail poslužitelju u pravilnom
 * intervalu ima li poruka u poštanskom sandučiću korisnika. Koristi se IMAP protokol. 
 * Poruke koje sadrže samo jedan privitak s točno traženim nazivom 
 * nazivamo NWTiS porukama, obrađuju se tako da se ispituje sadržaj datoteke privitka.
 * Za poruku koja ima privitak ispravne sintakse slijedi provođenja postupka: 
 * ako se radi o komandi "dodaj" tada treba dodati zapis u tablicu UREDAJI u bazi
 * podataka, s time da ako već postoji takav id, onda je pogreška. Ako se radi o
 * komandi "azuriraj" tada se provjerava zapis u tablici UREDAJI za zadani id IoT
 * i ako postoji, ažurira se u tablici. Ako ne postoji, onda je pogreška. Svaka
 * primljena poruka upisuje se u tablicu DNEVNIK bez obzira na status prethodne akcije.
 * Obrađene NWTiS poruke prebacuju se u posebnu mapu.Ako mapa ne postoji, dretva ju 
 * treba sama kreirati. Ostale poruke treba ostaviti da imaju isti status koje su imale prije
 * nego što ih je dretva provjeravala. 
 * @author Hrvoje
 */

public class ObradaPoruka extends Thread {

    private String posluzitelj;
    private String korime;
    private String lozinka;
    private int spavanje;
    private boolean radi = true;
    private ServletContext context = null;
    private String port;
    private String predmet;
    private String folderNwtis;
    private static ServletContext sc = null;
    private Connection veza;
    private static Statement statement;
    private String nazivPrivitka;
    private Folder folderInbox;
    private Session session;
    private Store store;
    List<Message> listaNwtisPoruka = new ArrayList<>();
    private Folder nwtisFolder;
    private Date datum = new Date();
    private int broj = 0;
    private float trajanjeObrade;
    private int brojPoruka;
    private int brojIot;
    private int brojAzuriraniIot;
    private int brojNeispravnih;
    private int brojObradaPoruka = 1;

    @Override
    public void interrupt() {
        radi = false;
        super.interrupt();
    }

    @Override
    public void run() {
        Date obradaZapocela = new Date();
        try {
            veza = SlusacAplikacije.getConnection();
            statement = this.veza.createStatement();
            while (radi) {
                pokreniSesiju();
                otvoriNwtisFolder();
                broj++;
                prelistajPoruke();
                zatvaranjeFoldera();
                System.out.println("Ovo je " + broj + ". interacija!");
                Date obradaZavrsila = new Date();
                trajanjeObrade = System.currentTimeMillis() - obradaZapocela.getTime();
                kreirajDatotekuORadu(obradaZapocela, obradaZavrsila, trajanjeObrade, brojPoruka, brojIot, brojAzuriraniIot, brojNeispravnih);
                try {
                    sleep(spavanje * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

    @Override
    public synchronized void start() {
        Konfiguracija konfig = (Konfiguracija) context.getAttribute("Mail_Konfig");
        posluzitelj = konfig.dajPostavku("mail.server");
        korime = konfig.dajPostavku("mail.usernameThread");
        lozinka = konfig.dajPostavku("mail.passwordThread");
        spavanje = Integer.parseInt(konfig.dajPostavku("mail.timeSecThreadCycle"));
        port = konfig.dajPostavku("mail.imap.port");
        predmet = konfig.dajPostavku("mail.subjectEmail");
        folderNwtis = konfig.dajPostavku("mail.folderNWTiS");
        nazivPrivitka = konfig.dajPostavku("mail.attachmentFilename");
        super.start();
    }

    /**
     * Funkcija koja starta sesiju spaja se na store te otvara inbox folder
     */
    public void pokreniSesiju() {
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            session = Session.getInstance(properties, null);

            store = session.getStore("imap");
            store.connect(posluzitelj, korime, lozinka);

            folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            Message[] messages = folderInbox.getMessages();
            brojPoruka = messages.length;
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja provjerava postoji li NWTiS folder ukoliko postoji otvara
     * ga ukoliko ne postoji kreira novi folder
     */
    public void otvoriNwtisFolder() {
        try {
            nwtisFolder = store.getFolder(folderNwtis);
            if (!nwtisFolder.exists()) {
                nwtisFolder.create(Folder.HOLDS_MESSAGES);
            }
            nwtisFolder.open(Folder.READ_WRITE);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja obrađuje komandu dodaj te izvršava ono što komanda mora
     * izvršavati
     *
     * @param regexDodaj regex koji provjerava je li zaprimljena komanda stvarno
     * "dodaj" komanda
     * @param message poruka iz inbox-a koju komanda dodaj obrađuje
     */
    public void obradiDodaj(String regexDodaj, Message message) {
        try {
            Pattern uzorak = Pattern.compile(regexDodaj);
            Matcher dodaj = uzorak.matcher(PregledPoruka.dohvatiSadrzajPrivitka(message));
            if (dodaj.find()) {
                Message[] poruke = new Message[]{message};
                folderInbox.copyMessages(poruke, nwtisFolder);
                message.setFlag(Flags.Flag.DELETED, true);
                String provjeri = "SELECT * FROM uredaji WHERE id =" + dodaj.group(2);
                ResultSet rezultat = this.statement.executeQuery(provjeri);
                boolean postoji = false;
                while (rezultat.next()) {
                    System.out.println("Pogreška: Id koji želite upisati već postoji u bazi!");
                    postoji = true;
                }
                if (!postoji) {
                    String upit2 = "INSERT INTO uredaji (id, naziv, sadrzaj) VALUES (" + dodaj.group(2) + ",'"
                            + dodaj.group(4) + "','" + PregledPoruka.dohvatiSadrzajPrivitka(message) + "')";
                    statement.executeUpdate(upit2);
                    brojIot++;
                }
            }
        } catch (MessagingException | SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja obrađuje komandu azuriraj te izvršava ono što komanda mora
     * izvršavati
     *
     * @param regexAzuriraj regex koji provjerava je li zaprimljena komanda
     * stvarno "azuriraj" komanda
     * @param message poruka iz inbox-a koju komanda dodaj obrađuje
     */
    public void obradAzuriraj(String regexAzuriraj, Message message) {
        try {
            Pattern uzorak2 = Pattern.compile(regexAzuriraj);
            Matcher azuriraj = uzorak2.matcher(PregledPoruka.dohvatiSadrzajPrivitka(message));
            if (azuriraj.find()) {
                Message[] poruke = new Message[]{message};
                folderInbox.copyMessages(poruke, nwtisFolder);
                message.setFlag(Flags.Flag.DELETED, true);
                String provjeri2 = "SELECT * FROM uredaji WHERE id =" + azuriraj.group(2);
                ResultSet rezultat2 = this.statement.executeQuery(provjeri2);
                boolean postoji2 = false;
                if (rezultat2.next()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String vrijemeIzmjene = dateFormat.format(datum.getTime());
                    String upit3 = "UPDATE uredaji SET sadrzaj='" + PregledPoruka.dohvatiSadrzajPrivitka(message)
                            + "',vrijeme_promjene='" + vrijemeIzmjene + "' WHERE id=" + azuriraj.group(2);
                    statement.executeUpdate(upit3);
                    brojAzuriraniIot++;
                    postoji2 = true;
                } if (!postoji2) 
                    System.out.println("Navedeni IOT uređaj ne postoji u bazi");
            }
        } catch (MessagingException | SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja obrađene "NWTiS" poruke prebacuje iz foldera "INBOX" u
     * "NWTiS folder"
     */
    public void prebaciObradenePoruke() {
        if (!listaNwtisPoruka.isEmpty()) {
            try {
                Message[] m = listaNwtisPoruka.toArray(new Message[listaNwtisPoruka.size()]);
                folderInbox.copyMessages(m, nwtisFolder);
            } catch (MessagingException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Funkcija koja zatvara "INBOX" i "NWTiS" folder briše označane poruke iz
     * "INBOX" foldera te zatvara store
     */
    public void zatvaranjeFoldera() {
        try {
            nwtisFolder.close(false);
            folderInbox.expunge();
            folderInbox.close(false);
            store.close();
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja obrađuje sve poruke iz foldera "INBOX" i izvršava komande
     * "dodaj" i "azuriraj"
     */
    public void prelistajPoruke() {
        String regexDodaj = "(.\"id\":)+([1-4])+(,\"komanda\":\"dodaj\",\"naziv\":)+\"([a-zA-Z žćčđš]+)\"+(.*)";
        String regexAzuriraj = "(.\"id\":)+([1-4])+(,\"komanda\":\"azuriraj\",)+\"([a-zA-Z žćčđš]+)\"+(.*)";
        try {
            Message[] messages = null;
            messages = folderInbox.getMessages();
            System.out.println("U sandučiću trenutno ima " + messages.length + " poruka!");
            for (int i = 0; i < messages.length; i++) {
                String upit = "INSERT INTO dnevnik (sadrzaj) VALUES ('" + PregledPoruka.dohvatiSadrzajPrivitka(messages[i]) + "')";
                this.statement.executeUpdate(upit);
                obradiDodaj(regexDodaj, messages[i]);
                obradAzuriraj(regexAzuriraj, messages[i]);
                odrediBrojNeispravnihPoruka(messages[i]);
            }
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja broji neispravne poruke
     * @param message prima poruku koju treba obraditi
     */
    public void odrediBrojNeispravnihPoruka(Message message) {
        String regexDodaj = "(.\"id\":)+([1-4])+(,\"komanda\":\"dodaj\",\"naziv\":)+\"([a-zA-Z žćčđš]+)\"+(.*)";
        String regexAzuriraj = "(.\"id\":)+([1-4])+(,\"komanda\":\"azuriraj\",)+\"([a-zA-Z žćčđš]+)\"+(.*)";
        try {
            Pattern uzorak = Pattern.compile(regexDodaj);
            Matcher dodaj = uzorak.matcher(PregledPoruka.dohvatiSadrzajPrivitka(message));
            Pattern uzorak2 = Pattern.compile(regexAzuriraj);
            Matcher azuriraj = uzorak2.matcher(PregledPoruka.dohvatiSadrzajPrivitka(message));
            if (!dodaj.find()) {
                brojNeispravnih++;
            } else if (!azuriraj.find()) {
                brojNeispravnih++;
            }
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Funkcija koja kreira datoteku o radu i sprema ju u WEB-INF direktorij
     * @param obradaZapocela vrijeme pocetka obrade
     * @param obradaZavrsila vrijeme zavrsetka obrade
     * @param trajanjeObrade trajanje obrade
     * @param brojPoruka broj poruka u jednom ciklusu
     * @param brojIot broj dodanih IOT uređaja
     * @param brojAzuriraniIot broj ažuriranih IOT uređaja
     * @param brojNeispravnih  broj neispravnih poruka
     */
    public void kreirajDatotekuORadu(Date obradaZapocela, Date obradaZavrsila, Float trajanjeObrade, int brojPoruka,
            int brojIot, int brojAzuriraniIot, int brojNeispravnih) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh.mm.ss.zzz");
        String zapocelaObrada = dateFormat.format(obradaZapocela);
        String zavrsilaObrada = dateFormat.format(obradaZavrsila);
        try {
            String podaciORadu = "Obrada poruka broj: " + brojObradaPoruka++;
            podaciORadu += "\r\nObrada započela u: " + zapocelaObrada;
            podaciORadu += "\r\nObrada završila u: " + zavrsilaObrada;
            podaciORadu += "\r\nTrajanje obrade u ms: " + trajanjeObrade;
            podaciORadu += "\r\nBroj poruka: " + brojPoruka;
            podaciORadu += "\r\nBroj dodanih IoT: " + brojIot;
            podaciORadu += "\r\nBroj ažuriranih IOT: " + brojAzuriraniIot;
            podaciORadu += "\r\nBroj neispravnih poruka: " + brojNeispravnih + "\r\n";
            String putanja = context.getRealPath("/WEB-INF") + java.io.File.separator;
            File datoteka = new File(putanja + folderNwtis);
            Files.write(datoteka.toPath(), podaciORadu.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
