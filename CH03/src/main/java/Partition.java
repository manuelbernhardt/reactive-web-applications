import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Partition {

    public static void main(String... args) {

        User bob = new User("Bob", "Marley", 19);
        User jimmy = new User("Jimmy", "Hendrix", 16);

        List<User> users = new LinkedList<>();
        users.add(bob);
        users.add(jimmy);

        List<User> minors = new ArrayList<User>();
        List<User> majors = new ArrayList<User>();

        for(int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if(u.getAge() < 18) {
                minors.add(u);
            } else {
                majors.add(u);
            }
        }
    }

}

