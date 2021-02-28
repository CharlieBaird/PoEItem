package poeitem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Modifier{
    
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    
    private String key;
    private ArrayList<ModifierTier> modifierTiers;

    public Modifier(String key, ModifierTier subModifier) {
        this.key = key;
        this.modifierTiers = new ArrayList<ModifierTier>();
        this.modifierTiers.add(subModifier);
    }
    
    public Modifier(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<ModifierTier> getSubModifiers() {
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
            modifier.setTiers();
        }
    }
    
    private void setTiers()
    {
        Collections.sort(this.modifierTiers, ModifierTier.comparatorSortByKey);
        
        for (int i=0; i<this.modifierTiers.size(); i++)
        {
            ModifierTier tier = modifierTiers.get(i);
            tier.setTier(this.modifierTiers.size() - i);
        }
    }
    
    
    
    public void print()
    {
        System.out.println(key);
        for (ModifierTier sub : modifierTiers)
        {
            sub.print();
        }
        System.out.println("------------------------------------------------------------------");
    }
}
