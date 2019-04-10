/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.rest.klijenti;

import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPodaci;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPrognoza;

/**
 *
 * @author grupa_4
 */
public class OWMKlijentPrognoza extends OWMKlijent {
    
    public OWMKlijentPrognoza(String apiKey) {
        super(apiKey);
    }
    
    public MeteoPrognoza[] getWeatherForecast(int id, String latitude, String longitude){
        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Forecast_Path());
        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);

        String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);

        try {
            JsonReader reader = Json.createReader(new StringReader(odgovor));
            JsonObject jo = reader.readObject();
            JsonArray prognoze = jo.getJsonArray("list");

            MeteoPrognoza[] mpr = new MeteoPrognoza[jo.getInt("cnt")];

            Calendar trenutni = Calendar.getInstance();
            Calendar prethodni = Calendar.getInstance();

            int dan = 1;

            for (JsonValue prognoza : prognoze) {
                int indeks = prognoze.indexOf(prognoza);
                MeteoPodaci mp = new MeteoPodaci();
                JsonObject jsonObject = (JsonObject) prognoza;

                mp.setLastUpdate(new Date(jsonObject.getJsonNumber("dt").bigDecimalValue().longValue() * 1000));

                mp.setTemperatureValue(new Double(jsonObject.getJsonObject("main").getJsonNumber("temp").doubleValue()).floatValue());
                mp.setTemperatureMin(new Double(jsonObject.getJsonObject("main").getJsonNumber("temp_min").doubleValue()).floatValue());
                mp.setTemperatureMax(new Double(jsonObject.getJsonObject("main").getJsonNumber("temp_max").doubleValue()).floatValue());
                mp.setTemperatureUnit("celsius");

                mp.setPressureValue(new Double(jsonObject.getJsonObject("main").getJsonNumber("pressure").doubleValue()).floatValue());
                mp.setPressureUnit("hPa");

                mp.setHumidityValue(new Double(jsonObject.getJsonObject("main").getJsonNumber("humidity").doubleValue()).floatValue());
                mp.setHumidityUnit("%");

                mp.setWeatherNumber(jsonObject.getJsonArray("weather").getJsonObject(0).getInt("id"));
                mp.setWeatherValue(jsonObject.getJsonArray("weather").getJsonObject(0).getString("description"));
                mp.setWeatherIcon(jsonObject.getJsonArray("weather").getJsonObject(0).getString("icon"));

                mp.setCloudsValue(jsonObject.getJsonObject("clouds").getInt("all"));

                mp.setWindSpeedValue(new Double(jsonObject.getJsonObject("wind").getJsonNumber("speed").doubleValue()).floatValue());
                mp.setWindDirectionValue(new Double(jsonObject.getJsonObject("wind").getJsonNumber("deg").doubleValue()).floatValue());

                if (indeks > 0) {
                    trenutni.setTime(mp.getLastUpdate());
                    prethodni.setTime(mpr[indeks - 1].getPrognoza().getLastUpdate());

                    if (trenutni.get(Calendar.DAY_OF_YEAR) != prethodni.get(Calendar.DAY_OF_YEAR)) {
                        dan++;
                    }
                }

                mpr[indeks] = new MeteoPrognoza(id, dan, mp);
            }

            return mpr;
        } catch (Exception ex) {
            Logger.getLogger(OWMKlijentPrognoza.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
