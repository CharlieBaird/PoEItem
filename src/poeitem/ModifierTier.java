package poeitem;

import poeitem.bases.BaseItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.CraftGroup;
import poeitem.bases.Influence;

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
    private CraftGroup craftGroup;
    private Influence influence;

    public ModifierTier(String key, String modGroup, StatTranslation[] statTranslations, String name, 
            int required_level, Affix affix_type, Stat[] ids, Weight[] weights, CraftGroup craftGroup) {
        this.key = key;
        this.modGroup = modGroup;
        this.statTranslations = statTranslations;
        this.name = name;
        this.required_level = required_level;
        this.affix_type = affix_type;
        this.ids = ids;
        this.weights = weights;
        this.craftGroup = craftGroup;
        
        int index = Collections.binarySearch(Modifier.AllExplicitModifiers, new Modifier(modGroup), Modifier.binarySearchComparator);
        if (index >= 0 && index < Modifier.AllExplicitModifiers.size())
        {
            this.parentModifier = Modifier.AllExplicitModifiers.get(index);
            parentModifier.getModifierTiers().add(this);
        }
        else
        {
            this.parentModifier = new Modifier(modGroup, this);
            Modifier.AllExplicitModifiers.add(parentModifier);
        }
        
        switch (name)
        {
            case "of Shaping":
            case "The Shapers's":
                this.influence = Influence.SHAPER;
                break;
            case "Eldritch":
            case "of the Elder":
                this.influence = Influence.ELDER;
                break;
            case "Crusader's":
            case "of the Crusade":
                this.influence = Influence.CRUSADER;
                break;
            case "Redeemer's":
            case "of the Redeemer":
                this.influence = Influence.REDEEMER;
                break;
            case "Hunter's":
            case "of the Hunt":
                this.influence = Influence.HUNTER;
                break;
            case "Warlord's":
            case "of the Conquest":
                this.influence = Influence.WARLORD;
                break;
            default:
                this.influence = Influence.NORMAL;
                break;
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

    public CraftGroup getCraftGroup() {
        return craftGroup;
    }

    public Influence getInfluence() {
        return influence;
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
    
    public boolean isApplicable(BaseItem base, ArrayList<Tag> tagsOnBase)
    {
        Weight[] modTags = this.getWeights();
        
        boolean overrideDefault = false;
        
        // XNOR Gate to check if either both the base and the mod are jewel based or both are not jewel based
        if (this.craftGroup != base.getCraftGroup())
        {
            return false;
        }
        
        if (this.getName().equals("of Warding"))
        {
            System.out.println();
        }
        
        for (int i = 0; i < tagsOnBase.size(); i++) {
            Tag tag = tagsOnBase.get(i);
            
            for (int j = 0; j < modTags.length; j++) {
                Weight modWeight = modTags[j];
                Tag modTag = modWeight.getTag();
                
                if (tag == modTag)
                {
                    if ((tag == Tag._default || tag == Tag._default_flask) && modWeight.getWeight() == 0)
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
        System.out.println(name + ": iLvl " + required_level);
        for (Stat stat : ids)
        {
            stat.print();
        }
    }
    
    @Override
    public String toString()
    {
        return this.getName();
    }
    
    public static ModifierTier match(ArrayList<Modifier> applicableModifiers, String name, String[] words, int tier) // words starts at i=1
    {
        for (Modifier m : applicableModifiers)
        {
            for (int i=0; i<m.getModifierTiers().size(); i++)
            {
                ModifierTier mt = m.getModifierTiers().get(i);
                if (mt.getName().equals(name))
                {
                    int mtTier = m.getModifierTiers().size() - i;
                    if (mtTier == tier)
                    {
                        for (StatTranslation s : mt.statTranslations)
                        {
                            if (s.strings.contains(words[1]))
                            {
                                return mt;
                            }
                            for (String st : s.strings)
                            {
                                if (st.contains(words[1]))
                                {
                                    return mt;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
