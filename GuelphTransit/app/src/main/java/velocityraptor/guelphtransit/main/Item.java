package velocityraptor.guelphtransit.main;

/**
 * Created by Aidan Maher on 09/10/17.
 * The Item class which is used to help inject Strings into a layout
 */

public class Item {
    private final CharSequence first;
    private final CharSequence second;
    public int position=-1;

    public Item(CharSequence aKey, CharSequence aValue,int pos)
    {
        first   = aKey;
        second = aValue;
        position = pos;
    }

    public CharSequence first()   { return first; }
    public CharSequence second() { return second; }
    public int position(){return position;}
}
