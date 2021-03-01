package poeitem.bases;

public class Affliction
{
    
    private String name;
    private String id;

    public Affliction(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
    
    public void print()
    {
        System.out.println(name + ": " + id);
    }
    
}
