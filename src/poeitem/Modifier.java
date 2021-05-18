package poeitem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.Affliction;
import poeitem.bases.BaseItem;
import poeitem.bases.Influence;

public class Modifier implements Serializable {
    
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();

    public ArrayList<String> statTranslations;

    public ArrayList<String> getStatTranslations() {
        return statTranslations;
    }

    public void setStatTranslations(ArrayList<String> statTranslations) {
        this.statTranslations = statTranslations;
    }
    private String key;
    private ArrayList<ModifierTier> modifierTiers;

    public Modifier(String key, ModifierTier subModifier) {
        this.key = key;
        this.modifierTiers = new ArrayList<>();
        this.modifierTiers.add(subModifier);
    }
    
    private Modifier(ArrayList<ModifierTier> modifierTiers)
    {
        this.key = modifierTiers.get(0).getKey();
        this.modifierTiers = modifierTiers;
    }
    
    public Modifier(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<ModifierTier> getModifierTiers() {
        return modifierTiers;
    }
    
    public ModifierTier[] getModifierTiers(int itemLevel) {
        ArrayList<ModifierTier> tiers = new ArrayList<>();
        for (ModifierTier tier : modifierTiers)
        {
            if (tier.getRequired_level() <= itemLevel)
            {
                tiers.add(tier);
            }
        }
        
        ModifierTier[] tiersArr = new ModifierTier[tiers.size()];
        for (int i = 0; i < tiersArr.length; i++)
        {
            tiersArr[i] = tiers.get(i);
        }
        
        return tiersArr;
    }
    
    public static Comparator<Modifier> binarySearchComparator = new Comparator<Modifier>()
    {
        @Override
        public int compare(Modifier mod1, Modifier mod2) {
            return mod1.key.compareTo(mod2.key);
        }
    };
    
    public static void setAllTiers()
    {
        for (Modifier modifier : AllExplicitModifiers)
        {
            modifier.sortTiers();
        }
    }
    
    private void sortTiers()
    {
        Collections.sort(this.modifierTiers, ModifierTier.comparatorSortByKey);
    }
    
    public static void removeUnnecessaryStatTranslations() {
        for (int i = 0; i < Modifier.AllExplicitModifiers.size(); i++) {
            Modifier modifier = AllExplicitModifiers.get(i);
            ArrayList<String> UsedStatTranslations = new ArrayList<>();
            for (ModifierTier tier : modifier.getModifierTiers())
            {
                for (StatTranslation translation : tier.getStatTranslations())
                {
                    for (int j=0; j<translation.strings.size(); j++)
                    {
                        if (tier.matches(translation, j))
                        {
                            if (!UsedStatTranslations.contains(translation.strings.get(j)))
                                UsedStatTranslations.add(translation.strings.get(j));
                        }
                    }
                }
            }
            modifier.setStatTranslations(UsedStatTranslations);
        }
    }
    
    public boolean isInfluenced()
    {
        return getInfluence() != Influence.NORMAL;
    }
    
    public Influence getInfluence()
    {
        return this.getModifierTiers().get(0).getInfluence();
    }
    
    public static ArrayList<Modifier> getAllApplicableModifiers(BaseItem baseItem)
    {
        return getAllApplicableModifiers(baseItem, Influence.NORMAL);
    }
    
    public static ArrayList<Modifier> getAllApplicableModifiers(BaseItem baseItem, Influence... influences)
    {
        ArrayList<Modifier> modifiers = new ArrayList<>();
        ArrayList<Tag> tagsOnBaseStamp = baseItem.getTags();
        
        ArrayList<Tag> tagsOnBase = new ArrayList<>();
        for (Tag t : tagsOnBaseStamp)
        {
            tagsOnBase.add(t);
        }
        
        for (Influence inf : influences)
        {
            if (inf == Influence.NORMAL) break;
            
            int length = tagsOnBase.size();
            for (int i=0; i<length; i++)
            {
                String joined = tagsOnBase.get(i).getTagName() + inf.tagSuffix;
                Tag t = Tag.getTypeFromTagName(joined);
                if (t != null && !tagsOnBase.contains(t)) tagsOnBase.add(t);
            }
        }
        
        if (baseItem.getAfflictions() != null)
        {
            for (Affliction aff : baseItem.getAfflictions())
            {
                tagsOnBase.add(Tag.getTypeFromTagName(aff.getId()));
            }
        }
        
        // Swap default to end of arraylist
        if (tagsOnBase.contains(Tag._default))
        {
            tagsOnBase.remove(Tag._default);
            tagsOnBase.add(Tag._default);
        }
        
        for (Modifier modifier : AllExplicitModifiers)
        {
            ArrayList<ModifierTier> modifierTiers = new ArrayList<>();
            for (ModifierTier modifierTier : modifier.getModifierTiers())
            {
                if (modifierTier.isApplicable(baseItem, tagsOnBase))
                {
                    modifierTiers.add(modifierTier);
                }
            }
            
            if (modifierTiers.isEmpty())
            {
                continue;
            }
            Modifier mod = new Modifier(modifierTiers);
            mod.setStatTranslations(modifier.getStatTranslations());
            modifiers.add(mod);
        }
        
        return modifiers;
    }
    
    public void print()
    {
        System.out.println(key);
        if (this.getStatTranslations() != null)
        {
            for (String s : this.getStatTranslations())
            {
                System.out.println(s);
            }
        }
        else
        {
            System.out.println("I got nothing lol");
        }
        
        System.out.println();
        for (ModifierTier sub : modifierTiers)
        {
            sub.print();
        }
        System.out.println("------------------------------------------------------------------");
    }
    
    private String getFriendlyString()
    {
//        StatTranslation[] translations = this.getModifierTiers().get(0).getStatTranslations();
//        return translations[0].strings.get(0);
        
        StringBuilder builder = new StringBuilder("");
        for (String s : this.getStatTranslations())
        {
            builder.append(s).append("\n");
        }
        
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object that)
    {
        if (that == null) return false;
        if (!(that instanceof Modifier)) return false;
        
        Modifier other = (Modifier) that;
        
        return this.getKey().equals(other.getKey())
                && getModifierTiers().get(0).equals(other.getModifierTiers().get(0))
                && getModifierTiers().get(getModifierTiers().size()-1).equals(other.getModifierTiers().get(other.getModifierTiers().size()-1));
    }
    
    @Override
    public String toString()
    {
        return this.getFriendlyString();
    }
}
