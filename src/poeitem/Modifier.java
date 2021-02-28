package poeitem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Modifier implements Serializable {

    public enum Type {
        EXPLICIT, IMPLICIT, ENCHANT, CRAFT, BASE, PSEUDO, TOTAL
    }
    
    public enum Affix {
        PREFIX, SUFFIX
    }
        
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllImplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllEnchantModifiers = new ArrayList<Modifier>();
}
