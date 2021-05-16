/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poeitem.bases;

/**
 *
 * @author charl
 */
public enum Influence {
    NORMAL("Normal", ""),
    SHAPER("Shaper", "_shaper"),
    ELDER("Elder", "_elder"),
    HUNTER("Hunter", "_basilisk"),
    REDEEMER("Redeemer", "_eyrie"),
    WARLORD("Warlord", "_adjudicator"),
    CRUSADER("Crusader", "_crusader");
    
    public String friendly;
    
    public String tagSuffix;
    
    public static Influence getFromFriendly(String friendly)
    {
        for (Influence inf : Influence.values())
        {
            if (inf.friendly.equals(friendly))
            {
                return inf;
            }
        }
        
        return Influence.NORMAL;
    }
    
    private Influence(String friendly, String tagSuffix)
    {
        this.friendly = friendly;
        this.tagSuffix = tagSuffix;
    }
}
