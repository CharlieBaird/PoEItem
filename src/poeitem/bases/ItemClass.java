package poeitem.bases;

import java.util.ArrayList;

public class ItemClass {
    
    public static ArrayList<ItemClass> ItemClasses;
    
    private String name;
    private ArrayList<BaseItem> bases;
    
    public ItemClass(String name)
    {
        if (ItemClasses == null)
        {
            ItemClasses = new ArrayList<>();
        }
        this.name = name;
        bases = new ArrayList<>();
        ItemClasses.add(this);
    }
    
    public void addBase(BaseItem baseItem)
    {
        if (baseItem.getName().contains("Talisman"))
        {
            return;
        }
        bases.add(baseItem);
    }

    public String getName() {
        return name;
    }

    public ArrayList<BaseItem> getBases() {
        return bases;
    }
    
    public static ItemClass getFromItemClassName(String itemClassName)
    {
        for (int i = 0; i < ItemClasses.size(); i++) {
            ItemClass itemClass = ItemClasses.get(i);
            if (itemClass.name.equals(itemClassName))
            {
                return itemClass;
            }
        }
        
        return null;
    }
    
    public static void printAll()
    {
        for (ItemClass itemClass : ItemClasses)
        {
            System.out.println(itemClass.name);
            for (BaseItem b : itemClass.bases)
            {
                System.out.println(b.getName());
            }
            System.out.println();
        }
    }
    
    
}
