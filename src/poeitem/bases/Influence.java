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
    NORMAL("Normal"),
    SHAPER("Shaper"),
    ELDER("Elder"),
    HUNTER("Hunter"),
    REDEEMER("Redeemer"),
    WARLORD("Warlord"),
    CRUSADER("Crusader");
    
    public String friendly;
    
    private Influence(String friendly)
    {
        this.friendly = friendly;
    }
}
