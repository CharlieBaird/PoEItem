package poeitem;

import com.google.gson.*;
import java.io.*; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.Modifier.Type;

public class ModifierLoader {
    
    public static void loadModifiers()
    {
        new ModifierLoader().loadModifiersFromJson();
    }
    
    private String getJson(String name)
    {
        String contents = "";
        contents = contentFromTextFile("/resources/" + name + ".json");
        
        return contents;
    }
    
    private void loadModifiersFromJson()
    {
        String mods = getJson("mods.min");
        String cluster_jewel_notables = getJson("cluster_jewel_notables.min");
        String cluster_jewels = getJson("cluster_jewels.min");
        String stat_translations = getJson("stat_translations.min");
        String stats = getJson("stats.min");
        
        System.out.println(cluster_jewel_notables);

        JsonParser parser = new JsonParser();

//        for (int i=0; i<json.length; i++)
//        {
//            String string = json[i];
//            String baseName = lines[i];
//            
//            JsonObject object = parser.parse(string).getAsJsonObject();
//            
//            String[] influences = new String[] {"normal", "elder", "shaper", "crusader", "redeemer", "hunter", "warlord"};
//            
//            int index = 0;
//            boolean isInfluenced = false;
//            for (String influence : influences)
//            {
//                if (index++ >= 1) isInfluenced = true;
//                    
//                JsonElement normalElement = object.get(influence);
//            
//                Modifier m = null;
//
//                if (normalElement.isJsonArray())
//                {
//                    JsonArray normal = normalElement.getAsJsonArray();
//                    for (int j=0; j<normal.size(); j++)
//                    {
//                        JsonObject obj = normal.get(j).getAsJsonObject();
//
//                        String ModGenerationTypeID = obj.get("ModGenerationTypeID").getAsString();
//                        String str = obj.get("str").getAsString();
//                        int itemLevel = Integer.valueOf(obj.get("Level").getAsString());
//                        
//                        String tierName = null;
//                        try {
//                             tierName = obj.get("Name").getAsString();
//                        } catch (UnsupportedOperationException ex) {
//                            // No name associated
//                        }
//                        
//                        if (tierName != null) {
//                            m = new Modifier(ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, tierName, baseName, itemLevel, isInfluenced);
//                        }
//                        else {
//                            m = new Modifier(false, ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, true);
//                        }
//                    }
//                }
//                else if (normalElement.isJsonObject())
//                {
//                    JsonObject normal = normalElement.getAsJsonObject();
//                    Set<String> keysSet = normal.keySet();
//                    for (String s : keysSet)
//                    {
//                        JsonObject obj = normal.get(s).getAsJsonObject();
//
//                        String ModGenerationTypeID = obj.get("ModGenerationTypeID").getAsString();
//                        String str = obj.get("str").getAsString();
//                        int itemLevel = Integer.valueOf(obj.get("Level").getAsString());
//                        
//                        if (str != null && (str.equals("1 Added Passive Skill is a Jewel Socket") || str.equals("<span class='mod-value'>2</span> Added Passive Skills are Jewel Sockets"))) continue;
//
//                        String tierName = null;
//                        try {
//                             tierName = obj.get("Name").getAsString();
//                        } catch (UnsupportedOperationException ex) {
//                            // No name associated
//                        }
//                        
//                        if (tierName != null) {
//                            m = new Modifier(ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, tierName, baseName, itemLevel, isInfluenced);
//                        }
//                        else {
//                            m = new Modifier(false, ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, true);
//                        }
//                    }
//                }
//            }
//        }
    }
    
    private static String contentFromTextFile(String endPath)
    {
        ModifierLoader m = new ModifierLoader();
        InputStream in = m.getClass().getResourceAsStream(endPath);
        InputStreamReader fr = null;
        try {
            fr = new InputStreamReader(in, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ModifierLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedReader br = new BufferedReader(fr);
        StringBuilder contentBuilder = new StringBuilder();

        String output;
        try {
            while ((output = br.readLine()) != null) {
                contentBuilder.append(output);
            }
        } catch (IOException ex) {
            Logger.getLogger(ModifierLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contentBuilder.toString();
    }
}
