/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.ejb.sb;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.foi.nwtis.hcosic2.ejb.eb.Meteo;
import org.foi.nwtis.hcosic2.ejb.eb.Parkiralista;

/**
 * Stateless session bean za klasu entiteta Meteo
 * @author Hrvoje
 */
@Stateless
public class MeteoFacade extends AbstractFacade<Meteo> {

    @PersistenceContext(unitName = "zadaca_4_1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MeteoFacade() {
        super(Meteo.class);
    }

    /**
     * Funkcija koja traži objekt tipa Meteo prema prosljeđenom id-u
     * @param p id parkiralista
     * @return 
     */
    public List<Meteo> findByParking(int p) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Meteo> meteo = cq.from(Meteo.class);
        Path<Parkiralista> premaParkingu = meteo.get("id");
        Expression<Integer> premaParkingId = premaParkingu.get("id");
        cq.where(cb.equal(premaParkingId, p));
        System.out.println(cq.toString());
        return getEntityManager().createQuery(cq).getResultList();
    }
    
}
