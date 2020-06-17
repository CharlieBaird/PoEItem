package poeitem;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseItem {
    
    public static ArrayList<BaseItem> AllBaseItems = new ArrayList<>();
    
    public final static HashMap<String, Base> BaseItemKey = new HashMap<String, Base>() {{
        put ("Claw", Base.CLAW);
        put ("Dagger", Base.DAGGER);
        put ("Wand", Base.WAND);
        put ("One+Hand+Sword", Base.ONE_HAND_SWORD);
        put ("Thrusting+One+Hand+Sword", Base.THRUSTING_ONE_HAND_SWORD);
        put ("One+Hand+Axe", Base.ONE_HAND_AXE);
        put ("One+Hand+Mace", Base.ONE_HAND_MACE);
        put ("Sceptre", Base.SCEPTRE);
        put ("Rune+Dagger", Base.RUNE_DAGGER);
        put ("Bow", Base.BOW);
        put ("Staff", Base.STAFF);
        put ("Two+Hand+Sword", Base.TWO_HAND_SWORD);
        put ("Two+Hand+Axe", Base.TWO_HAND_AXE);
        put ("Two+Hand+Mace", Base.TWO_HAND_MACE);
        put ("FishingRod", Base.FISHING_ROD);
        put ("Warstaff", Base.WARSTAFF);
        put ("BaseItemTypes&an=Crimson+Jewel", Base.CRIMSON_JEWEL);
        put ("BaseItemTypes&an=Viridian+Jewel", Base.VIRIDIAN_JEWEL);
        put ("BaseItemTypes&an=Murderous+Eye+Jewel", Base.MURDEROUS_EYE_JEWEL);
        put ("BaseItemTypes&an=Searching+Eye+Jewel", Base.SEARCHING_EYE_JEWEL);
        put ("BaseItemTypes&an=Ghastly+Eye+Jewel", Base.GHASTLY_EYE_JEWEL);
        put ("BaseItemTypes&an=Hypnotic+Eye+Jewel", Base.HYPNOTIC_EYE_JEWEL);
        put ("BaseItemTypes&an=Large+Cluster+Jewel", Base.LARGE_CLUSTER_JEWEL);
        put ("BaseItemTypes&an=Medium+Cluster+Jewel", Base.MEDIUM_CLUSTER_JEWEL);
        put ("BaseItemTypes&an=Small+Cluster+Jewel", Base.SMALL_CLUSTER_JEWEL);
        put ("Amulet", Base.AMULET);
        put ("Ring", Base.RING);
        put ("Ring&an=unset_ring", Base.UNSET_RING);
        put ("Belt", Base.BELT);
        put ("Gloves", Base.GLOVES);
        put ("Gloves&an=an=str_armour", Base.GLOVES);
        put ("Gloves&an=dex_armour", Base.GLOVES);
        put ("Gloves&an=int_armour", Base.GLOVES);
        put ("Gloves&an=str_dex_armour", Base.GLOVES);
        put ("Gloves&an=str_int_armour", Base.GLOVES);
        put ("Gloves&an=dex_int_armour", Base.GLOVES);
        put ("Boots", Base.BOOTS);
        put ("Boots&an=an=str_armour", Base.BOOTS);
        put ("Boots&an=dex_armour", Base.BOOTS);
        put ("Boots&an=int_armour", Base.BOOTS);
        put ("Boots&an=str_dex_armour", Base.BOOTS);
        put ("Boots&an=str_int_armour", Base.BOOTS);
        put ("Boots&an=dex_int_armour", Base.BOOTS);
        put ("Body+Armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=an=str_armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=dex_armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=int_armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=str_dex_armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=str_int_armour", Base.BODY_ARMOUR);
        put ("Body+Armour&an=dex_int_armour", Base.BODY_ARMOUR);
        put ("Helmet", Base.HELMET);
        put ("Helmet&an=an=str_armour", Base.HELMET);
        put ("Helmet&an=dex_armour", Base.HELMET);
        put ("Helmet&an=int_armour", Base.HELMET);
        put ("Helmet&an=str_dex_armour", Base.HELMET);
        put ("Helmet&an=str_int_armour", Base.HELMET);
        put ("Helmet&an=dex_int_armour", Base.HELMET);
        put ("Quiver", Base.QUIVER);
        put ("Shield", Base.SHIELD);
        put ("Shield&an=an=str_armour,str_shield", Base.SHIELD);
        put ("Shield&an=dex_armour,dex_shield", Base.SHIELD);
        put ("Shield&an=int_armour,focus", Base.SHIELD);
        put ("Shield&an=str_dex_armour,str_dex_shield", Base.SHIELD);
        put ("Shield&an=str_int_armour,str_int_shield", Base.SHIELD);
        put ("Shield&an=dex_int_armour,dex_int_shield", Base.SHIELD);
        put ("LifeFlask", Base.FLASK);
        put ("ManaFlask", Base.FLASK);
        put ("HybridFlask", Base.FLASK);
        put ("UtilityFlask", Base.FLASK);
        put ("UtilityFlaskCritical", Base.FLASK);
    }};
    
    public static BaseItem getFromBase(Base specifiedBase)
    {
        for (BaseItem b : AllBaseItems)
        {
            if (b.base == specifiedBase)
                return b;
        }
        
        return null;
    }

    public static void genBaseItems() {
        AllBaseItems.clear();
        for (Base base : Base.values())
        {
            AllBaseItems.add(new BaseItem(base));
        }
    }
    
    public ArrayList<Modifier> assocModifiers = new ArrayList<>();
    
    public Base base;
    
    private BaseItem(Base base)
    {
        this.base = base;
    }
    
    public Modifier getExplicitFromStr(String str)
    {
        for (Modifier m : assocModifiers)
        {
            if (m.getStr().equals(str))
            {
                return m;
            }
        }
        
        return null;
    }
}
