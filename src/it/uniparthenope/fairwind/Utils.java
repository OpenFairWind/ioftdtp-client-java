package it.uniparthenope.fairwind;

import mjson.Json;
import nz.co.fortytwo.signalk.model.SignalKModel;
import nz.co.fortytwo.signalk.model.impl.SignalKModelFactory;
import nz.co.fortytwo.signalk.util.SignalKConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class Utils {

    public static SignalKModel getSubTreeByKeys(Collection<String> pathEvents) {
        SignalKModel signalKModel=SignalKModelFactory.getInstance();
        SignalKModel temp = SignalKModelFactory.getCleanInstance();


        for (String path : pathEvents) {
            NavigableMap<String,Object> map=signalKModel.getSubMap(path);
            if (map != null) {
                boolean skipMeta=true;
                if (path.contains(SignalKConstants.dot+SignalKConstants.meta+SignalKConstants.dot)) {
                    skipMeta=false;
                }
                for(String key:map.keySet()) {
                    if ((key.contains(SignalKConstants.dot+SignalKConstants.meta+SignalKConstants.dot) && skipMeta==false) || key.contains(SignalKConstants.dot+SignalKConstants.meta+SignalKConstants.dot)==false) {
                        Object node = signalKModel.get(key);
                        if (node != null) {
                            temp.getFullData().put(key, node);
                        }
                    }
                }

            }
        }
        if (signalKModel.getFullData().isEmpty()) {
            return null;
        }
        return temp;
    }

    public static void fixSource(Json jsonDelta) {
        List<Json> updates=jsonDelta.at("updates").asJsonList();
        for(Json update:updates) {
            try {
                Json jsonSource=update.at("source");
                if (jsonSource!=null) {
                    Map<String, Json> map = jsonSource.asJsonMap();
                }
            } catch (UnsupportedOperationException ex1) {
                Json source = Json.object();
                try {
                    String sourceString = update.at("source").asString();
                    String[] parts = sourceString.split("[.]");
                    String label = parts[0];
                    String type = null;
                    source.set("label", label);
                    if (label.equals("urn:fairwind")==true) {
                        type="INTERNAL";
                    } else {
                        type = parts[1];
                        if (type.equals("NMEA0183")) {
                            String sentence = parts[2];
                            source.set("sentence", sentence);
                        }
                    }
                    source.set("type", type);
                } catch (UnsupportedOperationException ex2) {

                }
                update.set("source",source);
            }
        }

    }
}
