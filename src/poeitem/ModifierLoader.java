/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poeitem;

import com.google.gson.*;
import java.io.*; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifierLoader {
    
    private final String FileNames = 
        "Sacrifice_of_the_Vaal." +
        "Elder." +
        "Shaper." +
        "Crusader." +
        "Redeemer." +
        "Hunter." +
        "Warlord." +
        "Claw." +
        "Dagger." +
        "Wand." +
        "One+Hand+Sword." +
        "Thrusting+One+Hand+Sword." +
        "One+Hand+Axe." +
        "One+Hand+Mace." +
        "Sceptre." +
        "Rune+Dagger." +
        "Bow." +
        "Staff." +
        "Two+Hand+Sword." +
        "Two+Hand+Axe." +
        "Two+Hand+Mace." +
        "FishingRod." +
        "Warstaff." +
        "BaseItemTypes&an=Crimson+Jewel." +
        "BaseItemTypes&an=Viridian+Jewel." +
        "BaseItemTypes&an=Viridian+Jewel." +
        "BaseItemTypes&an=Prismatic+Jewel." +
        "BaseItemTypes&an=Murderous+Eye+Jewel." +
        "BaseItemTypes&an=Searching+Eye+Jewel." +
        "BaseItemTypes&an=Hypnotic+Eye+Jewel." +
        "BaseItemTypes&an=Ghastly+Eye+Jewel." +
        "BaseItemTypes&an=Timeless+Jewel." +
        "BaseItemTypes&an=Large+Cluster+Jewel." +
        "BaseItemTypes&an=Medium+Cluster+Jewel." +
        "BaseItemTypes&an=Small+Cluster+Jewel." +
        "Amulet." +
        "Ring." +
        "Ring&an=unset_ring." +
        "Belt." +
        "Gloves." +
        "Boots." +
        "Body+Armour." +
        "Helmet." +
        "Quiver." +
        "Shield." +
        "LifeFlask." +
        "ManaFlask." +
        "HybridFlask." +
        "UtilityFlask." +
        "UtilityFlaskCritical.";
    
    public static void loadModifiers()
    {
        new ModifierLoader().loadModifiersFromJson();
    }
    
    private String[] getJson()
    {
        String[] lines = FileNames.split("[.]");
        String[] contents = new String[lines.length];
        for (int i=0; i<lines.length; i++)
        {
            contents[i] = contentFromTextFile("/resources/" + lines[i] + ".json");
        }
        
        return contents;
    }
    
    private void loadModifiersFromJson()
    {
        String[] json = getJson();
        JsonParser parser = new JsonParser();

        for (String string : json)
        {
            JsonObject object = parser.parse(string).getAsJsonObject();
            
            String[] influences = new String[] {"normal", "elder", "shaper", "crusader", "redeemer", "hunter", "warlord"};
            
            for (String influence : influences)
            {
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

                        m = new Modifier(ModGenerationTypeID, "Modifier", str, false);
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
                        
                        if (str != null && str.equals("1 Added Passive Skill is a Jewel Socket")) continue;

                        m = new Modifier(ModGenerationTypeID, "Modifier", str, false);
                    }
                }
            }
        }
        
        Modifier.genPseudo();
        
        String content = contentFromTextFile("/resources/clusternotables.txt");

        String[] specNotable = content.split("[.]");
        specNotable = removeDuplicates(specNotable);

        for (String s : specNotable)
        {                
            Pattern p = Pattern.compile("([PS]{1})([_]+)([a-zA-Z ]*)");
            Matcher m = p.matcher(s);

            if (m.find())
            {
                int ps = m.group(1).equals("P") ? 1 : 2;
                String mod = "1 Added Passive Skill is " + m.group(3);

                new Modifier(String.valueOf(ps), "ClusterJewelNotable", mod, false);
            }
        }
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
                Modifier modifier = new Modifier(affixType, "Crafted", cr + " [crafted]", false);
//                modifier.print();
            }
        }
    }
    
    private static void genNormalImplicits(String data)
    {
        String[] lines = data.split("[*]");
        for (String s : lines)
        {
            Modifier i = new Modifier("3", "Implicit", s, true);
//            i.print();
        }
    }
    
    private static void genSynthesisImplicits(String html) // https://pathofexile.gamepedia.com/List_of_synthesis_implicit_modifiers
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([-+()0-9a-zA-Z ]+)([\" cl]{4})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier("3", "Implicit", base, true);
        }
    }
    
    private static void genMapPrefixes(String html)
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        html = html.replaceAll("[a-zA-Z]+[|]{1}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([-+%()0-9a-zA-Z. ]+)([<b\" ]{2})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier("1", "MapMod", base, false);
        }
        
        m = Pattern.compile("([value=\\\"]{7})([-+%()0-9a-zA-Z. ]+)[<br>]{4}([-+%()0-9a-zA-Z. ]+)([\" \n]+)").matcher(html);
        while (m.find())
        {
            String base = m.group(3);
            Modifier i = new Modifier("1", "MapMod", base, false);
        }
    }
    
    private static void genMapSuffixes(String html)
    {
        html = html.replaceAll("[\\[\\]]{2}", "");
        html = html.replaceAll("[a-zA-Z]+[|]{1}", "");
        Matcher m = Pattern.compile("([value=\"]{7})([-+%()0-9a-zA-Z. ]+)([<b\" ]{2})").matcher(html);
        
        while (m.find())
        {
            String base = m.group(2);
            Modifier i = new Modifier("2", "MapMod", base, false);
        }
        
        m = Pattern.compile("([value=\\\"]{7})([-+%()0-9a-zA-Z. ]+)[<br>]{4}([-+%()0-9a-zA-Z. ]+)([\" \n]+)").matcher(html);
        while (m.find())
        {
            String base = m.group(3);
            Modifier i = new Modifier("2", "MapMod", base, false);
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
            Modifier i = new Modifier("3", "Implicit", base, true);
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
