/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package item;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author charl
 */
public class Item {
    
//    public String rarity;
//    public String customName;
//    public String baseType;
    
//    public String itemType;
    public int quality = 0;
//    public int physicalDamage = 0;
//    public int fireDamage = 0;
//    public int coldDamage = 0;
//    public int lightningDamage = 0;
//    public int chaosDamage = 0;
//    public double baseCrit;
//    public double baseAps;
    
//    public int levelReq;
//    public Dictionary<String, Integer> attrReqs;
    
//    public String sockets;
    
//    public int itemLevel;
    
//    public ArrayList<Modifier> implicitModifiers = new ArrayList<>();
    
    public ArrayList<Modifier> explicitModifiers = new ArrayList<>();
    
//    public boolean corrupted;
    
//    public String influence;
    
    public static Item createItem(String raw)
    {
        if (raw == null) return null;
        else return new Item(raw);
    }
    
    private Item(String raw)
    {        
//        raw = Filters.parseMods(raw);
        
        String[] lines = raw.split("\\r?\\n");
        
        int start = 0;
        if (lines[0].contains("Quality"))
        {
            quality = Integer.valueOf(lines[0].substring(lines[0].indexOf("*") + 1));
            start++;
        }
        
        for (int i=start; i<lines.length; i++)
        {
            String s = lines[i];
            
            ArrayList<Double> rolls = new ArrayList<>();
            
            Matcher getRoll = Pattern.compile("([*]+)([(\\d+(?:\\.\\d+)?)]+)").matcher(s);
            while (getRoll.find())
            {
                rolls.add(Double.valueOf(getRoll.group(2)));
                s = s.replace(getRoll.group(0), "");
            }
            
            Modifier m = Modifier.getFromStr(s);
            
            for (int j=0; j<rolls.size(); j++)
            {
                try {
                    m.rolls[j] = rolls.get(j);
                } catch (NullPointerException e) {
                    System.out.println("Modifier not found: '" + s + "'");
                    return;
                }
            }
            
            explicitModifiers.add(m);
        }
        
//        print();
    }
                
    public final String getSingleString(String inputLine, String regex)
    {
        Matcher m = Pattern.compile(regex).matcher(inputLine);
        if (m.find())
            return m.group(3);
        return null;
    }
    
    public double getValue(String s)
    {
        for (Modifier m : explicitModifiers)
        {        
            if (m.getStr().equals(s))
            {
                return m.rolls[0];
            }
        }
        return 0;
    }
    
    public int[] numPrefixSuffix()
    {
        int[] totalPrefixSuffix = new int[2];
        for (Modifier em : explicitModifiers)
        {
            int type = em.getModGenerationTypeID();
            if (type == 1) totalPrefixSuffix[0]++;
            else if (type == 2) totalPrefixSuffix[1]++;
        }
        
        return totalPrefixSuffix;
    }
    
    public final void print()
    {
        System.out.println("- - - Item - - -");
//        System.out.println(rarity + " " + customName + " " + baseType);
//        System.out.println(itemType);
        System.out.println("Quality: " + quality);
//        System.out.println(
//                physicalDamage + " phys, " + 
//                fireDamage + " fire, " + 
//                coldDamage + " cold, " + 
//                lightningDamage + " lightning, " + 
//                chaosDamage + " chaos");
//        System.out.println("BaseCrit: " + baseCrit);
//        System.out.println("BaseAps: " + baseAps);
//        System.out.println("LevelReq: " + levelReq);
//        System.out.println("AttrReqs: " + attrReqs);
//        System.out.println("Sockets: " + sockets);
//        System.out.println("ItemLevel: " + itemLevel);
//        System.out.println("Implicits: ");
//        for (Modifier m : implicitModifiers) m.print();
        System.out.println("Explicits: ");
        for (Modifier m : explicitModifiers)
        {
            m.print();
        }
//        System.out.println("Corrupted: " + corrupted);
//        System.out.println("Influence: " + influence);
        System.out.println("- - - - - - - -");
    }
}
