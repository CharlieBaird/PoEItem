/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poeitem;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author charl
 */
public class PoEItem {
    
    public String rarity = "";
    public String customName = "";
    public String baseType = "";
    
    public String itemType = "";
//    public int physicalDamage = 0;
//    public int fireDamage = 0;
//    public int coldDamage = 0;
//    public int lightningDamage = 0;
//    public int chaosDamage = 0;
    
    public String sockets = "";
    
    public ArrayList<Modifier> baseModifiers = new ArrayList<>();
    public ArrayList<Modifier> implicitModifiers = new ArrayList<>();
    public ArrayList<Modifier> explicitModifiers = new ArrayList<>();
    
    public boolean corrupted;
    
//    public String influence;
    
    public static PoEItem createItem(String raw)
    {
        if (raw == null) return null;
        else return new PoEItem(raw);
    }
    
    private PoEItem(String raw)
    {        
        raw = parseMods(raw);
        
//        System.out.println(raw);
        if(raw.contains("Corrupted"))
        {
            corrupted = true;
            raw = raw.replace("Corrupted", "");
        }
        
        Matcher getRarity = Pattern.compile("([ity: ]{5})([a-zA-Z]+)").matcher(raw);
        if (getRarity.find())
        {
            rarity = getRarity.group(2);
            raw = raw.replace("Rar" + getRarity.group(0)+"\n", "");
        }

        Matcher getSockets = Pattern.compile("([Sockets: ]{9})([RGB -]+)").matcher(raw);
        if (getSockets.find())
        {
            sockets = getSockets.group(2);
            raw = raw.replace(getSockets.group(0)+"\n", "");
        }
        
//        System.out.println(raw);
        
        String[] lines = raw.split("\\r?\\n");
        
        StringBuilder UnusedBuilder = new StringBuilder();
        
        for (int i=0; i<lines.length; i++)
        {
            String s = lines[i];
            
            ArrayList<Double> rolls = new ArrayList<>();
            
            Matcher getRoll = Pattern.compile("([*]+)([(\\d+(?:\\.\\d+)?)]+)").matcher(s);
            while (getRoll.find())
            {
                rolls.add(Double.valueOf(getRoll.group(2)));
                s = s.replace(getRoll.group(0), "");
            }
            
            Modifier m = null;
            if (!s.contains("implicit"))
            {
                s = s.replace(" (augmented)", "");
                m = Modifier.getExplicitFromStr(s);
            }
            else
            {
                 m = Modifier.getImplicitFromStr(s.replace(" (implicit)", ""));
            }
            
            for (int j=0; j<rolls.size(); j++)
            {
                try {
                    m.rolls[j] = rolls.get(j);
                } catch (NullPointerException e) {
                    System.out.println("Modifier not found: '" + s + "'");
                    return;
                }
            }
            
            if (m == null)
            {
//                System.out.println("'" + s + "'");
                UnusedBuilder.append(s).append("&");
            }
            else
            {
                switch (m.getModGenerationTypeID())
                {
                    case 1:
                    case 2:
                        explicitModifiers.add(m);
                        break;
                    case 0:
                    case -3:
                        baseModifiers.add(m);
                        break;
                    case 3:
                        implicitModifiers.add(m);
                        break;
                    default:
//                        m.print();
                        break;
                }
//                m.print();
            }
        }
        
        String[] unusedLines = UnusedBuilder.toString().split("[&]");
        
        customName = unusedLines[0];
        baseType = unusedLines[1];
        itemType = unusedLines[2];
        
        
    }
    
    public static String parseMods(String mods)
    {
        String[] arr = mods.split("\\R");
        ArrayList<String> modLines = new ArrayList<String>();
        for (String s : arr) modLines.add(s);
        
        Pattern p1a = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)$"); // 2
        Pattern p1b = Pattern.compile("^(\\d+(?:\\.\\d+)?)(\\D+)$"); // 1
        Pattern p1c = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)$"); // 2
        Pattern p2  = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)$"); // 2, 4
        
        for (int i=0; i<modLines.size(); i++)
        {
            String str = modLines.get(i);
            
            Matcher m1a = p1a.matcher(str);
            Matcher m1b = p1b.matcher(str);
            Matcher m1c = p1c.matcher(str);
            Matcher m2  = p2.matcher(str);
            
            if      (m1a.find()) str = swapHash(str, m1a.group(2));
            else if (m1b.find()) str = swapHash(str, m1b.group(1));
            else if (m1c.find()) str = swapHash(str, m1c.group(2));
            else if (m2.find())  str = swapHash(str, m2.group(2), m2.group(4));
            
            modLines.set(i,str);
            
            modLines.set(i, modLines.get(i).replace("# added passive skill is a jewel socket", "# added passive skills are jewel sockets"));
            
            if (
//                    mAllWord.find() 
//                    || modLines.get(i).contains("(crafted)")
                    modLines.get(i).contains("Physical Damage: ")
                    || modLines.get(i).contains("Elemental Damage: ")
                    || modLines.get(i).contains("Requirements")
                    || modLines.get(i).contains("--------")
                )
            {
                modLines.remove(i);
                i--;
            }
        }
        String joined = String.join(String.valueOf(((char)10)), modLines);
                
        return joined;
    }
    
    private static String swapHash(String mod, String... keys)
    {
        for (int i=0; i<keys.length; i++)
        {
            int len = keys[i].length();
            int index = mod.indexOf(keys[i]);
                        
            mod = mod.substring(0, index) + "#" + mod.substring(index+len, mod.length());
            
            // Check for other weird things
            
            index = mod.indexOf(" (augmented");
            if (index != -1)
            {
                mod = mod.substring(0, index);
            }
        }
        
        for (String s : keys)
        {
            mod += "*" + s;
        }
        
        return mod;
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
        System.out.println(rarity + " " + customName + " " + baseType);
        System.out.println(itemType);
//        System.out.println(
//                physicalDamage + " phys, " + 
//                fireDamage + " fire, " + 
//                coldDamage + " cold, " + 
//                lightningDamage + " lightning, " + 
//                chaosDamage + " chaos");
        System.out.println("Sockets: " + sockets);
        System.out.println("Base: ");
        for (Modifier m: baseModifiers) m.print();
        System.out.println("Implicits: ");
        for (Modifier m : implicitModifiers) m.print();
        System.out.println("Explicits: ");
        for (Modifier m : explicitModifiers) m.print();
        System.out.println("Corrupted: " + corrupted);
//        System.out.println("Influence: " + influence);
        System.out.println("- - - - - - - - -");
    }
}
