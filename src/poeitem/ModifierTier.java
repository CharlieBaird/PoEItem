package poeitem;

import poeitem.bases.BaseItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.ItemClass;

public class ModifierTier implements Serializable, Comparable {

    private Modifier parentModifier;
    private String key;
    private String modGroup;
    private StatTranslation[] statTranslations;
    private String name;
    private int required_level;
    private Affix affix_type;
    private Stat[] ids;
    private Weight[] weights;

    public ModifierTier(String key, String modGroup, StatTranslation[] statTranslations, String name, int required_level, Affix affix_type, Stat[] ids, Weight[] weights) {
        this.key = key;
        this.modGroup = modGroup;
        this.statTranslations = statTranslations;
        this.name = name;
        this.required_level = required_level;
        this.affix_type = affix_type;
        this.ids = ids;
        this.weights = weights;
        
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

    public Weight[] getWeights() {
        return weights;
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
    
    public boolean isApplicable(ItemClass itemClass)
    {
        return isApplicable(itemClass.getBases().get(0));
    }
    
    public boolean isApplicable(BaseItem base)
    {
        Tag[] tagsOnBase = base.getTags();
        Weight[] modTags = this.getWeights();
        
        boolean overrideDefault = false;
        
        for (int i = 0; i < tagsOnBase.length; i++) {
            Tag tag = tagsOnBase[i];
            
            for (int j = 0; j < modTags.length; j++) {
                Weight modWeight = modTags[j];
                Tag modTag = modWeight.getTag();
                
                if (tag == modTag)
                {
                    if (tag == Tag._default && modWeight.getWeight() == 0)
                    {
                        if (!overrideDefault) return false;
                    }
                    
                    else if (modWeight.getWeight() == 0)
                    {
                        return false;
                    }
                    
                    else if (modWeight.getWeight() > 0)
                    {
                        overrideDefault = true;
                    }
                }
            }
        }
        
        return true;
    }
    
    public void print()
    {
        if (!this.isApplicable(ItemClass.getFromItemClassName("Boots"))) return;
        
        System.out.println(name + ": iLvl " + required_level);
        for (Stat stat : ids)
        {
            stat.print();
        }
    }
}
