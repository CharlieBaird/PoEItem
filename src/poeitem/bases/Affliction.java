package poeitem.bases;

public class Affliction
{
    
    private String name;
    private String[] stats;

    public Affliction(String name, String[] stats) {
        this.name = name;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public String[] getStats() {
        return stats;
    }
    
    public void print()
    {
        System.out.println(name);
        for (String s : stats)
        {
            System.out.println(s);
        }
    }
    
}
