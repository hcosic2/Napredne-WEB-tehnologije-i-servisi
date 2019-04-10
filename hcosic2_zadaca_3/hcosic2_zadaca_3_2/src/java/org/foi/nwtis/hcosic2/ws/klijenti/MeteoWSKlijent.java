package org.foi.nwtis.hcosic2.ws.klijenti;

import org.foi.nwtis.hcosic2.ws.serveri.MeteoPodaci;
import org.foi.nwtis.hcosic2.ws.serveri.Parkiraliste;

public class MeteoWSKlijent {

    public static java.util.List<org.foi.nwtis.hcosic2.ws.serveri.MeteoPodaci> dajMinMaxTemp(int arg0, java.lang.String arg1, java.lang.String arg2) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajMinMaxTemp(arg0, arg1, arg2);
    }

    public static java.util.List<org.foi.nwtis.hcosic2.ws.serveri.Parkiraliste> dajSvaParkiralista() {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSvaParkiralista();
    }

    public static java.util.List<org.foi.nwtis.hcosic2.ws.serveri.MeteoPodaci> dajSveMeteoPodatke(int arg0, java.lang.String arg1, java.lang.String arg2) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSveMeteoPodatke(arg0, arg1, arg2);
    }

    public static MeteoPodaci dajVazeceMeteoPodatke(int arg0) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajVazeceMeteoPodatke(arg0);
    }

    public static MeteoPodaci dajZadnjeMeteoPodatke(int arg0) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajZadnjeMeteoPodatke(arg0);
    }

    public static void dodajParkiraliste(org.foi.nwtis.hcosic2.ws.serveri.Parkiraliste arg0) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        port.dodajParkiraliste(arg0);
    }

    public static Parkiraliste dohvatiPodatkeParkiraliste(int arg0) {
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.hcosic2.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dohvatiPodatkeParkiraliste(arg0);
    }
}
