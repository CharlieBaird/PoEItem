package poeitem;

import java.io.Serializable;
import java.util.ArrayList;
import poeitem.StatTranslations.StatTranslation;

public class Modifier implements Serializable {
    
    public enum Affix {
        PREFIX, SUFFIX
    }
        
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllImplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllEnchantModifiers =  new ArrayList<Modifier>();
    
    public Modifier(String key, String modGroup, StatTranslation[] statTranslations,
            String name, int required_level, Affix affix_type, Stat[] ids)
    {
        
    }
}
