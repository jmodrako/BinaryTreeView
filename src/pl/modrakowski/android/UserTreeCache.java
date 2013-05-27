package pl.modrakowski.android;

import android.util.Pair;
import pl.modrakowski.android.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: Jack Modrakowski
 * Date: 5/27/13
 * Time: 4:32 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class UserTreeCache {
    private HashMap<Integer, User> userHashMap;

    public UserTreeCache() {
        userHashMap = new HashMap<Integer, User>();
    }

    public void addUser(User user) {
        userHashMap.put(user.userId, user);
    }

    public User getUserById(int id) {
        return userHashMap.get(id);
    }

    public User getUserByParentAndPlacement(int parentId, int placement) {
        Set<Map.Entry<Integer, User>> set = userHashMap.entrySet();
        for (Map.Entry<Integer, User> entry : set) {
            User user = entry.getValue();
            if (user.parentId == parentId && user.placement == placement) {
                return user;
            }
        }
        throw new IllegalStateException("There is no user with parentId: " + parentId);
    }

    public Pair<User, User> getChildsForParent(User parent) {
        return new Pair<User, User>(getUserByParentAndPlacement(parent.userId, 0), getUserByParentAndPlacement(parent.userId, 1));
    }
}
