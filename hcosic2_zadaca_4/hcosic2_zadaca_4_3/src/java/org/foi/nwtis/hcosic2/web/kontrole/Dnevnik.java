
package org.foi.nwtis.hcosic2.web.kontrole;

import java.util.Date;

/**
 * Klasa za pregled dnevnika
 * @author Hrvoje
 */

public class Dnevnik {

    private int id;
    private String sadrzaj;
    private Date vrijemeZapisa;

    public Dnevnik(int id, String sadrzaj, Date vrijemeZapisa) {
        this.id = id;
        this.sadrzaj = sadrzaj;
        this.vrijemeZapisa = vrijemeZapisa;
    }

    public int getId() {
        return id;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

    public Date getVrijemeZapisa() {
        return vrijemeZapisa;
    }
}
