package poeitem;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.bases.BaseItem;
import poeitem.bases.Influence;

public class PoEItem {
    
    public String rarity = "";
    public String customName = "";
    public BaseItem baseItem = null;
    
    public String itemType = "";
    public String sparkLine = "";
    
    public String sockets = "";
    
    public ArrayList<ModifierTier> explicitModifierTiers = new ArrayList<>();
    
    public boolean corrupted;
    
    public ArrayList<String> influences = new ArrayList<>();
    
    public static PoEItem createItem(String raw)
    {
        if (raw == null) return null;
        
        return new PoEItem(raw);
    }
    
    public static PoEItem createItem(String raw, BaseItem knownBaseItem, ArrayList<Modifier> applicableExplicits)
    {
        if (raw == null) return null;
        
        return new PoEItem(raw, knownBaseItem, applicableExplicits);
    }
    
    protected PoEItem(String raw)
    {
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(enchant\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(implicit\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(crafted\\)]{10})", "");

        String[] lines = raw.split("\\r?\\n");
        
        if(raw.contains("Corrupted\n"))
        {
            corrupted = true;
            raw = raw.replace("Corrupted\n", "");
        }
                
        Matcher getRarity = getRarityPattern.matcher(raw);
        if (getRarity.find())
        {
            rarity = getRarity.group(2);
            raw = raw.replace("Rar" + getRarity.group(0)+"\n", "");
        }
        
        customName = lines[2];

        Matcher getSockets = getSocketsPattern.matcher(raw);
        if (getSockets.find())
        {
            sockets = getSockets.group(2);
            raw = raw.replace(getSockets.group(0)+"\n", "");
        }
        
        Matcher getInfluence = getInfluencePattern.matcher(raw);
        while (getInfluence.find())
        {
            influences.add(getInfluence.group(1));
            raw = raw.replace(getInfluence.group(0)+"\n", "");
            raw = raw.replace(getInfluence.group(0), "");
            lines[3] = lines[3].replace("Synthesised ", "");
        }
        
        if (rarity.equals("Rare"))
        {
            baseItem = BaseItem.getBaseItemFromName(lines[3]);
        }
        else if (rarity.equals("Magic"))
        {
            Matcher mPrefix = prefixPattern.matcher(raw);
            if (mPrefix.find())
            {
                lines[2] = lines[2].replace(mPrefix.group(3) + " ", "");
            }
            Matcher mSuffix = suffixPattern.matcher(raw);
            if (mSuffix.find())
            {
                lines[2] = lines[2].replace(" " + mSuffix.group(3), "");
            }
            baseItem = BaseItem.getBaseItemFromName(lines[2]);
        }
        
        Influence[] infs = new Influence[influences.size()];
        for (int i = 0; i < infs.length; i++) {
            infs[i] = Influence.getFromFriendly(influences.get(i));
        }
        
        ArrayList<Modifier> applicableExplicits = Modifier.getAllApplicableModifiers(baseItem, infs);
        
        genExplicits(raw, applicableExplicits);
    }
    
    private static Pattern suffixPattern = Pattern.compile("([Suffix Modifier ]{16})([\"]{1})([a-zA-Z-' ]+)([\"]{1})");
    private static Pattern prefixPattern = Pattern.compile("([Prefix Modifier ]{16})([\"]{1})([a-zA-Z-' ]+)([\"]{1})");
    private static Pattern getTier = Pattern.compile("([Tier: ]{6})([0-9]+)([)]{1})");
    private static Pattern getName = Pattern.compile("([fier \"]{6})([a-zA-Z-' ]+)([\"]{1})");
    private void genExplicits(String raw, ArrayList<Modifier> applicableExplicits)
    {
        ArrayList<String> explicits = parseMods(raw);
        
        for (int i = 0; i < explicits.size(); i++) {
            
            Matcher m = getTier.matcher(explicits.get(i));
            int tier;
            if (m.find())
                tier = Integer.valueOf(m.group(2));
            else
                tier = 1;
            
            explicits.set(i,explicits.get(i).replaceAll("([(])([0-9-.']+)([)])", ""));
            explicits.set(i,explicits.get(i).replaceAll("([0-9.]+)(?!\\))", "#"));
            
            String[] lines = explicits.get(i).split("\\r?\\n");
            
            Matcher m2 = getName.matcher(explicits.get(i));
            if (!m2.find())
                continue;
            String name = m2.group(2);
            
            ModifierTier matchingModifierTier = ModifierTier.match(applicableExplicits, name, lines, tier);
            
            if (matchingModifierTier == null)
            {
                System.out.println("Error occurred");
            }
            
            this.explicitModifierTiers.add(matchingModifierTier);
        }
    }
    
    Pattern getRarityPattern = Pattern.compile("([ity: ]{5})([a-zA-Z]+)");
    Pattern getSocketsPattern = Pattern.compile("([Sockets: ]{9})([RGBW -]+)");
    Pattern getInfluencePattern = Pattern.compile("([Hunter|Shaper|Elder|Crusader|Warlord|Redeemer|Fractured|Synthesised]{5,11})([ Item]{5})");
    protected PoEItem(String raw, BaseItem knownBaseItem, ArrayList<Modifier> applicableExplicits)
    {
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(enchant\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(implicit\\)]{10})", "");
        raw = raw.replaceAll("([\\n]{1})(.+)([ \\(crafted\\)]{10})", "");
        
        this.baseItem = knownBaseItem;

        String[] lines = raw.split("\\r?\\n");
        
        if(raw.contains("Corrupted\n"))
        {
            corrupted = true;
            raw = raw.replace("Corrupted\n", "");
        }
                
        Matcher getRarity = getRarityPattern.matcher(raw);
        if (getRarity.find())
        {
            rarity = getRarity.group(2);
            raw = raw.replace("Rar" + getRarity.group(0)+"\n", "");
        }
        
        customName = lines[2];

        Matcher getSockets = getSocketsPattern.matcher(raw);
        if (getSockets.find())
        {
            sockets = getSockets.group(2);
            raw = raw.replace(getSockets.group(0)+"\n", "");
        }
        
        Matcher getInfluence = getInfluencePattern.matcher(raw);
        while (getInfluence.find())
        {
            influences.add(getInfluence.group(1));
            raw = raw.replace(getInfluence.group(0)+"\n", "");
            raw = raw.replace(getInfluence.group(0), "");
        }
        
        genExplicits(raw, applicableExplicits);
    }
    
    private static Pattern parseModsPattern = Pattern.compile("([{ ]{2})([\\w]{6})([ ]{1})");
    public static ArrayList<String> parseMods(String mods)
    {
        String[] arr = mods.split("--------");
        
        String explicits = "";
        
        for (String s : arr)
        {
            Matcher m = parseModsPattern.matcher(s);
            if (m.find())
            {
                explicits = s;
                break;
            }
        }
        
        String[] explicitMods = explicits.split("([{ ]{2})");
        ArrayList<String> modLines = new ArrayList<>();
        for (String s : explicitMods)
        {
            if (s.contains("Master Crafted")) continue;
            modLines.add(s);
        }
        
        return modLines;
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
        
        System.out.println(rarity + " " + customName);
        System.out.println("Base: " + baseItem.getName());
        if (!itemType.equals(""))
            System.out.println(itemType);
        if (!sockets.isEmpty()) System.out.println("Sockets: " + sockets);
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
