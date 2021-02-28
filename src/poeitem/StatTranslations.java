package poeitem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StatTranslations {
    
    public static ArrayList<StatTranslation> StatTranslations;
    public static ArrayList<Id> Ids;
    
    public void load(JsonArray content)
    {
        StatTranslations = new ArrayList<>();
        Ids = new ArrayList<>();
        
        for (int i=0; i<content.size(); i++)
        {
            StatTranslation statTranslation = new StatTranslation();
            JsonObject stat_translation = content.get(i).getAsJsonObject();
            JsonArray english = stat_translation.get("English").getAsJsonArray();
            for (int j=0; j<english.size(); j++)
            {
                JsonArray format = english.get(j).getAsJsonObject().get("format").getAsJsonArray();
                statTranslation.formats = new String[format.size()];
                for (int k=0; k<format.size(); k++)
                {
                    statTranslation.formats[k] = format.get(k).getAsString();
                }
                
                String string = english.get(j).getAsJsonObject().get("string").getAsString();
                statTranslation.strings.add(string);
            }
            
            JsonArray ids = stat_translation.get("ids").getAsJsonArray();
            String[] idsString = new String[ids.size()];
            for (int k=0; k<ids.size(); k++)
            {
                idsString[k] = ids.get(k).getAsString();
            }
            statTranslation.setIds(idsString);
            
            StatTranslations.add(statTranslation);
        }
        
        Collections.sort(Ids);
    }
    
    protected class StatTranslation
    {
        
        ArrayList<String> strings;
        String[] formats;
        Id[] ids;
                
        public StatTranslation()
        {
            strings = new ArrayList<>();
        }
        
        public void print()
        {
            for (String str : strings)
            {
                System.out.println(str);
            }
            for (String str : formats)
            {
                System.out.println(str);
            }
            for (Id id : ids)
            {
                System.out.println(id.str);
            }
            System.out.println();
        }

        private void setIds(String[] idsString) {
            ids = new Id[idsString.length];
            for (int i=0; i<idsString.length; i++)
            {
                Id id = new Id(idsString[i], this);
                ids[i] = id;
                Ids.add(id);
            }
        }
    }
}

class Id implements Comparable
{
    String str;
    StatTranslations.StatTranslation parent;

    public Id(String str, StatTranslations.StatTranslation parent)
    {
        this.str = str;
        this.parent = parent;
    }

    public Id(String str)
    {
        this.str = str;
    }

    @Override
    public int compareTo(Object o) {
        Id that = (Id) o;
        return this.str.compareTo(that.str);
    }
    
    public static Comparator<Id> comparator = new Comparator<Id>()
    {
        @Override
        public int compare(Id id1, Id id2) {
            return id1.str.compareTo(id2.str);
        }
    };

    public void print()
    {
        System.out.println(str);
    }
}
