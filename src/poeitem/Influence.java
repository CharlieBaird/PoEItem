package poeitem;

public enum Influence {
    CRUSADER ("Crusader"),
    ELDER ("Elder"),
    HUNTER ("Hunter"),
    REDEEMER ("Redeemer"),
    SHAPER ("Shaper"),
    WARLORD ("Warlord");
    
    public final String friendly;
    
    private Influence(String str)
    {
        this.friendly = str;
    }
}
