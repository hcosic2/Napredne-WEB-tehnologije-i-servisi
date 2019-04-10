
package org.foi.nwtis.hcosic2.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;

/**
 * Klasa koja upravlja promjenom jezika na svim xhtml-ovima
 * @author Hrvoje
 */

@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {

    private String odabraniJezik;
    private Locale locale;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public Lokalizacija() {
    }

    public String getOdabraniJezik() {
        odabraniJezik = FacesContext.getCurrentInstance()
                .getViewRoot().getLocale().getLanguage();
        return odabraniJezik;
    }

    public Object odaberiJezik(String jezik){
        this.locale = new Locale(jezik);
        FacesContext.getCurrentInstance()
                .getViewRoot().setLocale(locale);
        return "";
    }
    
    public String saljiPoruku(){
        return "saljiPoruku";
    }
    
    public String pregledPoruka(){
        return "pregledPoruka";
    }
    
    public String pregledDnevnika(){
        return "pregledDnevnika";
    }
}
