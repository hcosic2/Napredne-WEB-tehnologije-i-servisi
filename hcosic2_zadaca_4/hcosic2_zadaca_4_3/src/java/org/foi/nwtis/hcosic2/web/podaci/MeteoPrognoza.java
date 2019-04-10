/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.web.podaci;

/**
 *
 * @author dkermek
 */
public class MeteoPrognoza {

    private int id;
    private int sat;
    private MeteoPodaci prognoza;

    public MeteoPrognoza() {
    }

    public MeteoPrognoza(int id, int dan, MeteoPodaci prognoza) {
        this.id = id;
        this.sat = dan;
        this.prognoza = prognoza;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public MeteoPodaci getPrognoza() {
        return prognoza;
    }

    public void setPrognoza(MeteoPodaci prognoza) {
        this.prognoza = prognoza;
    }


}