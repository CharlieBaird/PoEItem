package poeitem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.Affliction;
import poeitem.bases.BaseItem;
import poeitem.bases.Influence;

public class Modifier{
    
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    
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
    
    public boolean isInfluenced()
    {
        return this.getModifierTiers().get(0).getInfluence() != Influence.NORMAL;
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
            modifiers.add(mod);
        }
        
        return modifiers;
    }
    
    public void print()
    {
        System.out.println(key);
        for (StatTranslation stat : modifierTiers.get(0).getStatTranslations())
        {
            stat.print();
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
        StatTranslation[] translations = this.getModifierTiers().get(0).getStatTranslations();
        return translations[0].strings.get(0);
        
        
    }
    
    @Override
    public String toString()
    {
        return this.getFriendlyString();
    }
}
