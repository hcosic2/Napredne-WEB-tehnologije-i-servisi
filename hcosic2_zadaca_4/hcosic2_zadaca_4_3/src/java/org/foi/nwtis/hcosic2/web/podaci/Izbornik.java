package org.foi.nwtis.hcosic2.web.podaci;

import java.util.Objects;

public class Izbornik {
    private String labela;
    private String vrijednost;

    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }

    public String getLabela() {
        return labela;
    }

    public void setLabela(String labela) {
        this.labela = labela;
    }

    public String getVrijednost() {
        return vrijednost;
    }

    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }    

    @Override
    public String toString() {
        return "Izbornik{" + "labela=" + labela + ", vrijednost=" + vrijednost + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Izbornik other = (Izbornik) obj;
        if (!Objects.equals(this.labela, other.labela)) {
            return false;
        }
        if (!Objects.equals(this.vrijednost, other.vrijednost)) {
            return false;
        }
        return true;
    }

    
}
