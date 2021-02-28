package poeitem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.StatTranslations.StatTranslation;

public class ModifierTier implements Serializable, Comparable {

    private Modifier parentModifier;
    private String key;
    private String modGroup;
    private StatTranslation[] statTranslations;
    private String name;
    private int required_level;
    private Affix affix_type;
    private Stat[] ids;
    private int tier = 1;

    public ModifierTier(String key, String modGroup, StatTranslation[] statTranslations, String name, int required_level, Affix affix_type, Stat[] ids) {
        this.key = key;
        this.modGroup = modGroup;
        this.statTranslations = statTranslations;
        this.name = name;
        this.required_level = required_level;
        this.affix_type = affix_type;
        this.ids = ids;
        
        int index = Collections.binarySearch(Modifier.AllExplicitModifiers, new Modifier(modGroup), Modifier.binarySearchComparator);
        if (index >= 0 && index < Modifier.AllExplicitModifiers.size())
        {
            this.parentModifier = Modifier.AllExplicitModifiers.get(index);
            parentModifier.getSubModifiers().add(this);
        }
        else
        {
            this.parentModifier = new Modifier(modGroup, this);
            Modifier.AllExplicitModifiers.add(parentModifier);
        }
        
        Collections.sort(Modifier.AllExplicitModifiers, Modifier.binarySearchComparator);
    }
    

    public String getKey() {
        return key;
    }

    public String getModGroup() {
        return modGroup;
    }

    public StatTranslation[] getStatTranslations() {
        return statTranslations;
    }

    public String getName() {
        return name;
    }

    public int getRequired_level() {
        return required_level;
    }

    public Affix getAffix_type() {
        return affix_type;
    }

    public Stat[] getIds() {
        return ids;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    @Override
    public int compareTo(Object o) {
        ModifierTier that = (ModifierTier) o;
        return this.getKey().compareTo(that.getKey());
    }
    
    public int getNumAtEndOfKey()
    {
        String modifiedKey = getKey().replaceAll("_", "");
        Pattern p = Pattern.compile("[\\d]*$");
        Matcher m = p.matcher(modifiedKey);
        int val = -1;
        try {
            if (m.find()) val = Integer.valueOf(m.group(0));
        } catch (NumberFormatException ex) {
            val = 0;
        }
        
        return val;
    }
    
    public static Comparator<ModifierTier> comparatorSortByKey = new Comparator<ModifierTier>()
    {
        @Override
        public int compare(ModifierTier tier1, ModifierTier tier2) {
            int val1 = tier1.getNumAtEndOfKey();
            int val2 = tier2.getNumAtEndOfKey();
            
            return val1 - val2;
        }
    };
    
    public enum Affix {
        PREFIX, SUFFIX
    }
    
    public void print()
    {
        System.out.println("T" + tier + " " + name + ": iLvl " + required_level);
        for (Stat stat : ids)
        {
            stat.print();
        }
    }
}
