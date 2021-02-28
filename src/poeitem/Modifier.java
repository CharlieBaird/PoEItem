package poeitem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import poeitem.StatTranslations.StatTranslation;
import poeitem.bases.BaseItem;
import poeitem.bases.ItemClass;

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
    
    public static ArrayList<Modifier> getAllApplicableModifiers(ItemClass itemClass)
    {
        return getAllApplicableModifiers(itemClass.getBases().get(0));
    }
    
    public static ArrayList<Modifier> getAllApplicableModifiers(BaseItem baseItem)
    {
        ArrayList<Modifier> modifiers = new ArrayList<>();
        
        for (Modifier modifier : AllExplicitModifiers)
        {
            ArrayList<ModifierTier> modifierTiers = new ArrayList<>();
            for (ModifierTier modifierTier : modifier.getModifierTiers())
            {
                if (modifierTier.isApplicable(baseItem))
                {
                    modifierTiers.add(modifierTier);
                }
            }
            
            if (modifierTiers.isEmpty())
            {
                continue;
            }
            Modifier mod = new Modifier(modifierTiers);
            mod.print();
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
}
