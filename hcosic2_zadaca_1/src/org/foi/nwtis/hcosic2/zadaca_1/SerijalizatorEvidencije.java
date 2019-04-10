package org.foi.nwtis.hcosic2.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageInputStream;
import org.foi.nwtis.hcosic2.konfiguracije.Konfiguracija;

/**
 * Klasa SerijalizatorEvidencije kreira konstruktor klase u koji se prenose podaci konfiguracije.
 * Služi za serijalizaciju podataka. Izvršava serijalizaciju evidencije
 * prema zadanom intervalu Potrebno je voditi brigu o međusobnom isključivanju
 * dretvi kod pristupa evidenciji rada.
 * @author Hrvoje
 */

class SerijalizatorEvidencije extends Thread {

    private final String nazivDretve;
    private final Konfiguracija konfig;
    private boolean radi = true;

    SerijalizatorEvidencije(String nazivDretve, Konfiguracija konfig) {
        super(nazivDretve);
        this.nazivDretve = nazivDretve;
        this.konfig = konfig;
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public void run() {
        String nazivDatoteke = konfig.dajPostavku("datoteka.evidencije.rada");
        int interval = Integer.parseInt(konfig.dajPostavku("interval.za.serijalizaciju"));
        while (radi) {
            long pocetak = System.currentTimeMillis();

            System.out.println("Dretva: " + nazivDretve + "Pocetak: " + pocetak);
            File datSer = new File(nazivDatoteke);
            try {
                datSer.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            }
             
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datSer));
                synchronized(ServerSustava.dohvatiEvidenciju()){
                    //oos.writeObject(ServerSustava.dohvatiEvidenciju());
                }
                ServerSustava.dohvatiEvidenciju().dodajBrojObavljenihSerijalizacija();
                oos.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            long kraj = System.currentTimeMillis();
            long rad = kraj - pocetak;
            long cekanje = interval * 1000 - rad;

            try {
                Thread.sleep(cekanje);
            } catch (InterruptedException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

}
