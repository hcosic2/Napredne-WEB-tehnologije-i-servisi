package org.foi.nwtis.hcosic2.web.zrna;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.json.Json;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Kod odabira slanja email poruka slijedi prikaz u obliku tablice 
 * unutar koje je obrazac za unos podataka poruke primatelj, pošiljatelj, 
 * predmet, sadržaj za privitak, brisanje sadržaja za privitak, preuzimanja 
 * sadržaja jedne od pripremljenih datoteka u element (kontrolu) sadržaja za privitak.
 * Inicijalno adresa primatelja je iz postavke konfguracije. Inicijalno adresa pošiljatelja je iz postavke
 * konfguracije. Inicijalno predmet je iz postavke konfguracije.  Ispravnost primatelja
 * i pošiljatelja temelji se na korektnost struktureemail adrese. 
 * Predmet poruke mora sadržavati min 10 znakova. Sadržaj za privitak treba biti ispravan json tekst.
 * @author Hrvoje
 */

@Named(value = "slanjePoruke")
@RequestScoped
public class SlanjePoruka {

    private String posluzitelj;
    private String prima;
    private String salje;
    private String predmet;
    private String sadrzajPrivitka;
    private List<String> popisDatoteka;
    private String odabranaDatoteka;
    private String nazivDatotekePrivitka;

    public SlanjePoruka() {
        sadrzajPrivitka = "{}";

        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        Konfiguracija konfig = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        this.posluzitelj = konfig.dajPostavku("mail.server");
        this.prima = konfig.dajPostavku("mail.usernameThread");
        this.salje = konfig.dajPostavku("mail.usernameEmailAddress");
        this.predmet = konfig.dajPostavku("mail.subjectEmail");
        this.nazivDatotekePrivitka = konfig.dajPostavku("mail.attachmentFilename");

        popisDatoteka = new ArrayList<>();
        String[] privici = new File(sc.getRealPath("/WEB-INF")).list();
        String regex = "(.*)+.jsp";
        Pattern uzorak = Pattern.compile(regex);
        for (String i:privici) {
            Matcher matcher = uzorak.matcher(i);
            if(matcher.matches()){
                popisDatoteka.add(i);
            }
        }
    }

    public String getPosluzitelj() {
        return posluzitelj;
    }

    public void setPosluzitelj(String posluzitelj) {
        this.posluzitelj = posluzitelj;
    }

    public String getPrima() {
        return prima;
    }

    public void setPrima(String prima) {
        this.prima = prima;
    }

    public String getSalje() {
        return salje;
    }

    public void setSalje(String salje) {
        this.salje = salje;
    }

    public String getPredmet() {
        return predmet;
    }

    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }

    public String getPrivitak() {
        return sadrzajPrivitka;
    }

    public void setPrivitak(String privitak) {
        this.sadrzajPrivitka = privitak;
    }

    public List<String> getPopisDatoteka() {
        return popisDatoteka;
    }

    public void setPopisDatoteka(List<String> popisDatoteka) {
        this.popisDatoteka = popisDatoteka;
    }

    public String getOdabranaDatoteka() {
        return odabranaDatoteka;
    }

    public void setOdabranaDatoteka(String odabranaDatoteka) {
        this.odabranaDatoteka = odabranaDatoteka;
    }

    public String promjenaJezika() {
        return "promjeniJezik";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }
    
    /**
     * Funkcija koja preuzima sadržaj .json privitka koji je učitan
     * iz web-inf mape i sprema ga u varijablu sadrzajPrivitka
     * koji se kasnije šalje korisniku u obliku texta
     * @return vraća poruku o uspješnosti preuzimanja sadržaja 
     */
    public String preuzmiSadrzaj(){
        try {
            ServletContext context = SlusacAplikacije.getSc();
            String putanja = context.getRealPath("/WEB-INF") + java.io.File.separator;
            javax.json.JsonReader jr = Json.createReader(new FileReader(putanja + odabranaDatoteka));
            javax.json.JsonObject jo = jr.readObject();
            sadrzajPrivitka = jo.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SlanjePoruka.class.getName()).log(Level.SEVERE, null, ex);
            return "Sadržaj datoteke nije uspješno preuzet: " +ex;
        }
        return "sadržajPreuzet";
    }
    
    /**
     * Funkcija briše sadržaj privitka odnosno sadržaj poruke
     * @return
     */
    public String obrisiPrivitak() {
        sadrzajPrivitka = "{}";
        return "privitakObrisan";
    }
     /**
      * Funkcija koja šalje mail korisniku
      * @return vraća poruku o uspješnosti ili neuspješnost slanja poruke
      */
    public String saljiPoruku() {
        try {
            File file = new File(nazivDatotekePrivitka);
            Files.write(file.toPath(), sadrzajPrivitka.getBytes());
            sadrzajPrivitka = "{}";
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            Session session = Session.getInstance(properties, null);
            MimeMessage message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(salje);
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(predmet);
            message.setText(sadrzajPrivitka);
            message.setSentDate(new Date());
            Transport.send(priloziPrivitak(message, file));
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Poruka nije poslana" + e;
        } catch (IOException ex) {
            Logger.getLogger(SlanjePoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        sadrzajPrivitka = "{}";
        return "porukaPoslana";
    }
     /**
      * Funkcija koja dodaje mail-u datoteku sa sadržajem kao privitak mail-a
      * @param message poruka na kojoj se dodaje privitak
      * @param file privitak koji se dodaje poruci
      * @return vraća porurku s privitkom
      * @throws MessagingException 
      */
    public Message priloziPrivitak(Message message, File file) throws MessagingException{
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        Multipart multipart = new MimeMultipart();
        
        messageBodyPart = new MimeBodyPart();
        String fileName = nazivDatotekePrivitka;
        DataSource source = new FileDataSource(file);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);

        message.setContent(multipart);
        return message;
    }
}
