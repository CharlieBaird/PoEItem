package poeitem.bases;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Set;
import poeitem.Tag;

public class BaseItem {

    public static ArrayList<BaseItem> BaseItems;
    
    private String item_class;
    private String name;
    private Tag[] tags;
    private CraftGroup craftGroup;

    public BaseItem(String item_class, String name, Tag[] tags) {
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

    public Tag[] getTags() {
        return tags;
    }
    
    public CraftGroup getCrafGroup() {
        return craftGroup;
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
            if (!domain.equals("item") && !domain.equals("affliction_jewel") && !domain.equals("misc") && !domain.equals("abyss_jewel"))
            {
                continue;
            }
            
            // Override "Jewel" for cluster jewels to be "Cluster Jewel"
            // Override "AbyssJewel" to be "Jewel"
            String item_class = base_item.get("item_class").getAsString();
            if (domain.equals("affliction_jewel"))
            {
                item_class = "Cluster Jewel";
            }
            if (domain.equals("abyss_jewel"))
            {
                item_class = "Abyss Jewel";
            }
            String name = base_item.get("name").getAsString();
            
            JsonArray tagsJson = base_item.get("tags").getAsJsonArray();
            Tag[] tags = new Tag[tagsJson.size()];
            for (int i=0; i<tagsJson.size(); i++)
            {
                tags[i] = Tag.getTypeFromTagName(tagsJson.get(i).getAsString());
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
//            BaseItems.get(i).print();

            if (!bases.contains(BaseItems.get(i).item_class))
            {
                bases.add(BaseItems.get(i).item_class);
                System.out.println(BaseItems.get(i).item_class);
            }

        }
    }
}
