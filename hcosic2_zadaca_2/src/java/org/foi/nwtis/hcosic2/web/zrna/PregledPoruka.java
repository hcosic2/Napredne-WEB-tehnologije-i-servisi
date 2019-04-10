package org.foi.nwtis.hcosic2.web.zrna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.hcosic2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.hcosic2.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.hcosic2.web.dretve.ObradaPoruka;
import org.foi.nwtis.hcosic2.web.kontrole.Izbornik;
import org.foi.nwtis.hcosic2.web.kontrole.Poruka;
import org.foi.nwtis.hcosic2.web.kontrole.Poruka.VrstaPoruka;

/**
 * Kod odabira pregleda primljenih email poruka prikazuje se padajući
 * izbornik u kojem su elementi mape iz email korisničkog računa
 * i gump za promjenu mape. Od svih mapa samo su od interesa i jedino se prikazuju mapa
 * INBOX i mapa koja je zadana konfiguracijom za NWTiS poruke ako
 * trenutno postoji. Na početku je odabrana mapa INBOX. Slijedi tablica s prikazom
 * informacija o n najsvježijih poruka iz odabrane mape. Za  svaku email poruku
 * potrebno je prikazati tko je poslao, kada je poslao, predmet poruke, 
 * vrsta poruke, sadržaj privitka ako je NWTiS poruka, ako nije NWTiS poruka 
 * onda ništa. NE SMIJU se čitati sve poruke iz mape nego 
 * samo onoliko koliko je potrebno za prikaz prema postavki. Ispod tablice prikazuje 
 * se ukupan broj email poruka u izabranoj mapi, gumb za prethodne i sljedeće poruke. 
 * @author Hrvoje
 */

@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

    private String posluzitelj;
    private String korime;
    private String lozinka;
    private List<Izbornik> popisMapa;
    private String odabranaMapa;
    private List<Poruka> popisPoruka = new ArrayList<>();
    private int ukupnoPoruka = 1;
    private int brojPrikazanihPoruka;
    private int odPrikazanihPoruka;
    private int doPrikazanihPoruka;
    private Store store;
    private HttpSession httpsession;
    private String mapaNwtis;
    private static String nazivPrivitka;

    public PregledPoruka() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String datoteka = ec.getRealPath("/WEB-INF") + File.separator + ec.getInitParameter("konfiguracija");
        Konfiguracija konfig = null;
        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (konfig != null)
            preuzmiPodatkeKonfiguracije(konfig);
        spremanjeVarijabli();
        java.util.Properties properties = System.getProperties();
        properties.put("mail.smtp.host", posluzitelj);
        Session session = Session.getInstance(properties, null);
        try {
            store = session.getStore("imap");
            store.connect(posluzitelj, korime, lozinka);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        preuzmiMape();
        preuzmiPoruke();
    }
    
    /**
     * Funkcija koja preuzima mape s James mail servera
     */
    private void preuzmiMape() {
        popisMapa = new ArrayList<>();
        try {
            Folder[] mape = store.getDefaultFolder().list();
            for (Folder f : mape) {
                if ("INBOX".equals(f.getFullName()) || mapaNwtis.equals(f.getFullName())) {
                    popisMapa.add(new Izbornik(f.getFullName() + " - " + f.getMessageCount(), f.getName()));
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Funkcija koja preuzima sve poruke iz odabrane mape s James mail servera
     * @throws IOException 
     */
    private void preuzmiPoruke() throws IOException {
        popisPoruka.clear();
        try {
            Folder folder = store.getFolder(httpsession.getAttribute("mapa").toString());
            folder.open(Folder.READ_ONLY);
            ukupnoPoruka = folder.getMessageCount();
            popisPoruka.clear();
            uvjetiStranicenja();
            if (ukupnoPoruka < brojPrikazanihPoruka)
                doPrikazanihPoruka = ukupnoPoruka;
            if (ukupnoPoruka > 0) {
                Message[] pomak = folder.getMessages(odPrikazanihPoruka, doPrikazanihPoruka);
                for (Message m : pomak) {
                    if (m.getContent() instanceof String) {
                        popisPoruka.add(new Poruka(String.valueOf(m.getMessageNumber()), m.getSentDate(), m.getReceivedDate(), Arrays.toString(m.getFrom()).replaceAll("\\[|\\]", ""), m.getSubject(), m.getContent().toString(), Poruka.VrstaPoruka.neNWTiS_poruka));
                    } else {
                        popisPoruka.add(new Poruka(String.valueOf(m.getMessageNumber()), m.getSentDate(), m.getReceivedDate(), Arrays.toString(m.getFrom()).replaceAll("\\[|\\]", ""), m.getSubject(), dohvatiSadrzajPrivitka(m), Poruka.VrstaPoruka.neNWTiS_poruka));
                    }
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        httpsession.setAttribute("od", odPrikazanihPoruka);
        httpsession.setAttribute("do", doPrikazanihPoruka);
    }

    public int getOdPrikazanihPoruka() {
        return odPrikazanihPoruka;
    }

    public void setOdPrikazanihPoruka(int odPrikazanihPoruka) {
        this.odPrikazanihPoruka = odPrikazanihPoruka;
    }

    public int getDoPrikazanihPoruka() {
        return doPrikazanihPoruka;
    }

    public void setDoPrikazanihPoruka(int doPrikazanihPoruka) {
        this.doPrikazanihPoruka = doPrikazanihPoruka;
    }

    public int getUkupnoPoruka() {
        return ukupnoPoruka;
    }

    public void setUkupnoPoruka(int ukupnoPoruka) {
        this.ukupnoPoruka = ukupnoPoruka;
    }

    public String getPosuzitelj() {
        return posluzitelj;
    }

    public void setPosuzitelj(String posuzitelj) {
        this.posluzitelj = posuzitelj;
    }

    public String getKorime() {
        return korime;
    }

    public void setKorime(String korime) {
        this.korime = korime;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public List<Izbornik> getPopisMapa() {
        return popisMapa;
    }

    public void setPopisMapa(List<Izbornik> popisMapa) {
        this.popisMapa = popisMapa;
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    public List<Poruka> getPopisPoruka() {
        return popisPoruka;
    }

    public void setPopisPoruka(List<Poruka> popisPoruka) {
        this.popisPoruka = popisPoruka;
    }

    public String promjeniJezik() {
        return "promjeniJezik";
    }

    public String saljiPoruku() {
        return "saljiPoruku";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }
    
    /**
     * Funkcija koja mijenja trenutno odabranu mapu iz koje se dohvaćaju
     * sve poruke s James mail servera
     * @return vraća String "PromjenaMape"
     * @throws IOException 
     */
    public String promjenaMape() throws IOException {
        httpsession.setAttribute("mapa", odabranaMapa);
        odPrikazanihPoruka = 1;
        doPrikazanihPoruka = brojPrikazanihPoruka;
        this.preuzmiPoruke();
        return "PromjenaMape";
    }
    
    /**
     * Funkcija koja prikazuje prethodne mape u intervalu koji je određen
     * konfiguracijom
     * @return vraća string "PrethodnePoruke"
     * @throws IOException 
     */
    public String prethodnePoruke() throws IOException {
        httpsession.setAttribute("od", odPrikazanihPoruka);
        httpsession.setAttribute("do", doPrikazanihPoruka);
        doPrikazanihPoruka = odPrikazanihPoruka - 1;
        odPrikazanihPoruka -= brojPrikazanihPoruka;
        this.preuzmiPoruke();
        return "PrethodnePoruke";
    }
    
    /**
     * Funkcija koja prikazuje sljedeće mape u intervalu koji je određen
     * konfiguracijom
     * @return vraća string "SljedecePoruke"
     * @throws IOException 
     */
    public String sljedecePoruke() throws IOException {
        httpsession.setAttribute("od", odPrikazanihPoruka);
        httpsession.setAttribute("do", doPrikazanihPoruka);
        odPrikazanihPoruka = doPrikazanihPoruka + 1;
        doPrikazanihPoruka += brojPrikazanihPoruka;
        this.preuzmiPoruke();
        return "SljedecePoruke";
    }
    
    /**
     * Funkcija koja dohvaća sadržaj privitka prosljeđene poruke
     * @param message poruka kojoj je potrebno dohvatiti privitak
     * @return vraća sadržaj privitka u String obliku
     * @throws MessagingException 
     */
    public static String dohvatiSadrzajPrivitka(Message message) throws MessagingException {
        String contentType = message.getContentType();
        String sadrzaj = "";
        if (contentType.contains("multipart")) {
            try {
                Multipart multiPart = (Multipart) message.getContent();
                for (int i = 0; i < multiPart.getCount(); i++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        part.saveFile("D:/Foi/NWTiS/zadaca_2/hcosic2_zadaca_2/build/web/WEB-INF/"+ nazivPrivitka);
                        InputStream input = part.getInputStream();
                        int byteRead;
                        StringBuffer sb = new StringBuffer();
                        while ((byteRead = input.read()) != -1) {
                            sb.append((char) byteRead);
                        }
                        sadrzaj = sb.toString();
                    }
                }
            } catch (IOException | ClassCastException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sadrzaj;
    }
    
    /**
     * Funkcija koja sprema varijable "odabranaMapa", "odPrikazanihPoruka",
     * "doPrikazanihPoruka" u sesiju koje su nam kasnije bitne zbog straničenja
     */
    public void spremanjeVarijabli() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        httpsession = (HttpSession) facesContext.getExternalContext().getSession(true);
        if (httpsession.getAttribute("mapa") != null) {
            odabranaMapa = httpsession.getAttribute("mapa").toString();
        } else {
            odabranaMapa = "INBOX";
            httpsession.setAttribute("mapa", odabranaMapa);
        }
        if (httpsession.getAttribute("od") != null) {
            odPrikazanihPoruka = Integer.parseInt(httpsession.getAttribute("od").toString());
        } else {
            httpsession.setAttribute("od", odPrikazanihPoruka);
        }
        if (httpsession.getAttribute("do") != null) {
            doPrikazanihPoruka = Integer.parseInt(httpsession.getAttribute("do").toString());
        } else {
            httpsession.setAttribute("do", doPrikazanihPoruka);
        }
    }
    
    /**
     * Funkcija koja preuzima potrebne podatke iz konfiguracije 
     * "NWTiS.db.config_1"
     * @param konfig 
     */
    public void preuzmiPodatkeKonfiguracije(Konfiguracija konfig) {
        posluzitelj = konfig.dajPostavku("mail.server");
        korime = konfig.dajPostavku("mail.usernameThread");
        lozinka = konfig.dajPostavku("mail.passwordThread");
        brojPrikazanihPoruka = Integer.parseInt(konfig.dajPostavku("mail.numMessagesToShow"));
        mapaNwtis = konfig.dajPostavku("mail.folderNWTiS");
        nazivPrivitka = konfig.dajPostavku("mail.attachmentFilename");
    }
    
    /**
     * Funkcija koja upravlja straničenjem pregleda poruka upravlja s gumbima
     * "Sljedeće", "Prethodne"
     */
    public void uvjetiStranicenja() {
        if (ukupnoPoruka < brojPrikazanihPoruka) {
            doPrikazanihPoruka = ukupnoPoruka;
        }
        if (odPrikazanihPoruka > ukupnoPoruka) {
            doPrikazanihPoruka = ukupnoPoruka;
            odPrikazanihPoruka = ukupnoPoruka - brojPrikazanihPoruka;
        }
        if (doPrikazanihPoruka > ukupnoPoruka) {
            doPrikazanihPoruka = ukupnoPoruka;
        }
        if (odPrikazanihPoruka < 1) {
            odPrikazanihPoruka = 1;
            doPrikazanihPoruka = brojPrikazanihPoruka;
        }
    }
}
