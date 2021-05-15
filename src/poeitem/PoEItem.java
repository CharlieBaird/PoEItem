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
    public ArrayList<ModifierTier> explicitModifierTiers = new ArrayList<>();
    
    public boolean corrupted;
    
    public ArrayList<String> influences = new ArrayList<>();
    
    public static PoEItem createItem(String raw)
    {
        if (raw == null) return null;
        
        return new PoEItem(raw);
    }
    
    protected PoEItem(String raw)
    {
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(enchant\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(implicit\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(crafted\\)]{10})", "");
        
        
        raw = parseMods(raw);
        
//        System.out.println(raw);

        
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
        
        Matcher getInfluence = Pattern.compile("([Hunter|Shaper|Elder|Crusader|Warlord|Redeemer|Fractured|Synthesised]{5,11})([ Item]{5})").matcher(raw);
        while (getInfluence.find())
        {
            influences.add(getInfluence.group(1));
            raw = raw.replace(getInfluence.group(0)+"\n", "");
            raw = raw.replace(getInfluence.group(0), "");
        }
        
//        while(raw.contains(" (fractured)"))
//            raw = raw.replace(" (fractured)", "");
    }
    
    public static String parseMods(String mods)
    {
        String[] arr = mods.split("--------");
        
        String explicits = "";
        
        Pattern pattern = Pattern.compile("([{ ]{2})([\\w]{6})([ ]{1})");
        for (String s : arr)
        {
            Matcher m = pattern.matcher(s);
            if (m.find())
            {
                explicits = s;
                break;
            }
        }
        
        String[] explicitMods = explicits.split("([{ ]{2})");
        ArrayList<String> modLines = new ArrayList<>();
        Pattern p = Pattern.compile("[\"]{1}([\\w -']+)[\"]{1}");
        for (String s : explicitMods)
        {
            if (s.contains("Master Crafted")) continue;
            
            System.out.println(s);
            Matcher m = p.matcher(s);
            if (m.find())
                modLines.add(m.group(1));
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
    
    public void print()
    {
        System.out.println("- - - Item - - -");
        
        System.out.println(rarity + " " + customName + " " + baseType);
        if (!itemType.equals(""))
            System.out.println(itemType);
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
        if (!explicitModifierTiers.isEmpty())
        {
            System.out.println("Explicits: ");
            for (ModifierTier m : explicitModifierTiers) m.print();
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
