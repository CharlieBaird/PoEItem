package poeitem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StatTranslations implements Serializable {
    
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
                JsonObject jsonObject = english.get(j).getAsJsonObject();
                
                JsonArray format = jsonObject.get("format").getAsJsonArray();
                JsonArray condition = jsonObject.get("condition").getAsJsonArray();
                
                String string = jsonObject.get("string").getAsString();
                for (int k=0; k<format.size(); k++)
                {
                    String formatK = format.get(k).getAsString();
                    string = string.replaceFirst("\\{\\d\\}", formatK);
                    
//                    if (formatK.equals("ignore"))
//                        continue;
                    
                    int min;
                    int max;
                    try {
                        min = condition.get(k).getAsJsonObject().get("min").getAsInt();
                    } catch (NullPointerException e) {
                        min = -100000;
                    }
                    try {
                        max = condition.get(k).getAsJsonObject().get("max").getAsInt();
                    } catch (NullPointerException e)
                    {
                        max = 100000;
                    }
                    
                    statTranslation.conditions.add(new Condition(min, max, true));
                    
                }
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
    
    protected class StatTranslation implements Serializable
    {
        ArrayList<Condition> conditions;
        ArrayList<String> strings;
        Id[] ids;
                
        public StatTranslation()
        {
            strings = new ArrayList<>();
            conditions = new ArrayList<>();
        }
        
        public void print()
        {
            for (String str : strings)
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

class Condition implements Serializable
{
    int min;
    int max;
    boolean valid;
    
    public Condition(int min, int max, boolean valid)
    {
        this.min = min;
        this.max = max;
        this.valid = valid;
    }
}

class Id implements Comparable, Serializable
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
