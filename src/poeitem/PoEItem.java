package poeitem;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PoEItem {
    
    public String rarity = "";
    public String customName = "";
    public String baseType = "";
    
    public String itemType = "";
    public String sparkLine = "";
    
    public String sockets = "";
    
    public ArrayList<Modifier> baseModifiers = new ArrayList<>();
    public ArrayList<Modifier> enchantModifiers = new ArrayList<>();
    public ArrayList<Modifier> implicitModifiers = new ArrayList<>();
    public ArrayList<Modifier> explicitModifiers = new ArrayList<>();
    
    public boolean corrupted;
    
    public ArrayList<String> influences = new ArrayList<>();
    
    public static PoEItem createItem(String raw)
    {
//        if (raw == null
//            || raw.contains("Rarity: Unique")
//            || raw.contains("Rarity: Currency")
//            || raw.contains("Map Tier: ")
//            || raw.contains("Rarity: Divination Card")
//            || raw.contains()
//            ) return null;
        if (raw == null) return null;
        
        if (!raw.contains("Rarity: Magic") && !raw.contains("Rarity: Rare"))
        {
            if (raw.contains("Watchstone"))
                return new Watchstone(raw);
            else
                return null;
        }
        
        return new PoEItem(raw);
    }
    
    protected PoEItem(String... broken)
    {
        for (String s : broken) brokenModifiers.add(s);
    }
    
    public ArrayList<String> brokenModifiers = new ArrayList<>();
    
    protected PoEItem(String raw)
    {
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(enchant\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(implicit\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(crafted\\)]{10})", "");
        
        raw = parseMods(raw);
        if(raw.contains("Corrupted\n"))
        {
            corrupted = true;
            raw = raw.replace("Corrupted\n", "");
        }
                
        Matcher getRarity = Pattern.compile("([ity: ]{5})([a-zA-Z]+)").matcher(raw);
        if (getRarity.find())
        {
            rarity = getRarity.group(2);
            raw = raw.replace("Rar" + getRarity.group(0)+"\n", "");
        }

        Matcher getSockets = Pattern.compile("([Sockets: ]{9})([RGBW -]+)").matcher(raw);
        if (getSockets.find())
        {
            sockets = getSockets.group(2);
            raw = raw.replace(getSockets.group(0)+"\n", "");
        }
        
        Matcher getInfluence = Pattern.compile("([Hunter|Shaper|Elder|Crusader|Warlord|Redeemer|Fractured|Synthesized]{5,11})([ Item]{5})").matcher(raw);
        while (getInfluence.find())
        {
            influences.add(getInfluence.group(1));
            raw = raw.replace(getInfluence.group(0)+"\n", "");
            raw = raw.replace(getInfluence.group(0), "");
        }
        
        while(raw.contains(" (fractured)"))
            raw = raw.replace(" (fractured)", "");
        
        String[] lines = raw.split("\\r?\\n");
        
        StringBuilder UnusedBuilder = new StringBuilder();
        
        for (int i=0; i<lines.length; i++)
        {
            boolean skipOver = false;
            String s = lines[i];
            
            ArrayList<Double> rolls = new ArrayList<>();
            
            Matcher getRoll = Pattern.compile("([@]+)([(\\d+(?:\\.\\d+)?)]+)").matcher(s);
            while (getRoll.find())
            {
                rolls.add(Double.valueOf(getRoll.group(2)));
                s = s.replaceFirst(getRoll.group(0), "");
            }
            
            s = s.replace(" (augmented)", "");
            s = s.replace(" (Resistance Modifiers)", "");
            s = s.replace(" (Caster Modifiers)", "");
            s = s.replace(" (Elemental Damage)", "");
            s = s.replace(" (Attribute Modifiers)", "");
            s = s.replace(" (Attack Modifiers)", "");
            s = s.replace(" (Life and Mana Modifiers)", "");
            s = s.replace(" (Defence Modifiers)", "");
            s = s.replace(" (unmet)", "");
            
            Modifier m = Modifier.getExplicitFromStr(s);
            
            for (int j=0; j<rolls.size(); j++)
            {
                try {
                    m.rolls[j] = rolls.get(j);
                } catch (NullPointerException e) {
                    System.out.println("Modifier not found: '" + s + "'");
                    brokenModifiers.add(s);
                    skipOver = true;
                }
            }
            
            if (skipOver)
            {
                continue;
            }
            
            if (m == null)
            {
                UnusedBuilder.append(s).append("&");
            }
            else
            {
                if (m.getStr().equals("+#% to all Elemental Resistances"))
                {
                    Modifier fire = Modifier.getExplicitFromStr("+#% to Fire Resistance").dupe();
                    Modifier cold = Modifier.getExplicitFromStr("+#% to Cold Resistance").dupe();
                    Modifier ligh = Modifier.getExplicitFromStr("+#% to Lightning Resistance").dupe();
                    fire.setCorrectGroup("Suggested");
                    cold.setCorrectGroup("Suggested");
                    ligh.setCorrectGroup("Suggested");
                    fire.setModGenerationTypeID(10);
                    cold.setModGenerationTypeID(10);
                    ligh.setModGenerationTypeID(10);
                    fire.rolls = new double[] {m.rolls[0]};
                    cold.rolls = new double[] {m.rolls[0]};
                    ligh.rolls = new double[] {m.rolls[0]};
                    explicitModifiers.add(fire);
                    explicitModifiers.add(cold);
                    explicitModifiers.add(ligh);
                }
                
                switch (m.getModGenerationTypeID())
                {
                    case -1:
                    case 1:
                    case 2:
                    case 4:
                    case 5:
                        explicitModifiers.add(m);
                        break;
                    case 0:
                    case -3:
                        baseModifiers.add(m);
                        break;
                    default:
                        break;
                }
            }
        }
        
        String[] unusedLines = UnusedBuilder.toString().split("[&]");
        
        if (rarity.equals("Rare"))
        {
            customName = unusedLines[0];
            baseType = unusedLines[1];
            try {
                if (!unusedLines[2].equals("\n"))
                {
                    itemType = unusedLines[2];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                itemType = "";
            }
            try {
                sparkLine = unusedLines[3];
            } catch (ArrayIndexOutOfBoundsException e) {
                sparkLine = "";
            }
        }
        else
        {
            customName = unusedLines[0];
            try {
                if (!unusedLines[1].equals("\n"))
                {
                    itemType = unusedLines[1];
                }
            } catch (ArrayIndexOutOfBoundsException e) {}
            try {
                sparkLine = unusedLines[2];
            } catch (ArrayIndexOutOfBoundsException e) {
                sparkLine = "";
            }
        }
    }
    
    public static String parseMods(String mods)
    {
        String[] arr = mods.split("\\R");
        ArrayList<String> modLines = new ArrayList<>();
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
                
        return joined + "\n";
    }
    
    private static String swapHash(String mod, String... keys)
    {
        for (int i=0; i<keys.length; i++)
        {
            int len = keys[i].length();
            int index = mod.indexOf(keys[i]);
                        
            mod = mod.substring(0, index) + "#" + mod.substring(index+len, mod.length());
            
        }
        
        for (String s : keys)
        {
            mod += "@" + s;
        }
        
        return mod;
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
            if (type == 1 || type == 4) totalPrefixSuffix[0]++;
            else if (type == 2 || type == 5) totalPrefixSuffix[1]++;
        }
        
        return totalPrefixSuffix;
    }
    
    public void print()
    {
        System.out.println("- - - Item - - -");
        
        System.out.println(rarity + " " + customName + " " + baseType);
        if (!itemType.equals(""))
            System.out.println(itemType);
//        System.out.println(
//                physicalDamage + " phys, " + 
//                fireDamage + " fire, " + 
//                coldDamage + " cold, " + 
//                lightningDamage + " lightning, " + 
//                chaosDamage + " chaos");
        if (!sockets.isEmpty()) System.out.println("Sockets: " + sockets);
        System.out.println("Base: ");
        for (Modifier m: baseModifiers) m.print();
        if (!enchantModifiers.isEmpty())
        {
            System.out.println("Enchants: ");
            for (Modifier m : enchantModifiers) m.print();
        }
        if (!implicitModifiers.isEmpty())
        {
            System.out.println("Implicits: ");
            for (Modifier m : implicitModifiers) m.print();
        }
        if (!explicitModifiers.isEmpty())
        {
            System.out.println("Explicits: ");
            for (Modifier m : explicitModifiers) m.print();
        }
        System.out.println("Corrupted: " + corrupted);
        
        if (!influences.isEmpty())
        {
            System.out.println("Influence: ");
            for (String m: influences) System.out.println("- " + m);
        }
        
        if (!sparkLine.isEmpty())
        {
            System.out.println(sparkLine);
        }
        
        System.out.println("- - - - - - - - -");
    }
}
