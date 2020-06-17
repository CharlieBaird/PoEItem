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
import static poeitem.Modifier.getRolls;
import static poeitem.Modifier.removeRolls;

public class ModifierTier implements Comparable<ModifierTier>, Serializable {
    
    private String name;
    private double value;
    private boolean hasRolls = true;
    private int itemLevel;
    
    public int getItemLevel() {
        return itemLevel;
    }

    public boolean hasRolls() {
        return hasRolls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
    private static final Pattern[] Patterns = new Pattern[] {
        Pattern.compile("([#]{1})([^#]+)([#-]{3})"), // # to #-#
        Pattern.compile("([#-]{3})([^#]+)([#]{1})") // #-# to #
    };
    
    public ModifierTier(String name, String raw, int itemLevel) {
        this.name = name;
        this.itemLevel = itemLevel;
        
        ArrayList<Double> tierRolls = getRolls(raw);
        raw = removeRolls(raw, false);
        
        this.value = calcValue(raw, tierRolls);
        
        if (!hasRolls) return;
    }
    
    private double calcValue(String raw, ArrayList<Double> values) // precondition vlaues.size() >= 1
    {
        switch (values.size())
        {
            case 5:
            case 4: return (values.get(0) + values.get(2)) / 2.0;
            case 3:
            {
                for (int i=0; i<Patterns.length; i++)
                {
                    Pattern p = Patterns[i];
                    Matcher m = p.matcher(raw);

                    if (m.find())
                    {
                        switch (i)
                        {
                            case 1: return (values.get(0) + values.get(1)) / 2.0;
                            case 2: return (values.get(0) + values.get(2)) / 2.0;
                        }
                    }
                }
            }
            case 2:
            case 1: return (values.get(0));
            case 0:
            {
                hasRolls = false;
                return 0;
            }
        }
        
        
        System.out.println("Errored on " + raw);
        return 0;
    }
    
    @Override
    public boolean equals(Object other)
    {
        ModifierTier mt = (ModifierTier) other;
        return this.name.equals(mt.name) && this.value == mt.value;
    }
    
    public void print()
    {
        System.out.printf("%-4s %-15s %-10s\n", itemLevel, name, value);
    }

    @Override
    public int compareTo(ModifierTier other)
    {
        return (int)(this.value - other.value);
    }
}
