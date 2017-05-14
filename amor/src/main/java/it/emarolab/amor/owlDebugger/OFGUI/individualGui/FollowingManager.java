package it.emarolab.amor.owlDebugger.OFGUI.individualGui;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowingManager {

    private int tableType;
    private int frameId;
    private List<Color> color;
    private List<String> followTxt;

    // private List< Boolean> isMapperFor = new ArrayList< Boolean>();

    public FollowingManager(int frameID, int type) {
        // accept the candidate if ...
        if (type < 5) {
            this.tableType = type;
            if (frameID >= 0) {
                if (!getAllInstances().keySet().contains(frameID)) {
                    this.frameId = frameID;
                    followTxt = new ArrayList<String>();
                    color = new ArrayList<Color>();
                    TrackedClass();
                }
            }
        }
    }

    public synchronized int addFollowText(List<String> tofollow, List<Color> tocolor) {
        int count = 0;
        int i = 0;
        if (tofollow.size() == tocolor.size()) {
            for (String s : tofollow) {
                if (addFollowText(s, tocolor.get(i++)))
                    ;
                count++;
            }
        }
        return (count);
    }

    public synchronized boolean addFollowText(String tofollow, Color tocolor) {
        if( followTxt != null){
            if ( !isFollowed(tofollow)) {
                if ( !isFollowed(tocolor)) {
                    // add to list
                    this.followTxt.add(tofollow);
                    this.color.add(tocolor);
                    // this.isMapperFor.add( false);
                    return (true);
                }
            }
        }
        return false;
    }

    public synchronized static boolean addFollowText(String tofollow,
            Color tocolor, int frameID) {
        FollowingManager fm = getAllInstances().get(frameID);
        if (fm != null) {
            return (fm.addFollowText(tofollow, tocolor));
        }
        return (false);
    }

    public synchronized static int addFollowText(List<String> tofollow,
            List<Color> tocolor, int frameID) {
        FollowingManager fm = getAllInstances().get(frameID);
        if (fm != null) {
            return (fm.addFollowText(tofollow, tocolor));
        }
        return (0);
    }

    public synchronized static Color addFollowText(String tofollow, int frameID) {
        FollowingManager fm = getAllInstances().get(frameID);
        if (fm != null) { // so frameID is correct
            if (fm.getFollowTxt().contains(tofollow)) { // fine
                Color c = fm.getColor()
                        .get(fm.getFollowTxt().indexOf(tofollow));
                fm.addFollowText(tofollow, c);
                return (c);
            }
        }
        return (null);
    }

    public synchronized boolean removeFollowingText(String toremove) {
        Color col;
        boolean txt;
        try {
            col = color.remove(followTxt.indexOf(toremove));
            txt = followTxt.remove(toremove);
            // isMapperFor.remove( followTxt.indexOf( toremove));
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            return (false);
        }
        if ((txt) && (col != null))
            return (true);
        else
            return (false);
    }

    public synchronized int removeFollowingText(List<String> toremove) {
        int count = 0;
        for (String s : toremove) {
            if (removeFollowingText(s))
                count++;
        }
        return (count);
    }

    public synchronized static boolean removeFollowingText(String toremove,
            int frameID) {
        FollowingManager fm = getAllInstances().get(frameID);
        if (fm != null) {
            return (fm.removeFollowingText(toremove));
        }
        return (false);
    }

    public synchronized static int removeFollowingText(List<String> toremove,
            int frameID) {
        int count = 0;
        for (String s : toremove) {
            if (removeFollowingText(s, frameID))
                count++;
        }
        return (count);
    }

    public synchronized void removeFrameFollowing() {
        allInstances.remove(frameId);
    }

    public synchronized static void removeFrameFollowing(int frameID) {
        allInstances.remove(frameID);
    }

    // getters
    public List<String> getFollowTxt() {
        return (followTxt);
    }

    public List<Color> getColor() {
        return (color);
    }

    public Integer getFrameId() {
        return (frameId);
    }

    public Integer getTableType() {
        return (tableType);
    }

    /*
     * // set from color renderer // must be called always after add !!!! public
     * void setSetterfor( String tofollow){ int idx = followTxt.indexOf(
     * tofollow); if( idx > 0){ isMapperFor.set(idx, true); } } public void
     * removeSetterfor( String tofollow){ int idx = followTxt.indexOf(
     * tofollow); if( idx > 0){ isMapperFor.set(idx, true); } } public boolean
     * isSetterfor( String tofollow){ int idx = followTxt.indexOf( tofollow);
     * if( idx < 0) return( false); else return( true); }
     */
    // store instances w.r.t. frameId
    private static Map<Integer, FollowingManager> allInstances = new HashMap<Integer, FollowingManager>();
    static {
        allInstances = new HashMap<Integer, FollowingManager>();
    }

    protected void finalize() {
        allInstances.values().remove(this);
    }

    private synchronized void TrackedClass() {
        allInstances.put(frameId, this);
    }

    public static synchronized Map< Integer, FollowingManager> getAllInstances() {
        return ((HashMap<Integer, FollowingManager>) allInstances);
    }

    public static synchronized boolean isFollowed(String tofollow) {
        if (tofollow == null) {
            //System.out.println(" \t\t esco perchè tofollow " + tofollow + " è nullo");
            return (true); // means that cannot be followed
        }
        if ((allInstances != null) || (!allInstances.isEmpty())) {
            for (Integer id : allInstances.keySet()) {
                List<String> texts = allInstances.get(id).getFollowTxt();
                for (String s : texts) {
                    //System.out.println("confronto " + s + " con " + tofollow);
                    if (s.equals(tofollow)) {
                        //System.out.println(" is NOT followable 1!!");
                        return (true);
                    } //else
                        //System.out.println(" \t\t confronto != " + s + " "
                            //    + tofollow);
                }
            }
        } else {
            //System.out.println(" is NOT followable 2!!");
            return true;
        }

        return false;
    }

    public static synchronized boolean isFollowed(Color tocolor) {
        if (tocolor == null) {
            //System.out.println(" \t esco perchè toColor " + tocolor
                //    + " è nullo");
            return true; // means that cannot be followed
        }
        // go deeply if non followable
        if ((tocolor == ClassExchange.getNullcolor())
                || (tocolor == ClassExchange.getAlreadyselectedcolor())) {
            if ((allInstances != null) || (!allInstances.isEmpty())) {
                for (Integer id : allInstances.keySet()) {
                    List<Color> colors = allInstances.get(id).getColor();
                    for (Color c : colors) {
                        System.out
                                .println("confronto " + c + " con " + tocolor);
                        if (c.equals(tocolor)) {
                            //System.out.println(" is NOT followable 3!!");
                            return (true);
                        } //else
                            //System.out.println(" \t confronto != " + c + " "
                                //    + tocolor);
                    }
                }
            } else {
                //System.out.println(" is NOT followable 4!!");
                return true;
            }
        } //else
            //System.out.println(" \t esco perchè toColor " + tocolor
                //    + " è ben settato");

        return false;
    }

    public static synchronized void printActiveIstances() { // return the number of activated
                                                // instances
        int count = 0;
        for (Map.Entry<Integer, FollowingManager> entry : allInstances
                .entrySet())
            System.out.println(count++ + " : " + entry);
    }

    public static synchronized String printActiveIstances(boolean getString) { // return the
                                                                    // number of
                                                                    // activated
                                                                    // instances
        int count = 0;
        String ret = "";
        if (!getString)
            for (Map.Entry<Integer, FollowingManager> entry : allInstances
                    .entrySet())
                System.out.println(count++ + " : " + entry);
        else {
            ret = "\n";
            for (Map.Entry<Integer, FollowingManager> entry : allInstances
                    .entrySet())
                ret += count++ + " : " + entry + "\n";
        }
        return ret;
    }

    public static synchronized boolean isChoosedColor(Color color) {
        for (Integer id : allInstances.keySet()) {
            FollowingManager fm = allInstances.get(id);
            for (Color c : fm.getColor()) {
                if (c.equals(color)) {
                    return (true);
                }
            }
        }
        return (false);
    }

    public static synchronized Map<String, Color> allColorToFollow() {
        Map<String, Color> target = new HashMap<String, Color>();
        // System.out.println( "\t\t all instances.keys " +
        // allInstances.keySet());
        for (Integer id : allInstances.keySet()) {
            FollowingManager fm = allInstances.get(id); // for all the instances
            List<Color> color = fm.getColor();

            // System.out.println( "\t\t\t colors " + color);
            // System.out.println( "\t\t\t text   " + fm.getFollowTxt());

            if (color.size() == fm.getFollowTxt().size()) {
                int i = 0;
                for (String s : fm.getFollowTxt()) { // for all the following in
                                                        // a frame

                    // System.out.println("\t\t\t\t for all in target keys : " +
                    // s);

                    if (!target.keySet().contains(s)) {
                        target.put(s, color.get(i));
                        // System.out.println("\t\t\t\t found " + target);
                    }
                }
            }
        }
        return target;
    }


    

}


