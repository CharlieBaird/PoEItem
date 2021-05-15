package poeitem;

import java.io.Serializable;

public class Weight implements Serializable {
    
    private final Tag tag;
    private final int weight;
    private final boolean canSpawn;

    public Weight(Tag tag, int weight) {
        this.tag = tag;
        this.weight = weight;
        
        canSpawn = weight > 0;
    }

    public Tag getTag() {
        return tag;
    }

    public int getWeight() {
        return weight;
    }

    public boolean canSpawn() {
        return canSpawn;
    }
    
}
