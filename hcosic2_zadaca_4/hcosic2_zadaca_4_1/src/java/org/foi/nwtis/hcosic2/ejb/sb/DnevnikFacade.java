
package org.foi.nwtis.hcosic2.ejb.sb;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.hcosic2.ejb.eb.Dnevnik;

/**
 * Stateless session bean za klasu entiteta Dnevnik
 * @author Hrvoje
 */
@Stateless
public class DnevnikFacade extends AbstractFacade<Dnevnik> {

    @PersistenceContext(unitName = "zadaca_4_1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DnevnikFacade() {
        super(Dnevnik.class);
    }

    /**
     * Funkcija koja prema prosljeđenim parametrima filtrira podatke iz baze podataka
     * @param ipAdresa ip adresa korisnika
     * @param odDatuma datum od kojeg se filtriraju zahtjevi
     * @param doDatuma datum do kojeg se filtriraju zahtjevi
     * @param adresaZahtjeva url zahtjeva
     * @param trajanje trajanje izvršene akcije
     * @return vraća listu filtriranih dnevnika
     */
    public List<Dnevnik> dnevnikFilter(String ipAdresa, long odDatuma, long doDatuma, String adresaZahtjeva, Integer trajanje) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Dnevnik> cq = cb.createQuery(Dnevnik.class);
        Root<Dnevnik> dnevnik = cq.from(Dnevnik.class);
        List<Predicate> predikati = new ArrayList<Predicate>();
        provjeriUneseneParametre(predikati, cb, dnevnik, ipAdresa, odDatuma, doDatuma, adresaZahtjeva, trajanje);
        cq.select(dnevnik).where(predikati.toArray(new Predicate[]{}));
        TypedQuery<Dnevnik> tq = em.createQuery(cq);
        List<Dnevnik> listaDnevnika = tq.getResultList();
        return listaDnevnika;
    }

    /**
     * Funkcija koja provjerava koji su parametri upisani te definira slučajeve
     * ukoliko su pojedini parametri definirani odnosno nisu
     * @param predikati lista prediakata
     * @param cb CriteriaBuilder
     * @param dnevnik Root lista
     * @param ipAdresa ip adresa korisnika
     * @param odDatuma datum od kojeg se filtriraju zahtjevi
     * @param doDatuma datum do kojeg se filtriraju zahtjevi
     * @param adresaZahtjeva url zahtjeva
     * @param trajanje trajanje izvršene akcije
     */
    public void provjeriUneseneParametre(List<Predicate> predikati, CriteriaBuilder cb, Root<Dnevnik> dnevnik,
            String ipAdresa, long odDatuma, long doDatuma, String adresaZahtjeva, Integer trajanje){
        DateFormat formatDatuma = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (!ipAdresa.equals("")) {
            predikati.add(cb.like(dnevnik.<String>get("ipadresa"), ipAdresa));
        }
        if (odDatuma > 0 && doDatuma > 0) {
            predikati.add(cb.between(dnevnik.<Timestamp>get("vrijeme"),
                    Timestamp.valueOf(formatDatuma.format(new Date(odDatuma))), Timestamp.valueOf(formatDatuma.format(new Date(doDatuma)))));
        }else if (odDatuma > 0) {
            predikati.add(cb.between(dnevnik.<Timestamp>get("vrijeme"),
                    Timestamp.valueOf(formatDatuma.format(new Date(odDatuma))), Timestamp.valueOf(formatDatuma.format(new Date()))));
        } else if (doDatuma > 0) {
            predikati.add(cb.between(dnevnik.<Timestamp>get("vrijeme"),
                    Timestamp.valueOf(formatDatuma.format(new Date(0))), Timestamp.valueOf(formatDatuma.format(new Date(doDatuma)))));
        }
        if (!adresaZahtjeva.equals("")) {
            predikati.add(cb.like(dnevnik.<String>get("url"), adresaZahtjeva));
        }
        if (trajanje != null) {
            predikati.add(cb.equal(dnevnik.<Integer>get("trajanje"), trajanje));
        }
    }
}
