package org.foi.nwtis.hcosic2.zadaca_1;

/**
 * Klasa Evidencija služi za evidenciju podataka i može se serijalizirati.
 * Treba odrediti dodatne klase i varijable u koje će se pridružiti vrijednosti.
 * Potrebno je voditi brigu o međusobnom isključivanju dretvi kod pristupa
 * evidenciji rada.
 * @author Hrvoje
 */

public class Evidencija {
    private long ukupanBrojZahtjeva = 0;
    private long brojNeispravnihZahtjeva = 0;
    private long brojNedozvoljenihZahtjeva = 0;
    private long brojUspjesnihZahtjeva =  0;
    private long brojPrekinutihZahtjeva = 0;
    private long ukupnoVrijemeRadaRadnihDretvi = 0;
    private long brojObavljenihSerijalizacija = 0;

    public long getUkupanBrojZahtjeva() {
        return ukupanBrojZahtjeva;
    }

    public void setUkupanBrojZahtjeva(long ukupanBrojZahtjeva) {
        this.ukupanBrojZahtjeva = ukupanBrojZahtjeva;
    }

    public long getBrojNeispravnihZahtjeva() {
        return brojNeispravnihZahtjeva;
    }

    public void setBrojNeispravnihZahtjeva(long brojNeispravnihZahtjeva) {
        this.brojNeispravnihZahtjeva = brojNeispravnihZahtjeva;
    }

    public long getBrojNedozvoljenihZahtjeva() {
        return brojNedozvoljenihZahtjeva;
    }

    public void setBrojNedozvoljenihZahtjeva(long brojNedozvoljenihZahtjeva) {
        this.brojNedozvoljenihZahtjeva = brojNedozvoljenihZahtjeva;
    }

    public long getBrojUspjesnihZahtjeva() {
        return brojUspjesnihZahtjeva;
    }

    public void setBrojUspjesnihZahtjeva(long brojUspjesnihZahtjeva) {
        this.brojUspjesnihZahtjeva = brojUspjesnihZahtjeva;
    }

    public long getBrojPrekinutihZahtjeva() {
        return brojPrekinutihZahtjeva;
    }

    public void setBrojPrekinutihZahtjeva(long brojPrekinutihZahtjeva) {
        this.brojPrekinutihZahtjeva = brojPrekinutihZahtjeva;
    }

    public long getUkupnoVrijemeRadaRadnihDretvi() {
        return ukupnoVrijemeRadaRadnihDretvi;
    }

    public void setUkupnoVrijemeRadaRadnihDretvi(long ukupnoVrijemeRadaRadnihDretvi) {
        this.ukupnoVrijemeRadaRadnihDretvi = ukupnoVrijemeRadaRadnihDretvi;
    }

    public long getBrojObavljenihSerijalizacija() {
        return brojObavljenihSerijalizacija;
    }

    public void setBrojObavljenihSerijalizacija(long brojObavljenihSerijalizacija) {
        this.brojObavljenihSerijalizacija = brojObavljenihSerijalizacija;
    }
    public void dodajUkupanBrojZahtjeva(){
        ukupanBrojZahtjeva++;
    }
    public void dodajNeispravniZahtjev(){
        ukupanBrojZahtjeva++;
    }
    public void dodajNedozvoljeniZahtjev(){
        ukupanBrojZahtjeva++;
    }
    public void dodajUspjesniZahtjev(){
        ukupanBrojZahtjeva++;
    }
    public void dodajPrekinutiZahtjev(){
        ukupanBrojZahtjeva++;
    }
    public void dodajVrijemeRadnihDretvi(){
        ukupanBrojZahtjeva++;
    }
    public void dodajBrojObavljenihSerijalizacija(){
        ukupanBrojZahtjeva++;
    }
}
