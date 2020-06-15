/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poeitem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Modifier implements Serializable {
    
    public enum Type {
        EXPLICIT, IMPLICIT, ENCHANT, CRAFT, BASE, PSEUDO, TOTAL
    }
    
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllImplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllEnchantModifiers = new ArrayList<Modifier>();
    
    private int ModGenerationTypeID; // 1 = prefix, 2 = suffix
    private String CorrectGroup;
    private String str;
    private Type type;
    
    public Modifier[] pseudoSupportedModifiers = null;
    
    public double[] rolls;

    public int getModGenerationTypeID() {
        return ModGenerationTypeID;
    }

    public void setModGenerationTypeID(int ModGenerationTypeID) {
        this.ModGenerationTypeID = ModGenerationTypeID;
    }

    public String getCorrectGroup() {
        return CorrectGroup;
    }

    public void setCorrectGroup(String CorrectGroup) {
        this.CorrectGroup = CorrectGroup;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
    
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public void print()
    {
        int ModGenerationTypeIDDisplay = ModGenerationTypeID;
        if (ModGenerationTypeID == 4) ModGenerationTypeIDDisplay = 1;
        else if (ModGenerationTypeID == 5) ModGenerationTypeIDDisplay = 2;
        System.out.printf("%-12s %-5s %-20s %-50s", type, ModGenerationTypeIDDisplay, CorrectGroup, str);
        if (rolls != null)
            for (double d : rolls)
                System.out.print(d + " ");
        System.out.println();
        
    }
    
    public static Modifier getExplicitFromStr(String str)
    {
        for (Modifier m : AllExplicitModifiers)
        {
            if (m.getStr().equals(str))
            {
                return m;
            }
        }
        
        return null;
    }
    
    public static Modifier getImplicitFromStr(String str)
    {
        for (Modifier m : AllImplicitModifiers)
        {
            if (m.getStr().equals(str))
            {
                return m;
            }
        }
        
        return null;
    }
    
    public static Modifier getEnchantFromStr(String str)
    {
        for (Modifier m : AllEnchantModifiers)
        {
            if (m.getStr().equals(str))
            {
                return m;
            }
        }
        
        return null;
    }
    
    public Modifier dupe()
    {
        return new Modifier(String.valueOf(this.getModGenerationTypeID()), this.CorrectGroup, this.str, this.type);
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, Type type)
    {
        try {
            this.ModGenerationTypeID = Integer.valueOf(ModGenerationTypeID);
        } catch (NumberFormatException e) {
            System.out.println("Threw a NumberFormatException");
            return;
        }
        this.CorrectGroup = CorrectGroup;
        this.type = type;
        
        str = str.replaceAll("<span class='mod-value'>", "");
        str = str.replaceAll("</span>", "");
        str = str.replaceAll("&ndash;", "-");
        str = str.replaceAll("[\\(\\)]", "");
        str = str.replaceAll("<br/>", "<br>");
        String[] jointModifier = str.split("<br>");
        
        if (jointModifier.length > 1)
        {
            str = jointModifier[0];
            for (int i=1; i<jointModifier.length; i++)
            {
                Modifier other = new Modifier(ModGenerationTypeID, CorrectGroup, jointModifier[i], type);
//                other.print();
            }
        }
        
        str = removeRolls(str);
        this.str = str;
        
        int count = str.length() - str.replaceAll("#", "").length();
        rolls = new double[count];
        
        switch (type)
        {
            case IMPLICIT:
                for (Modifier implModifier : AllImplicitModifiers)
                    if (implModifier.str.equals(this.str))
                        return;
                AllImplicitModifiers.add(this);
                break;
            case EXPLICIT:
            case BASE:
            case TOTAL:
            case PSEUDO:
                for (Modifier explModifier : AllExplicitModifiers)
                    if (explModifier.str.equals(this.str))
                        return;
                AllExplicitModifiers.add(this);
                break;
            case CRAFT:
                for (Modifier explModifier : AllExplicitModifiers)
                    if (explModifier.str.equals(this.str) && explModifier.getCorrectGroup().equals("Crafted"))
                        return;
                AllExplicitModifiers.add(this);
                break;
            case ENCHANT:
                for (Modifier enchModifier : AllEnchantModifiers)
                    if (enchModifier.str.equals(this.str))
                        return;
                AllEnchantModifiers.add(this);
                break;
        }
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, String[] pseudoSupportedModifiersStrs)
    {
        this(ModGenerationTypeID, CorrectGroup, str, Type.PSEUDO);
        
        pseudoSupportedModifiers = new Modifier[pseudoSupportedModifiersStrs.length];
        for (int i=0; i<pseudoSupportedModifiersStrs.length; i++)
        {
            pseudoSupportedModifiers[i] = Modifier.getExplicitFromStr(pseudoSupportedModifiersStrs[i]);
        }
    }
        
    public static String removeRolls(String str)
    {
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(str);
        
        ArrayList<String> toRep = new ArrayList<String>();
        
        while(m.find())
        {
//            System.out.print(m.group(1) + ",");
            toRep.add(m.group(1));
        }
        
        for (String s : toRep)
        {
            str = str.replaceFirst(s, "#");
        }
        
//        System.out.print("\n");
        str = str.replaceAll("#-#", "#");
        
        return str;
    }
        
    @Override
    public boolean equals(Object that)
    {
        Modifier other = (Modifier) that;
        
        if (ModGenerationTypeID == other.getModGenerationTypeID() &&
            CorrectGroup.equals(other.getCorrectGroup()) &&
            str.equals(other.getStr()))
            return true;
        
        return false;
    }
    
    public static void genPseudo()
    {
        new Modifier("-1", "Pseudo", "+#% total Elemental Resistance", new String[]
        {
            "+#% to Cold Resistance",
            "+#% to Fire Resistance",
            "+#% to Lightning Resistance"
        });
        new Modifier("-1", "Pseudo", "+#% total Resistance", new String[]
        {
            "+#% to Cold Resistance",
            "+#% to Fire Resistance",
            "+#% to Lightning Resistance",
            "+#% to Chaos Resistance"
        });
        
        new Modifier("-3", "Total", "Energy Shield: #", Type.TOTAL);
        new Modifier("-3", "Total", "Evasion Rating: #", Type.TOTAL);
        new Modifier("-3", "Total", "Armour: #", Type.TOTAL);
        
        new Modifier("-2", "Pseudo", "# Empty Suffix Modifiers", Type.PSEUDO);
        new Modifier("-2", "Pseudo", "# Empty Prefix Modifiers", Type.PSEUDO);
        
        new Modifier("-3", "Base", "Quality: +#%", Type.BASE);
        new Modifier("-3", "Base", "Critical Strike Chance: #%", Type.BASE);
        new Modifier("-3", "Base", "Attacks per Second: #", Type.BASE);
        new Modifier("-3", "Base", "Weapon Range: #", Type.BASE);
        new Modifier("-3", "Base", "Level: #", Type.BASE);
        new Modifier("-3", "Base", "Dex: #", Type.BASE);
        new Modifier("-3", "Base", "Str: #", Type.BASE);
        new Modifier("-3", "Base", "Int: #", Type.BASE);
        new Modifier("-3", "Base", "Map Tier: #", Type.BASE);
        new Modifier("-3", "Base", "Item Level: #", Type.BASE);
        new Modifier("-3", "Base", "Item Quantity: +#%", Type.BASE);
        new Modifier("-3", "Base", "Item Rarity: +#%", Type.BASE);
        new Modifier("-3", "Base", "Monster Pack Size: +#%", Type.BASE);
        new Modifier("-3", "Base", "Item Level: #", Type.BASE);
        new Modifier("-3", "Base", "Chance to Block: #%", Type.BASE);
        
        new Modifier("5", "Crafted", "Prefixes Cannot Be Changed [crafted]", Type.CRAFT);
        new Modifier("4", "Crafted", "Suffixes Cannot Be Changed [crafted]", Type.CRAFT);
        new Modifier("5", "Crafted", "Can have up to 3 Crafted Modifiers [crafted]", Type.CRAFT);
        new Modifier("5", "Crafted", "Cannot roll Attack Modifiers [crafted]", Type.CRAFT);
        new Modifier("5", "Crafted", "Cannot roll Caster Modifiers [crafted]", Type.CRAFT);
        
        new Modifier("-3", "FlaskBase", "Recovers # Life over # Seconds", Type.BASE);
        new Modifier("-3", "FlaskBase", "Recovers # Mana over # Seconds", Type.BASE);
        new Modifier("-3", "FlaskBase", "Consumes # of # Charges on use", Type.BASE);
        new Modifier("-3", "FlaskBase", "Currently has # Charges", Type.BASE);
        new Modifier("-3", "FlaskBase", "Lasts # Seconds", Type.BASE);
        
        new Modifier("2", "Aspect", "Grants Level # Aspect of the Avian Skill", Type.EXPLICIT);
        new Modifier("2", "Aspect", "Grants Level # Aspect of the Crab Skill", Type.EXPLICIT);
        new Modifier("2", "Aspect", "Grants Level # Aspect of the Spider Skill", Type.EXPLICIT);
        new Modifier("2", "Aspect", "Grants Level # Aspect of the Cat Skill", Type.EXPLICIT);
    }
}
