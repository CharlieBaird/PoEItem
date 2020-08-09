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
        BaseItem.genBaseItems();
        new ModifierLoader().loadModifiersFromJson();
    }

    public static void reloadModifiers() {
        BaseItem.AllBaseItems.clear();
        BaseItem.genBaseItems();
        Modifier.AllEnchantModifiers.clear();
        Modifier.AllExplicitModifiers.clear();
        Modifier.AllImplicitModifiers.clear();
        new ModifierLoader().loadModifiersFromJson();
    }
    
    private String[] getJson()
    {
        String[] lines = Arrays.copyOf(BaseItem.BaseItemKey.keySet().toArray(), BaseItem.BaseItemKey.keySet().toArray().length, String[].class);
        String[] contents = new String[lines.length];
        for (int i=0; i<lines.length; i++)
        {
            contents[i] = contentFromTextFile("/resources/" + lines[i] + ".json");
        }
        
        return contents;
    }
    
    private void loadModifiersFromJson()
    {
        String[] lines = Arrays.copyOf(BaseItem.BaseItemKey.keySet().toArray(), BaseItem.BaseItemKey.keySet().toArray().length, String[].class);
        String[] json = getJson();
        JsonParser parser = new JsonParser();

        for (int i=0; i<json.length; i++)
        {
            String string = json[i];
            String baseName = lines[i];
            
            JsonObject object = parser.parse(string).getAsJsonObject();
            
            String[] influences = new String[] {"normal", "elder", "shaper", "crusader", "redeemer", "hunter", "warlord"};
            
            int index = 0;
            boolean isInfluenced = false;
            for (String influence : influences)
            {
                if (index++ >= 1) isInfluenced = true;
                    
                JsonElement normalElement = object.get(influence);
            
                Modifier m = null;

                if (normalElement.isJsonArray())
                {
                    JsonArray normal = normalElement.getAsJsonArray();
                    for (int j=0; j<normal.size(); j++)
                    {
                        JsonObject obj = normal.get(j).getAsJsonObject();

                        String ModGenerationTypeID = obj.get("ModGenerationTypeID").getAsString();
                        String str = obj.get("str").getAsString();
                        int itemLevel = Integer.valueOf(obj.get("Level").getAsString());
                        
                        String tierName = null;
                        try {
                             tierName = obj.get("Name").getAsString();
                        } catch (UnsupportedOperationException ex) {
                            // No name associated
                        }
                        
                        if (tierName != null) {
                            m = new Modifier(ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, tierName, baseName, itemLevel, isInfluenced);
                        }
                        else {
                            m = new Modifier(false, ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, true);
                        }
                    }
                }
                else if (normalElement.isJsonObject())
                {
                    JsonObject normal = normalElement.getAsJsonObject();
                    Set<String> keysSet = normal.keySet();
                    for (String s : keysSet)
                    {
                        JsonObject obj = normal.get(s).getAsJsonObject();

                        String ModGenerationTypeID = obj.get("ModGenerationTypeID").getAsString();
                        String str = obj.get("str").getAsString();
                        int itemLevel = Integer.valueOf(obj.get("Level").getAsString());
                        
                        if (str != null && (str.equals("1 Added Passive Skill is a Jewel Socket") || str.equals("<span class='mod-value'>2</span> Added Passive Skills are Jewel Sockets"))) continue;

                        String tierName = null;
                        try {
                             tierName = obj.get("Name").getAsString();
                        } catch (UnsupportedOperationException ex) {
                            // No name associated
                        }
                        
                        if (tierName != null) {
                            m = new Modifier(ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, tierName, baseName, itemLevel, isInfluenced);
                        }
                        else {
                            m = new Modifier(false, ModGenerationTypeID, "Modifier", str, Type.EXPLICIT, true);
                        }
                    }
                }
            }
        }
        
        new Modifier(false, "2", "Delve", "Has # Abyssal Socket", Type.EXPLICIT, true);
        
        for (int i=0; i<BaseItem.AllBaseItems.size(); i++)
        {
            Collections.sort(BaseItem.AllBaseItems.get(i).assocModifiers);
        }
        Collections.sort(Modifier.AllExplicitModifiers);
        Collections.sort(Modifier.AllEnchantModifiers);
        Collections.sort(Modifier.AllImplicitModifiers);
        
        Modifier.genPseudo();
        
        for (int i=0; i<BaseItem.AllBaseItems.size(); i++)
        {
            Collections.sort(BaseItem.AllBaseItems.get(i).assocModifiers);
        }
        Collections.sort(Modifier.AllExplicitModifiers);
        Collections.sort(Modifier.AllEnchantModifiers);
        Collections.sort(Modifier.AllImplicitModifiers);
        
        genClusterMods();
        
        String data;
        data = contentFromTextFile("/resources/corruptedimplicits.txt");
        genCorruptedImplicits(data);
        
        data = contentFromTextFile("/resources/synthesisimplicits.txt");
        genSynthesisImplicits(data);
        
        data = contentFromTextFile("/resources/normalimplicits.txt");
        genNormalImplicits(data);
        
        data = contentFromTextFile("/resources/helenacrafted.txt");
        genCrafted(data);
        
        data = contentFromTextFile("/resources/juncrafted.txt");
        genCrafted(data);
        
        data = contentFromTextFile("/resources/syndicatecrafted.txt");
        genCrafted(data);
        
        data = contentFromTextFile("/resources/mapprefixes.txt");
        genMapPrefixes(data);
        
        data = contentFromTextFile("/resources/mapsuffixes.txt");
        genMapSuffixes(data);
        
        data = contentFromTextFile("/resources/enchants.txt");
        genEnchantments(data);
        
        for (int i=0; i<BaseItem.AllBaseItems.size(); i++)
        {
            Collections.sort(BaseItem.AllBaseItems.get(i).assocModifiers);
        }
        Collections.sort(Modifier.AllExplicitModifiers);
        Collections.sort(Modifier.AllEnchantModifiers);
        Collections.sort(Modifier.AllImplicitModifiers);
    }
    
    private static void genClusterMods()
    {
        // Generate base enchantments
        String content = contentFromTextFile("/resources/clusterbases.txt");
        String[] specBase = content.split("[.]");
        specBase = removeDuplicates(specBase);
        for (String s : specBase)
        {
            Modifier cb = new Modifier(false, "0", "ClusterJewelBase", s, Type.ENCHANT, true);
            cb.addToBase(Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
            cb = new Modifier(false, "-1", "ClusterJewelBase", s, Type.EXPLICIT, true);
            cb.addToBase(Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        }
        
        Pattern p = Pattern.compile("([value=\"]{7})([^<>\\n\"]+)([\" cl|\\n]{4})");
        
        // Generate small cluster mods + notables
        
        content = contentFromTextFile("/resources/smallclustermods.txt");
        Matcher ma = Pattern.compile("([\\[]{2})([a-zA-Z]+)([|]{1})([a-zA-Z]+)([\\]]{2})").matcher(content);
        while(ma.find())
        {
            content = content.replace(ma.group(0), ma.group(4));
        }
        content = content.replaceAll("([\\[\\]]{2})", "");
        
        Matcher m = p.matcher(content);
        while (m.find())
        {
            String mod = m.group(2);
            Modifier cj = new Modifier(false, "1", "ClusterJewel", mod, Type.EXPLICIT, true);
            cj.addToBase(Base.SMALL_CLUSTER_JEWEL);
        }
        
        // Generate medium cluster mods + notables
        
        content = contentFromTextFile("/resources/mediumclustermods.txt");
        ma = Pattern.compile("([\\[]{2})([a-zA-Z]+)([|]{1})([a-zA-Z]+)([\\]]{2})").matcher(content);
        while(ma.find())
        {
            content = content.replace(ma.group(0), ma.group(4));
        }
        content = content.replaceAll("([\\[\\]]{2})", "");
        
        m = p.matcher(content);
        while (m.find())
        {
            String mod = m.group(2);
            Modifier cj = new Modifier(false, "1", "ClusterJewel", mod, Type.EXPLICIT, true);
            cj.addToBase(Base.MEDIUM_CLUSTER_JEWEL);
        }
        
        // Generate large cluster mods + notables
        
        content = contentFromTextFile("/resources/largeclustermods.txt");
        ma = Pattern.compile("([\\[]{2})([a-zA-Z]+)([|]{1})([a-zA-Z]+)([\\]]{2})").matcher(content);
        while(ma.find())
        {
            content = content.replace(ma.group(0), ma.group(4));
        }
        content = content.replaceAll("([\\[\\]]{2})", "");
        
        m = p.matcher(content);
        while (m.find())
        {
            String mod = m.group(2);
            Modifier cj = new Modifier(false, "1", "ClusterJewel", mod, Type.EXPLICIT, true);
            cj.addToBase(Base.LARGE_CLUSTER_JEWEL);
        }
    }
    
    
            
    private static void genCrafted(String data)
    {
        String[] sections = data.split("</tr>");
        for (String s : sections)
        {
            Matcher m = Pattern.compile("(<a href=\"\\/[_a-zA-Z= \"-]+>)").matcher(s);
            while (m.find())
            {
                s = s.replace(m.group(0), "");
            }
            
            m = Pattern.compile("</a>").matcher(s);
            while (m.find())
            {
                s = s.replace(m.group(0), "");
            }
            
            ArrayList<String> craft = new ArrayList<>();
            String affixType = null;
            
            m = Pattern.compile("([_Stats\\\">]{8}|[<br>]{4})([- +%()0-9a-zA-Z.']+)").matcher(s);
            while (m.find())
            {
                craft.add(m.group(2));
            }
            
            m = Pattern.compile("(field_Affix_type\">)([a-zA-Z]+)").matcher(s);
            while (m.find())
            {
                affixType = m.group(2).equals("Prefix") ? "4" : "5";
            }
            
//            System.out.println(s);
            for (String cr : craft)
            {
//                System.out.println(cr);
                Modifier modifier = new Modifier(false, affixType, "Crafted", cr + " [crafted]", Type.CRAFT, true);
//                modifier.print();
            }
        }
    }
    
    private static void genNormalImplicits(String data)
    {
        String[] lines = data.split("[*]");
        for (String s : lines)
        {
            Modifier i = new Modifier(false, "3", "Implicit", s, Type.IMPLICIT, true);
//            i.print();
        }
        
        Modifier i = new Modifier(false, "3", "Implicit", "Item sells for much more to vendors", Type.IMPLICIT, true);
    }
    
    private static void genSynthesisImplicits(String html) // https://pathofexile.gamepedia.com/List_of_synthesis_implicit_modifiers
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([-+()0-9a-zA-Z ]+)([\" cl]{4})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier(false, "3", "Implicit", base, Type.IMPLICIT, true);
        }
    }
    
    private static void genMapPrefixes(String html)
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        html = html.replaceAll("[a-zA-Z]+[|]{1}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([^\"]+)([\" class=\"tc]{11})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier(false, "1", "MapMod", base, Type.EXPLICIT, true);
            i.addToBase(Base.MAP);
        }
    }
    
    private static void genMapSuffixes(String html)
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        html = html.replaceAll("[a-zA-Z]+[|]{1}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([^\"]+)([\" class=\"tc]{11})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier(false, "2", "MapMod", base, Type.EXPLICIT, true);
            i.addToBase(Base.MAP);
        }
    }
    
    private static void genEnchantments(String html)
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        html = html.replaceAll("[a-zA-Z]+[|]{1}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([^\"]+)([\" class=\"tc]{11})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier(false, "-4", "Enchantment", base, Type.ENCHANT, true);
        }
    }
    
    private static void genCorruptedImplicits(String data)
    {
        data = data.replaceAll("[(]", "");
        data = data.replaceAll("[)]", "");
        
        Pattern p = Pattern.compile("([c|mod|]{6})([^}]+)([}}]{2})");
        Matcher m = p.matcher(data);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier(false, "3", "Implicit", base, Type.IMPLICIT, true);
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
    
    private static String[] removeDuplicates(String[] input)
    {
        ArrayList<String> arr = new ArrayList<>();
        for (String s : input)
            if (!arr.contains(s))
                arr.add(s);
        
        String[] output = new String[arr.size()];
        for (int i=0; i<output.length; i++)
        {
            output[i] = arr.get(i);
        }
        
        return output;
    }
    
}
