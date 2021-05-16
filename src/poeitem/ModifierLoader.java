package poeitem;

import poeitem.bases.BaseItem;
import com.google.gson.*;
import java.io.*; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import poeitem.ModifierTier.Affix;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.Affliction;
import poeitem.bases.CraftGroup;

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
        
        // Load mods string from json
        String modsString = getJson("mods.min");
        JsonObject mods = parser.parse(modsString).getAsJsonObject();
        
        // Load baseitems from json
        String base_itemsString = getJson("base_items.min");
        JsonObject base_items = parser.parse(base_itemsString).getAsJsonObject();
        BaseItem.parse(base_items);
        
        // Load translations from json
        String stat_translationsString = getJson("stat_translations.min");
        JsonArray stat_translations = parser.parse(stat_translationsString).getAsJsonArray();
        new StatTranslations().load(stat_translations);
        
        // Load cluster jewel afflictons from json
        String cluster_jewelsString = getJson("cluster_jewels.min");
        JsonObject cluster_jewels = parser.parse(cluster_jewelsString).getAsJsonObject();

        
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
            
            String type = mod.get("type").getAsString();
            CraftGroup craftGroup = CraftGroup.NORMAL;
            if (key.contains("Jewel") || type.contains("Jewel") || type.contains("AfflictionNotable") || type.contains("Flask"))
            {
                if (domain.equals("abyss_jewel"))
                {
                    craftGroup = CraftGroup.ABYSS_JEWEL;
                }
                else if (domain.equals("affliction_jewel"))
                {
                    craftGroup = CraftGroup.CLUSTER_JEWEL;
                }
                else if (domain.equals("flask"))
                {
                    craftGroup = CraftGroup.FLASK;
                }
                else
                {
                    craftGroup = CraftGroup.JEWEL;
                }
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
            Stat[] ids = new Stat[stats.size()];
            StatTranslation[] statTranslations = new StatTranslation[stats.size()];
            for (int i=0; i<stats.size(); i++)
            {
                JsonObject stat = stats.get(i).getAsJsonObject();
                ids[i] = new Stat(stat.get("id").getAsString(), stat.get("min").getAsDouble(), stat.get("max").getAsDouble());

                Id id = new Id(ids[i].getId()); // ids always has size >= 1
                int index = Collections.binarySearch(StatTranslations.Ids, id, Id.comparator);
                StatTranslation statTranslation = StatTranslations.Ids.get(index).parent;
                statTranslations[i] = statTranslation;
            }

            // Key, modGroup, statTranslations, name, required_level, affix_type, ids
            String modGroup = key.replaceAll("[_]", "");
            modGroup = modGroup.replaceAll("[\\d]*$", "");

            String name = mod.get("name").getAsString(); // Name of the mod. Example: "Athlete's"
            
            
            int required_level = mod.get("required_level").getAsInt(); // Sets item level
            Affix affix_type = generation_type.equals("prefix") ? Affix.PREFIX : Affix.SUFFIX; // Only possible remaining generation_types are prefix / suffix
            
            // Parse weightings, where the mod can spawn
            Weight[] weights = new Weight[spawn_weights.size()];
            for (int i=0; i<spawn_weights.size(); i++)
            {
                JsonObject weight = spawn_weights.get(i).getAsJsonObject();
                String tagStr = weight.get("tag").getAsString();
                if (craftGroup == CraftGroup.FLASK)
                {
                    tagStr = tagStr.replace("default", "default_flask");
                }
                Tag tag = Tag.getTypeFromTagName(tagStr);
                int weightVal = weight.get("weight").getAsInt();
                Weight w = new Weight(tag, weightVal);
                weights[i] = w;
            }
            
            ModifierTier modifierTier = new ModifierTier(key, modGroup, statTranslations, name, required_level, affix_type, ids, weights, craftGroup);
        }
        
        // Loading almost done. Adding afflictions to Large/Medium/Small cluster jewels (enchantments)
        
        for (int i=0; i<cluster_jewels.keySet().size(); i++)
        {
            JsonObject clusterSize = cluster_jewels.get((String) cluster_jewels.keySet().toArray()[i]).getAsJsonObject();
            
            String name = clusterSize.get("name").getAsString();
            BaseItem baseItem = BaseItem.getBaseItemFromName(name);
            
            JsonArray passive_skills = clusterSize.get("passive_skills").getAsJsonArray();
            for (int j = 0; j < passive_skills.size(); j++) {
                JsonObject afflictionObject = passive_skills.get(j).getAsJsonObject();
                
                String afflictionName = afflictionObject.get("name").getAsString();
                String afflictionId = afflictionObject.get("id").getAsString();
                
                Affliction affliction = new Affliction(afflictionName, afflictionId);
                
                baseItem.getAfflictions().add(affliction);
            }
        }
        
        Modifier.setAllTiers();

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