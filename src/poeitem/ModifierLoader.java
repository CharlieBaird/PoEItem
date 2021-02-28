package poeitem;

import com.google.gson.*;
import java.io.*; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.Modifier.Type;
import poeitem.Id;
import poeitem.StatTranslations.StatTranslation;

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
        JsonParser parser = new JsonParser();
        
        String modsString = getJson("mods.min");
        JsonObject mods = parser.parse(modsString).getAsJsonObject();
        
        String stat_translationsString = getJson("stat_translations.min");
        JsonArray stat_translations = parser.parse(stat_translationsString).getAsJsonArray();
        new StatTranslations().load(stat_translations);
//        for (StatTranslation t : StatTranslations.StatTranslations)
//        {
//            t.print();
//        }
//        for (Id id : StatTranslations.Ids)
//        {
//            id.print();
//        }
        
        Set<String> modKeys = mods.keySet();
        for (String key : modKeys)
        {
            
            JsonObject mod = mods.getAsJsonObject(key);
            
            // Weed out the weird mods: enchants, atlas, uniques mods, corrupted implicits, essence mods
            
            String generation_type = mod.get("generation_type").getAsString();
            if (generation_type.equals("unique") || generation_type.equals("corrupted") || generation_type.equals("enchantment"))
            {
                continue;
            }
            
            String domain = mod.get("domain").getAsString();
            if (domain.equals("atlas") || domain.equals("area") || domain.equals("crafted") || domain.equals("delve"))
            {
                continue;
            }
            if (domain.equals("misc") && !(generation_type.equals("prefix") || generation_type.equals("suffix")))
            {
                continue;
            }
            
            boolean is_essence_only = mod.get("is_essence_only").getAsBoolean();
            if (is_essence_only)
            {
                continue;
            }
            
            // check if all weightings are 0: the mod is disabled and cannot spawn
            
            JsonArray spawn_weights = mod.get("spawn_weights").getAsJsonArray();
            boolean passed = false;
            for (int i=0; i<spawn_weights.size(); i++)
            {
                int weight = spawn_weights.get(i).getAsJsonObject().get("weight").getAsInt();
                if (weight != 0)
                {
                    passed = true;
                }
            }
            if (!passed)
            {
                continue;
            }
                
            
            // get stat ids string
            
            JsonArray stats = mod.getAsJsonArray("stats");
            if (stats.size() >= 1)
            {
                String[] ids = new String[stats.size()];
                for (int i=0; i<stats.size(); i++)
                {
                    JsonObject stat = stats.get(i).getAsJsonObject();
                    ids[i] = stat.get("id").getAsString();
                    
                    
                }
                
                Id id = new Id(ids[0]);
                    
                int index = Collections.binarySearch(StatTranslations.Ids, id, Id.comparator);

                StatTranslation statTranslation = StatTranslations.Ids.get(index).parent;
            }
        }
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
