package poeitem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Watchstone extends PoEItem {
    
    public int sextantUsesRemaining;
    
    private static Pattern usesRemainingPattern = Pattern.compile("([0-9]+)([ uses]{5})");
    
    private String parseSextantMod(String str)
    {
        Pattern p1a = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)$"); // 2
        Pattern p1b = Pattern.compile("^(\\d+(?:\\.\\d+)?)(\\D+)$"); // 1
        Pattern p1c = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)$"); // 2
        Pattern p2  = Pattern.compile("^(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)(\\d+(?:\\.\\d+)?)(\\D+)$"); // 2, 4
        
        Matcher m1a = p1a.matcher(str);
        Matcher m1b = p1b.matcher(str);
        Matcher m1c = p1c.matcher(str);
        Matcher m2  = p2.matcher(str);

        if      (m1a.find()) str = swapHash(str, m1a.group(2));
        else if (m1b.find()) str = swapHash(str, m1b.group(1));
        else if (m1c.find()) str = swapHash(str, m1c.group(2));
        else if (m2.find())  str = swapHash(str, m2.group(2), m2.group(4));
        
        return str;
    }
    
    private static String swapHash(String mod, String... keys)
    {
        for (int i=0; i<keys.length; i++)
        {
            int len = keys[i].length();
            int index = mod.indexOf(keys[i]);
                        
            mod = mod.substring(0, index) + "#" + mod.substring(index+len, mod.length());
            
        }
        
        return mod;
    }
    
    protected Watchstone(String raw)
    {
        String[] lines = raw.split("\\r?\\n");
        
        Matcher getRarity = Pattern.compile("([ity: ]{5})([a-zA-Z]+)").matcher(raw);
        if (getRarity.find())
        {
            rarity = getRarity.group(2);
        }
        
        switch (rarity)
        {
            case "Normal":
                customName = "";
                baseType = lines[1];
                break;
            case "Unique":
                customName = lines[1];
                baseType = lines[2];
                break;
        }
        
        StringBuilder sextantMod = new StringBuilder("");
        int index = 0;
        for (String line : lines)
        {
            if (!line.contains(" (enchant)")) continue;
            
            Matcher m = usesRemainingPattern.matcher(line);
            if (m.find())
            {
                sextantUsesRemaining = Integer.valueOf(m.group(1));
                break;
            }
            
            if (index++ >= 1) sextantMod.append("\n");
            
            line = line.replace(" (enchant)", "");
            
            line = parseSextantMod(line);
            
            sextantMod.append(line);
        }
        
        Modifier m = SextantModifierFromStr(sextantMod.toString());
        
        if (m != null) this.explicitModifiers.add(m);
    }
    
    private Modifier SextantModifierFromStr(String str)
    {
        for (Modifier m : BaseItem.getFromBase(Base.WATCHSTONE).assocModifiers)
        {
            if (sextantModsEqual(m, str))
            {
                return m;
            }
        }
        
        return null;
    }
    
    @Override
    public void print()
    {
        System.out.println("- - - Item - - -");
        
        System.out.println(rarity + " " + customName + " " + baseType);
        if (!itemType.equals(""))
            System.out.println(itemType);
        if (!explicitModifiers.isEmpty())
        {
            System.out.println("Explicits: ");
            for (Modifier m : explicitModifiers) m.print();
        }
        
        if (!sparkLine.isEmpty())
        {
            System.out.println(sparkLine);
        }
        
        System.out.println("- - - - - - - - -");
    }
    
    public boolean sextantModsEqual(Modifier a, String b)
    {
        String[] aMods = a.getStr().split("\n");
        String[] bMods = b.split("\n");
        
        if (aMods.length != bMods.length) return false;
        
        for (String aStr : aMods)
        {
            boolean pass = false;
            for (String bStr : bMods)
            {
                if (aStr.equals(bStr))
                    pass = true;
            }
            
            if (!pass) return false;
        }
        
        return true;
    }
}
