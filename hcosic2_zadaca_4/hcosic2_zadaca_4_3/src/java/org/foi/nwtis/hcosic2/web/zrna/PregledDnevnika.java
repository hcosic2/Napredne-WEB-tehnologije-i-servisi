
package org.foi.nwtis.hcosic2.web.zrna;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.hcosic2.ejb.eb.Dnevnik;
import org.foi.nwtis.hcosic2.ejb.sb.DnevnikFacade;

/**
 * Klasa koja služi za dohvaćanje zapisa o dnevniku iz baze podataka. Podaci se
 * filtriraju na temelju upisanih parametara. Upisati možemo parametre po želji,
 * a ne moramo upisati niti jedan parametar. Ukoliko se ne upiše niti jedan parametar
 * onda se dohvaćaju svi podaci iz tablice dnevnik.
 * @author Hrvoje
 */
@Named(value = "pregledDnevnika")
@SessionScoped
public class PregledDnevnika implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    private String ipAdresa;
    private String odDatuma;
    private String doDatuma;
    private String adresaZahtjeva;
    private Integer trajanje;
    private List<Dnevnik> listaDnevnika = new ArrayList<>();
    private EntityManager em;

    public PregledDnevnika() {
    }

    /**
     * Funkcija koja dohvaća filtrirane podatke o dnevniku iz baze podataka na
     * temelju prosljeđenih parametara. Da bi se funkcija izvršila moguće je 
     * ne upisati niti jedan parametar.
     */
    public void dohvatiDnevnik() {
        try {
            listaDnevnika.clear();
            System.out.println("Ovo je datum od: " + odDatuma + "  Ovo je datum do: " + doDatuma);
            DateFormat formatDatuma = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            if (odDatuma.equals("") && doDatuma.equals("")) {
                Date datumOd = new Date(0);
                Date datumDo = new Date();
                odDatuma = formatDatuma.format(datumOd);
                doDatuma = formatDatuma.format(datumDo);
            } else if (odDatuma.equals("")) {
                Date datumOd = new Date(0);
                odDatuma = formatDatuma.format(datumOd);
            } else if (doDatuma.equals("")) {
                Date datumDo = new Date();
                doDatuma = formatDatuma.format(datumDo);
            }
            Date parseOd = (Date) formatDatuma.parse(odDatuma);
            Date parseDo = (Date) formatDatuma.parse(doDatuma);
            long datumOd = parseOd.getTime();
            long datumDo = parseDo.getTime();
            listaDnevnika.addAll(dnevnikFacade.dnevnikFilter(ipAdresa, datumOd, datumDo, adresaZahtjeva, trajanje));
        } catch (ParseException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DnevnikFacade getDnevnikFacade() {
        return dnevnikFacade;
    }

    public void setDnevnikFacade(DnevnikFacade dnevnikFacade) {
        this.dnevnikFacade = dnevnikFacade;
    }

    public String getIpAdresa() {
        return ipAdresa;
    }

    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    public String getOdDatuma() {
        return odDatuma;
    }

    public void setOdDatuma(String odDatuma) {
        this.odDatuma = odDatuma;
    }

    public String getDoDatuma() {
        return doDatuma;
    }

    public void setDoDatuma(String doDatuma) {
        this.doDatuma = doDatuma;
    }

    public String getAdresaZahtjeva() {
        return adresaZahtjeva;
    }

    public void setAdresaZahtjeva(String adresaZahtjeva) {
        this.adresaZahtjeva = adresaZahtjeva;
    }

    public Integer getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(Integer trajanje) {
        this.trajanje = trajanje;
    }

    public List<Dnevnik> getListaDnevnika() {
        return listaDnevnika;
    }

    public void setListaDnevnika(List<Dnevnik> listaDnevnika) {
        this.listaDnevnika = listaDnevnika;
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

}
