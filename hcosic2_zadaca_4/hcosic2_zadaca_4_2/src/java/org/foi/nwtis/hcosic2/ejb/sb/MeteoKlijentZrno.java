/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.ejb.sb;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import org.foi.nwtis.hcosic2.rest.klijenti.GMKlijent;
import org.foi.nwtis.hcosic2.rest.klijenti.OWMKlijentPrognoza;
import org.foi.nwtis.hcosic2.web.podaci.Lokacija;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPrognoza;

/**
 * Klasa koja dohvaća geolokaciju i 5-dnevnu prognozu za određeno parkiralište
 * @author Hrvoje
 */
@Stateless
@LocalBean
public class MeteoKlijentZrno {

    private String apiKey;
    private String gmApiKey;
    
    /**
     * Funkcija koja postavlja vrijednost apiKey i gmApiKey koji su kasnije
     * potrebni za dohvaćanje geolokacije odnosno prognoze
     * @param apiKey apiKey koji je potreban za odhvaćanje prognoze
     * @param gmApiKey gmApiKey koj je potreban za dohvaćanje geolokacije
     */
    public void postaviKorisnickePodatke(String apiKey, String gmApiKey) {
       this.apiKey=apiKey;
       this.gmApiKey=gmApiKey;
    }
    
    /**
     * Fukcija koja dohvaća geolokaciju za određenu adresu
     * @param adresa adresa za koju se dohvaća geolokacija
     * @return 
     */
    public Lokacija dajLokaciju(String adresa) {
        GMKlijent gmk = new GMKlijent(gmApiKey);
        return gmk.getGeoLocation(adresa);
    }
    
    /**
     * Fukcija koja dohvaća 5-dnevnu meteo prognozu
     * @param id id parkirališta za koje je potrebno dohvatiti meteo prognozu
     * @param adresa adresa parkirališta za koju je potrebno dohvatiti geolokaciju
     * @return 
     */
    public MeteoPrognoza[] dajMeteoPrognoze(int id, String adresa){
        OWMKlijentPrognoza klijentPrognoza = new OWMKlijentPrognoza(apiKey);
        Lokacija l = dajLokaciju(adresa);
        MeteoPrognoza[] mp = 
                klijentPrognoza.getWeatherForecast(id, 
                l.getLatitude(), l.getLongitude());
        return mp;
    }
}
