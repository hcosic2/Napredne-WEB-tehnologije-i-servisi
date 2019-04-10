package org.foi.nwtis.hcosic2.zadaca_1;

/**
 * Klasa IOT služi za evidenciju podataka o svim IOT uređajima. 
 * Treba odrediti dodatne klase i varijable u koje će se pridružiti vrijednosti.
 * Potrebno je voditi brigu o međusobnom isključivanju dretvi kod pristupa
 * evidenciji rada.
 * 
 * @author Hrvoje
 */

import java.util.ArrayList;
import java.util.List;

public class IOT {
    
    public int id;
    public List<String> lista = new ArrayList<String>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getLista() {
        return lista;
    }

    public void setLista(List<String> lista) {
        this.lista = lista;
    }
    
    
    
}
