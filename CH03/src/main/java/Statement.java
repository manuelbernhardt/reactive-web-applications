import java.util.LinkedList;
import java.util.List;

public class Statement {

    /**
     * Statement that mutates the original list
     * @param list the list to be filtered
     * @param toRemove the String to remove
     */
    public static void removeElement(List<String> list, String toRemove) {
        int index = 0;
        for (String s : list) {
            if (s.equals(toRemove)) {
                list.remove(index);
            }
            index++;
        }
    }

    /**
     * Expression that returns a new list without altering the original one
     * @param list the list to be filtered
     * @param toRemove the String to be removed
     * @return a new list that does not contain the String to be removed
     */
    public static List<String> filterNot(List<String> list, String toRemove) {
        List<String> filtered = new LinkedList<String>();
        for (String s : list) {
            if (!s.equals(toRemove)) {
                filtered.add(s);
            }
        }
        return filtered;
    }


}