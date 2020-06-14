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
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<Modifier>();
    public static ArrayList<Modifier> AllImplicitModifiers = new ArrayList<Modifier>();
    
    private int ModGenerationTypeID; // 1 = prefix, 2 = suffix
    private String CorrectGroup;
    private String str;
    
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
    
    public void print()
    {
        int ModGenerationTypeIDDisplay = ModGenerationTypeID;
        if (ModGenerationTypeID == 4) ModGenerationTypeIDDisplay = 1;
        else if (ModGenerationTypeID == 5) ModGenerationTypeIDDisplay = 2;
        System.out.printf("%-5s %-20s %-50s", ModGenerationTypeIDDisplay, CorrectGroup, str);
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
//                System.out.println(true);
                return m;
            }
        }
        
        return null;
    }
    
    public Modifier dupe()
    {
        return new Modifier(String.valueOf(this.getModGenerationTypeID()), this.CorrectGroup, this.str, false);
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, boolean isImplicit)
    {
        try {
            this.ModGenerationTypeID = Integer.valueOf(ModGenerationTypeID);
        } catch (NumberFormatException e) {
            System.out.println("Threw a NumberFormatException");
            return;
        }
        this.CorrectGroup = CorrectGroup;
        
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
                new Modifier(ModGenerationTypeID, CorrectGroup, jointModifier[i], isImplicit);
            }
        }
        
        str = removeRolls(str);
        this.str = str;
        
        if (!isImplicit)
        {
//            if (CorrectGroup.equals("Crafted")) System.out.println(str);
            for (Modifier explModifier : AllExplicitModifiers)
            {
                if (explModifier.str.equals(this.str))
                {
                    if (this.CorrectGroup.equals("Crafted"))
                    {
                        if (explModifier.CorrectGroup.equals("Crafted"))
                        {
//                            System.out.println(str);
                            return;
                        }
                    }
                    else
                    {
//                        System.out.println(str);
                        return;
                    }
                }
            }
        }
        else
            for (Modifier implModifier : AllImplicitModifiers)
                if (implModifier.str.equals(this.str))
                    return;
        
        int count = str.length() - str.replaceAll("#", "").length();
        rolls = new double[count];
        
        if (!isImplicit)
        {
            AllExplicitModifiers.add(this);
        }
        else
        {
            AllImplicitModifiers.add(this);
        }
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, String[] pseudoSupportedModifiersStrs)
    {
        this(ModGenerationTypeID, CorrectGroup, str, false);
        
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
        
        new Modifier("0", "Total", "Energy Shield: #", false);
        new Modifier("0", "Total", "Evasion Rating: #", false);
        new Modifier("0", "Total", "Armour: #", false);
        
        new Modifier("-2", "Pseudo", "# Empty Suffix Modifiers", false);
        new Modifier("-2", "Pseudo", "# Empty Prefix Modifiers", false);
        
        new Modifier("-3", "Base", "Quality: +#%", false);
        new Modifier("-3", "Base", "Critical Strike Chance: #%", false);
        new Modifier("-3", "Base", "Attacks per Second: #", false);
        new Modifier("-3", "Base", "Weapon Range: #", false);
        new Modifier("-3", "Base", "Level: #", false);
        new Modifier("-3", "Base", "Dex: #", false);
        new Modifier("-3", "Base", "Str: #", false);
        new Modifier("-3", "Base", "Int: #", false);
        new Modifier("-3", "Base", "Map Tier: #", false);
        new Modifier("-3", "Base", "Item Level: #", false);
        new Modifier("-3", "Base", "Item Quantity: +#%", false);
        new Modifier("-3", "Base", "Item Rarity: +#%", false);
        new Modifier("-3", "Base", "Monster Pack Size: +#%", false);
        new Modifier("-3", "Base", "Item Level: #", false);
        
        new Modifier("5", "Crafted", "Prefixes Cannot Be Changed [crafted]", false);
        new Modifier("4", "Crafted", "Suffixes Cannot Be Changed [crafted]", false);
        new Modifier("5", "Crafted", "Can have up to 3 Crafted Modifiers [crafted]", false);
        new Modifier("5", "Crafted", "Cannot roll Attack Modifiers [crafted]", false);
        new Modifier("5", "Crafted", "Cannot roll Caster Modifiers [crafted]", false);
        
        new Modifier("-3", "FlaskBase", "Recovers # Life over # Seconds", false);
        new Modifier("-3", "FlaskBase", "Recovers # Mana over # Seconds", false);
        new Modifier("-3", "FlaskBase", "Consumes # of # Charges on use", false);
        new Modifier("-3", "FlaskBase", "Currently has # Charges", false);
        new Modifier("-3", "FlaskBase", "Lasts # Seconds", false);
    }
}
