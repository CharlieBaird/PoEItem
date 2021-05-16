package poeitem;

import java.io.Serializable;

class Stat implements Serializable {
    
    private String id;
    private double min;
    private double max;

    public Stat(String id, double min, double max) {
        this.id = id;
        this.min = min;
        this.max = max;
    }

    public String getId() {
        return id;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
    
    public void print()
    {
        System.out.println("    " + id);
        System.out.println("        min: " + min);
        System.out.println("        max: " + max);
    }
    
}
