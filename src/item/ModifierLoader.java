/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package item;

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
    
    private String[] getJson()
    {
        String[] lines = FileNames.split("[.]");
        String[] contents = new String[lines.length];
        for (int i=0; i<lines.length; i++)
        {
            InputStream in = getClass().getResourceAsStream("/resources/" + lines[i] + ".json");
            InputStreamReader fr = null;
            try {
                fr = new InputStreamReader(in, "utf-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ModifierLoader.class.getName()).log(Level.SEVERE, null, ex);
            }

            BufferedReader br = new BufferedReader(fr);
            
            StringBuilder content = new StringBuilder();

            String output;
            try {
                while ((output = br.readLine()) != null) {
                    content.append(output);
                }
            } catch (IOException ex) {
                Logger.getLogger(ModifierLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            contents[i] = content.toString();
        }
        
        return contents;
    }
    
    public void loadModifiers()
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
                        String CorrectGroup = obj.get("CorrectGroup").getAsString();
                        String str = obj.get("str").getAsString();

                        m = new Modifier(ModGenerationTypeID, CorrectGroup, str);
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
                        String CorrectGroup = obj.get("CorrectGroup").getAsString();
                        String str = obj.get("str").getAsString();
                        
                        // ignore:
                        if (str != null && str.equals("1 Added Passive Skill is a Jewel Socket")) continue;
//                        System.out.println(obj.get("id").getAsString());

                        m = new Modifier(ModGenerationTypeID, CorrectGroup, str);
                    }
                }
            }
        }
        
        Modifier.genPseudo();
        
        InputStream in = getClass().getResourceAsStream("/resources/clusternotables.txt");
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

        String content = contentBuilder.toString();

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

                new Modifier(String.valueOf(ps), "ClusterJewelNotable", mod);
            }
        }
        
        
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
