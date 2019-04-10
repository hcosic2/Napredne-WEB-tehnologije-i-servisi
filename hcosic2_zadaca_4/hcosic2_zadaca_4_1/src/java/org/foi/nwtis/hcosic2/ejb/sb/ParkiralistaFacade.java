
package org.foi.nwtis.hcosic2.ejb.sb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.hcosic2.ejb.eb.Parkiralista;

/**
 * Stateless session bean za klasu entiteta Parkiralista
 * @author Hrvoje
 */
@Stateless
public class ParkiralistaFacade extends AbstractFacade<Parkiralista> {

    @PersistenceContext(unitName = "zadaca_4_1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ParkiralistaFacade() {
        super(Parkiralista.class);
    }
    
}
