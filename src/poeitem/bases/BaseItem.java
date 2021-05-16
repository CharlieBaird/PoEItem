package poeitem.bases;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import poeitem.Tag;

public class BaseItem implements Serializable {

    public static ArrayList<BaseItem> BaseItems;
    
    private String item_class;
    private String name;
    private ArrayList<Tag> tags;
    private CraftGroup craftGroup;
    private ArrayList<Affliction> afflictions;

    public BaseItem(String item_class, String name, ArrayList<Tag> tags) {
        this.item_class = item_class;
        this.name = name;
        this.tags = tags;
        
        switch (item_class)
        {
            case "Abyss Jewel":
                craftGroup = CraftGroup.ABYSS_JEWEL;
                break;
            case "Jewel":
                craftGroup = CraftGroup.JEWEL;
                break;
            case "Cluster Jewel":
                craftGroup = CraftGroup.CLUSTER_JEWEL;
                afflictions = new ArrayList<>();
                break;
            case "UtilityFlask":
            case "HybridFlask":
            case "LifeFlask":
            case "ManaFlask":
            case "UtilityFlaskCritical":
                craftGroup = CraftGroup.FLASK;
                break;
            default:
                craftGroup = CraftGroup.NORMAL;
                break;
        }
    }

    public String getItem_class() {
        return item_class;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }
    
    public CraftGroup getCraftGroup() {
        return craftGroup;
    }

    public ArrayList<Affliction> getAfflictions() {
        return afflictions;
    }
    
    public static BaseItem getBaseItemFromName(String name)
    {
        for (BaseItem baseItem : BaseItems)
        {
            if (baseItem.getName().equals(name))
            {
                return baseItem;
            }
        }
        
        return null;
    }
    
    public void print()
    {
        System.out.println(item_class);
        System.out.println(name);
        for (Tag t : tags)
        {
            System.out.print(t.getTagName() + ", ");
        }
        if (this.getAfflictions() != null)
        {
            System.out.println("Afflictions:");
            for (Affliction aff : this.getAfflictions())
            {
                aff.print();
            }
        }
        System.out.println();
        System.out.println();
    }
    
    public static void parse(JsonObject base_items) {
        BaseItems = new ArrayList<>();
        Set<String> modKeys = base_items.keySet();
        ItemClass.ItemClasses = new ArrayList<>();
        for (String key : modKeys)
        {
            JsonObject base_item = base_items.get(key).getAsJsonObject();
            
            // Only accept entries of the following domains.
            String domain = base_item.get("domain").getAsString();
            if (!domain.equals("item") && !domain.equals("affliction_jewel") && !domain.equals("misc") && !domain.equals("abyss_jewel") && !domain.equals("flask"))
            {
                continue;
            }
            
            // Override "Jewel" for cluster jewels to be "Cluster Jewel"
            // Override "AbyssJewel" to be "Jewel"
            
            String name = base_item.get("name").getAsString();
            
            JsonArray tagsJson = base_item.get("tags").getAsJsonArray();
            ArrayList<Tag> tags = new ArrayList<>();
            for (int i=0; i<tagsJson.size(); i++)
            {
                String toTag = tagsJson.get(i).getAsString();
                if (domain.equals("flask"))
                {
                    toTag = toTag.replace("default", "default_flask");
                }

                tags.add(Tag.getTypeFromTagName(toTag));
            }
            
            String item_class = base_item.get("item_class").getAsString();
            if (domain.equals("affliction_jewel"))
            {
                item_class = "Cluster Jewel";
            }
            else if (domain.equals("abyss_jewel"))
            {
                item_class = "Abyss Jewel";
            }
            
            BaseItem baseItem = new BaseItem(item_class, name, tags);
            ItemClass itemClass = ItemClass.getFromItemClassName(item_class);
            if (itemClass != null)
            {
                itemClass.addBase(baseItem);
            }
            else
            {
                itemClass = new ItemClass(item_class);
                itemClass.addBase(baseItem);
            }
            BaseItems.add(baseItem);
        }
        
    }
    
    public static void printAll()
    {
        
        ArrayList<String> bases = new ArrayList<>();
        
        for (int i = 0; i < BaseItems.size(); i++) {
            BaseItems.get(i).print();

            if (!bases.contains(BaseItems.get(i).item_class))
            {
                bases.add(BaseItems.get(i).item_class);
//                System.out.println(BaseItems.get(i).item_class);
            }

        }
    }
    
    @Override
    public String toString()
    {
        return this.getName();
    }
}
