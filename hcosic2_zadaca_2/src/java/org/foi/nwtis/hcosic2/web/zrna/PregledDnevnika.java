package org.foi.nwtis.hcosic2.web.zrna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import org.foi.nwtis.hcosic2.web.dretve.ObradaPoruka;
import org.foi.nwtis.hcosic2.web.kontrole.Dnevnik;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Kod odabira pregleda dnevnika prikazuje se odabir intervala Od - Do i gump za pregled.
 * Upisanim elementima Od i Do treba provjeriti ispravnost u formatu datumskog 
 * tipa podatka. Slijedi tablica s prikazom informacijao n najsvježijih zaspisa u 
 * dnevniku za traženi interval. Za svaki zapis iz dnevnika potrebno je prikazati 
 * kada je spremljen i njegov sadržaj. NE SMIJU se čitati u memoriju svi zapisi 
 * iz dnevnika nego samo onoliko koliko je potrebno prema postavki. Ispod tablice 
 * prikazuje se ukupan broj zapisa u dnevniku koji zadovoljavaju interval Od Do, 
 * gumb za prethodne i sljedeće zapise dnevnika. Pojedini gumb treba sakriti 
 * ako nema sadržaja koji o njemu ovisi. Npr. na početku nema prethodnih
 * zapisa iz dnevnika. Aktiviranjem pojedinog gumba treba prikazati izabrani skup 
 * zapisa iz dnevnika. 
 * @author Hrvoje
 */

@Named(value = "pregledDnevnika")
@RequestScoped
public class PregledDnevnika {

    private List<Dnevnik> listaDnevnika = new ArrayList<>();
    private int ukupanBrojZapisa;
    private int brojZapisaZaPrikaz;
    private String odPrikazanihZapisa;
    private String doPrikazanihZapisa;

    public PregledDnevnika() {
    }
    /**
     * Funkcija koja preuzima podatke iz paze podataka na temelju 
     * prosljeđenih parametara
     * @param odPrikazanihZapisa parametar koji nam govori od kojeg
     * vremenskog razdoblja želimo ispisati podatke
     * @param doPrikazanihZapisa parametar koji nam govori do kojeg
     * vremenskog razdoblja želimo ispisati podatke
     */
    public void preuzmiZapise(String odPrikazanihZapisa, String doPrikazanihZapisa){
        try {
            ResultSet rs = null;
            Connection con = SlusacAplikacije.getConnection();
            PreparedStatement pst = null;
            String stm = "Select * from dnevnik where vrijeme between'"+odPrikazanihZapisa+"'and'"+doPrikazanihZapisa+"'";
            
            pst = con.prepareStatement(stm);
            pst.execute();
            rs = pst.getResultSet();
            
            while(rs.next()){
                Dnevnik dnevnik = new Dnevnik(Integer.parseInt(rs.getString(1)), rs.getString(2), rs.getDate(3));
                listaDnevnika.add(dnevnik);
                ukupanBrojZapisa = listaDnevnika.size();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     /**
      * Funkcija koja mijenja interval "od", "do" kojeg se dohvaćaju podaci
      * iz baze podataka
      * @return vraća String "PromjenaIntervala"
      */
    public String promjenaIntervala() {
        this.preuzmiZapise(odPrikazanihZapisa, doPrikazanihZapisa);
        return "PromjenaIntervala";
    }

    public String prethodniZapisi() {

        return "PrethodniZapisi";
    }

    public String sljedeciZapisi() {

        return "SljedeciZapisi";
    }

    public String promjenaJezika() {
        return "promjenaJezika";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String saljiPoruku() {
        return "saljiPoruku";
    }

    public List<Dnevnik> getListaDnevnika() {
        return listaDnevnika;
    }

    public void setListaDnevnika(List<Dnevnik> listaDnevnika) {
        this.listaDnevnika = listaDnevnika;
    }

    public int getUkupanBrojZapisa() {
        return ukupanBrojZapisa;
    }

    public void setUkupanBrojZapisa(int ukupanBrojZapisa) {
        this.ukupanBrojZapisa = ukupanBrojZapisa;
    }

    public int getBrojZapisaZaPrikaz() {
        return brojZapisaZaPrikaz;
    }

    public void setBrojZapisaZaPrikaz(int brojZapisaZaPrikaz) {
        this.brojZapisaZaPrikaz = brojZapisaZaPrikaz;
    }

    public String getOdPrikazanihZapisa() {
        return odPrikazanihZapisa;
    }

    public void setOdPrikazanihZapisa(String odPrikazanihZapisa) {
        this.odPrikazanihZapisa = odPrikazanihZapisa;
    }

    public String getDoPrikazanihZapisa() {
        return doPrikazanihZapisa;
    }

    public void setDoPrikazanihZapisa(String doPrikazanihZapisa) {
        this.doPrikazanihZapisa = doPrikazanihZapisa;
    }
    
}

