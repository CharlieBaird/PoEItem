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
        
    public static ArrayList<Modifier> AllExplicitModifiers = new ArrayList<>();
    public static ArrayList<Modifier> AllImplicitModifiers = new ArrayList<>();
    public static ArrayList<Modifier> AllEnchantModifiers = new ArrayList<>();
    
    public ArrayList<ModifierTier> tiers = new ArrayList<>();
    public ArrayList<BaseItem> ApplicableBases = new ArrayList<>();
    
    private int ModGenerationTypeID; // 1 = prefix, 2 = suffix
    private String CorrectGroup;
    private String str;
    private Type type;
    private Base base;
    private boolean rejected = false;
    private boolean searchable = true;

    public boolean isSearchable() {
        return searchable;
    }
    
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
        if (base != null)
            System.out.printf("%-10s %-12s %-5s %-20s %-50s", base, type, ModGenerationTypeIDDisplay, CorrectGroup, str);
        else
            System.out.printf("%-12s %-5s %-20s %-50s", type, ModGenerationTypeIDDisplay, CorrectGroup, str);
        
        if (rolls != null)
            for (double d : rolls)
                System.out.print(d + " ");
        System.out.println();
        if (tiers.size() >= 1)
                for (ModifierTier t : tiers)
                    t.print();
        
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
        return new Modifier(String.valueOf(this.getModGenerationTypeID()), this.CorrectGroup, this.str, this.type, true);
    }
    
    public void addToBase(Base... bases)
    {
        if (this.rejected) return;
        for (Base b : bases)
        {
            BaseItem bi = BaseItem.getFromBase(b);
            bi.assocModifiers.add(this);
        }
    }
    
    public void addToAllBasesExcept(Base... bases)
    {
        if (this.rejected) return;
        for (Base b : Base.values())
        {
            boolean breakout = false;
            for (Base testBase : bases)
                if (testBase == b)
                {
                    breakout = true;
                    break;
                }
            
            if (breakout) continue;
            
            BaseItem bi = BaseItem.getFromBase(b);
            bi.assocModifiers.add(this);
        }
    }
    
    public ModifierTier[] getTiersWithLevel(int maxLevel)
    {
        ArrayList<ModifierTier> tiersAL = new ArrayList<>();
        for (ModifierTier tier : tiers)
        {
            if (tier.getItemLevel() <= maxLevel)
            {
                tiersAL.add(tier);
            }
        }
        
        ModifierTier[] tiersArr = new ModifierTier[tiersAL.size()];
        for (int i=0; i<tiersAL.size(); i++)
        {
            tiersArr[i] = tiersAL.get(i);
        }
        
        return tiersArr;
    }
    
    public int getHighestHittableTier(int maxLevel)
    {
        for (int i=0; i<tiers.size(); i++)
        {
            if (tiers.get(i).getItemLevel() > maxLevel)
            {
                return tiers.size() - i + 1;
            }
        }
        
        return 1;
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, Type type, String tierName, String base, int itemLevel)
    {
        this(ModGenerationTypeID, CorrectGroup, str, type, true);
        
        str = str.replaceAll("<span class='mod-value'>", "");
        str = str.replaceAll("</span>", "");
        str = str.replaceAll("&ndash;", "-");
        str = str.replaceAll("[\\(\\)]", "");
        str = str.replaceAll("<br/>", "<br>");
        
        String[] multiple = str.split("<br>");
        for (int i=0; i<multiple.length; i++)
        {
            String s = multiple[i];
            
            Modifier other = new Modifier(ModGenerationTypeID, CorrectGroup, s, type, false);
            other.base = BaseItem.BaseItemKey.get(base);
            BaseItem b = BaseItem.getFromBase(other.base);
            
            Modifier existing = b.getExplicitFromStr(removeRolls(s, true));
            
            if (multiple.length == 1)
            {
                if (existing == null) 
                {
                    b.assocModifiers.add(other);
                    existing = other;
                }

                ModifierTier t = new ModifierTier(tierName, s, itemLevel);
                if (!existing.tiers.contains(t)) {
                    existing.tiers.add(t);
                }
            }
        }
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, Type type, boolean addToLists)
    {
        try {
            this.ModGenerationTypeID = Integer.valueOf(ModGenerationTypeID);
        } catch (NumberFormatException e) {
            System.out.println("Threw a NumberFormatException");
            rejected = true;
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
                Modifier other = new Modifier(ModGenerationTypeID, CorrectGroup, jointModifier[i], type, addToLists);
//                other.print();
            }
        }
        
        str = removeRolls(str, true);
        this.str = str;
        
        int count = str.length() - str.replaceAll("#", "").length();
        rolls = new double[count];
        
        if (addToLists)
        {
            switch (type)
            {
                case IMPLICIT:
                    for (Modifier implModifier : AllImplicitModifiers)
                        if (implModifier.str.equals(this.str))
                        {
                            rejected = true;
                            return;
                        }
                    AllImplicitModifiers.add(this);
                    break;
                case EXPLICIT:
                case BASE:
                case TOTAL:
                case PSEUDO:
                    for (Modifier explModifier : AllExplicitModifiers)
                        if (explModifier.str.equals(this.str))
                        {
                            rejected = true;
                            return;
                        }
                    AllExplicitModifiers.add(this);
                    break;
                case CRAFT:
                    for (Modifier explModifier : AllExplicitModifiers)
                        if (explModifier.str.equals(this.str) && explModifier.getCorrectGroup().equals("Crafted"))
                        {
                            rejected = true;
                            return;
                        }
                    AllExplicitModifiers.add(this);
                    break;
                case ENCHANT:
                    for (Modifier enchModifier : AllEnchantModifiers)
                        if (enchModifier.str.equals(this.str))
                        {
                            rejected = true;
                            return;
                        }
                    AllEnchantModifiers.add(this);
                    break;
            }
        }
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, String[] pseudoSupportedModifiersStrs)
    {
        this(ModGenerationTypeID, CorrectGroup, str, Type.PSEUDO, true);
        
        pseudoSupportedModifiers = new Modifier[pseudoSupportedModifiersStrs.length];
        for (int i=0; i<pseudoSupportedModifiersStrs.length; i++)
        {
            pseudoSupportedModifiers[i] = Modifier.getExplicitFromStr(pseudoSupportedModifiersStrs[i]);
        }
    }
    
    public Modifier(String ModGenerationTypeID, String CorrectGroup, String str, Type type, boolean addToLists, boolean isSearchable)
    {
        this(ModGenerationTypeID, CorrectGroup, str, type, addToLists);
        searchable = isSearchable;
    }
        
    public static String removeRolls(String str, boolean removeDouble)
    {
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(str);
        
        ArrayList<String> toRep = new ArrayList<String>();
        
        while(m.find())
        {
            toRep.add(m.group(1));
        }
        
        for (String s : toRep)
        {
            str = str.replaceFirst(s, "#");
        }
        
        if (removeDouble)
            str = str.replaceAll("#-#", "#");
        
        return str;
    }
    
    public static ArrayList<Double> getRolls(String str)
    {
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(str);
        
        ArrayList<Double> al = new ArrayList<>();
        
        while(m.find())
        {
            al.add(Double.valueOf(m.group(1)));
        }
        
        return al;
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
        Modifier other;
        other = new Modifier("-1", "Pseudo", "+#% total Elemental Resistance", new String[]
        {
            "+#% to Cold Resistance",
            "+#% to Fire Resistance",
            "+#% to Lightning Resistance"
        });
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        other = new Modifier("-1", "Pseudo", "+#% total Resistance", new String[]
        {
            "+#% to Cold Resistance",
            "+#% to Fire Resistance",
            "+#% to Lightning Resistance",
            "+#% to Chaos Resistance"
        });
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        
        other = new Modifier("-3", "Total", "Energy Shield: #", Type.TOTAL, true);
        other.addToBase(Base.HELMET, Base.BODY_ARMOUR, Base.SHIELD, Base.GLOVES, Base.BOOTS);
        other = new Modifier("-3", "Total", "Evasion Rating: #", Type.TOTAL, true);
        other.addToBase(Base.HELMET, Base.BODY_ARMOUR, Base.SHIELD, Base.GLOVES, Base.BOOTS);
        other = new Modifier("-3", "Total", "Armour: #", Type.TOTAL, true);
        other.addToBase(Base.HELMET, Base.BODY_ARMOUR, Base.SHIELD, Base.GLOVES, Base.BOOTS);
        
        other = new Modifier("-2", "Pseudo", "# Empty Suffix Modifiers", Type.PSEUDO, true);
        other.addToAllBasesExcept();
        other = new Modifier("-2", "Pseudo", "# Empty Prefix Modifiers", Type.PSEUDO, true);
        other.addToAllBasesExcept();
        
        other = new Modifier("-3", "Base", "Quality: +#%", Type.BASE, true);
        other.addToAllBasesExcept();
        other = new Modifier("-3", "Base", "Critical Strike Chance: #%", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Attacks per Second: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Weapon Range: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Level: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Dex: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Str: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Int: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Map Tier: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Item Level: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Item Quantity: +#%", Type.BASE, true);
        other = new Modifier("-3", "Base", "Item Rarity: +#%", Type.BASE, true);
        other = new Modifier("-3", "Base", "Monster Pack Size: +#%", Type.BASE, true);
        other = new Modifier("-3", "Base", "Item Level: #", Type.BASE, true, false);
        other = new Modifier("-3", "Base", "Chance to Block: #%", Type.BASE, true, false);
        
        other = new Modifier("5", "Crafted", "Prefixes Cannot Be Changed [crafted]", Type.CRAFT, true);
        other = new Modifier("4", "Crafted", "Suffixes Cannot Be Changed [crafted]", Type.CRAFT, true);
        other = new Modifier("5", "Crafted", "Can have up to 3 Crafted Modifiers [crafted]", Type.CRAFT, true);
        other = new Modifier("5", "Crafted", "Cannot roll Attack Modifiers [crafted]", Type.CRAFT, true);
        other = new Modifier("5", "Crafted", "Cannot roll Caster Modifiers [crafted]", Type.CRAFT, true);
        
        other = new Modifier("-3", "FlaskBase", "Recovers # Life over # Seconds", Type.BASE, true, false);
        other = new Modifier("-3", "FlaskBase", "Recovers # Mana over # Seconds", Type.BASE, true, false);
        other = new Modifier("-3", "FlaskBase", "Consumes # of # Charges on use", Type.BASE, true, false);
        other = new Modifier("-3", "FlaskBase", "Currently has # Charges", Type.BASE, true, false);
        other = new Modifier("-3", "FlaskBase", "Lasts # Seconds", Type.BASE, true, false);
        
        other = new Modifier("2", "Aspect", "Grants Level # Aspect of the Avian Skill", Type.EXPLICIT, true);
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        other = new Modifier("2", "Aspect", "Grants Level # Aspect of the Crab Skill", Type.EXPLICIT, true);
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        other = new Modifier("2", "Aspect", "Grants Level # Aspect of the Spider Skill", Type.EXPLICIT, true);
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
        other = new Modifier("2", "Aspect", "Grants Level # Aspect of the Cat Skill", Type.EXPLICIT, true);
        other.addToAllBasesExcept(Base.MAP, Base.SMALL_CLUSTER_JEWEL, Base.MEDIUM_CLUSTER_JEWEL, Base.LARGE_CLUSTER_JEWEL);
    }
}
