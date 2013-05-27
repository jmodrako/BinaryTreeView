package pl.modrakowski.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import pl.modrakowski.android.models.User;

/**
 * User: Jack Modrakowski
 * Date: 5/27/13
 * Time: 1:02 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class BinaryTreeFragment extends Fragment {

    protected UserViewWrapper parentLayoutWrapper;
    protected UserViewWrapper leftLayoutWrapper;
    protected UserViewWrapper rightLayoutWrapper;

    private CustomFontTextView parentFirstName;
    private CustomFontTextView parentLastName;
    private CustomFontTextView parentDesc;
    private CircularImageView parentImage;
    private CustomFontTextView parentRole;

    private CustomFontTextView leftFirstName;
    private CustomFontTextView leftLastName;
    private CustomFontTextView leftDesc;
    private CircularImageView leftImage;
    private CustomFontTextView leftRole;

    private CustomFontTextView rightFirstName;
    private CustomFontTextView rightLastName;
    private CustomFontTextView rightDesc;
    private CircularImageView rightImage;
    private CustomFontTextView rightRole;

    private User currentParent;
    private User currentLeft;
    private User currentRight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        final UserTreeCache userTreeCache = new UserTreeCache();
        userTreeCache.addUser(new User(0, -1, "Jacek", "Modrakowski", getString(R.string.jacek_desc), getString(R.string.jacek_role), R.drawable.jacek, -1));
        userTreeCache.addUser(new User(1, 0, "Clint", "Eastwood", getString(R.string.clint_desc), getString(R.string.clint_role), R.drawable.clinteastwood, 0));
        userTreeCache.addUser(new User(2, 0, "Mary", "Ure", getString(R.string.mary_ure_desc), getString(R.string.mary_role), R.drawable.mary_ure, 1));
        userTreeCache.addUser(new User(3, 1, "Anton", "Diffring", getString(R.string.anton_desc), getString(R.string.anton_role), R.drawable.anton_diffring, 0));
        userTreeCache.addUser(new User(4, 1, "Derren", "Nesbitt", getString(R.string.daren_desc), getString(R.string.daren_role), R.drawable.daren, 1));
        userTreeCache.addUser(new User(5, 2, "Morgan", "Freeman", getString(R.string.morgan_desc), getString(R.string.morgan_role), R.drawable.morgan, 0));
        userTreeCache.addUser(new User(6, 2, "Richard", "Harris", getString(R.string.haris_desc), getString(R.string.haris_role), R.drawable.harris, 1));

        parentLayoutWrapper = (UserViewWrapper) getView().findViewById(R.id.parent);
        leftLayoutWrapper = (UserViewWrapper) getView().findViewById(R.id.left_child);
        rightLayoutWrapper = (UserViewWrapper) getView().findViewById(R.id.right_child);

        parentFirstName = (CustomFontTextView) getView().findViewById(R.id.test_parent_first_name);
        parentLastName = (CustomFontTextView) getView().findViewById(R.id.test_parent_last_name);
        parentImage = (CircularImageView) getView().findViewById(R.id.circular_imageview_parent);
        parentDesc = (CustomFontTextView) getView().findViewById(R.id.parent_back);
        parentRole = (CustomFontTextView) getView().findViewById(R.id.test_parent_stan_name);

        leftFirstName = (CustomFontTextView) getView().findViewById(R.id.test_left_first_name);
        leftLastName = (CustomFontTextView) getView().findViewById(R.id.test_left_last_name);
        leftImage = (CircularImageView) getView().findViewById(R.id.circular_imageview_left);
        leftDesc = (CustomFontTextView) getView().findViewById(R.id.left_back);
        leftRole = (CustomFontTextView) getView().findViewById(R.id.test_left_stan_name);

        rightFirstName = (CustomFontTextView) getView().findViewById(R.id.test_right_first_name);
        rightLastName = (CustomFontTextView) getView().findViewById(R.id.test_right_last_name);
        rightImage = (CircularImageView) getView().findViewById(R.id.circular_imageview_right);
        rightDesc = (CustomFontTextView) getView().findViewById(R.id.right_back);
        rightRole = (CustomFontTextView) getView().findViewById(R.id.test_right_stan_name);

        User parent = userTreeCache.getUserById(0);
        User left = userTreeCache.getChildsForParent(parent).first;
        User right = userTreeCache.getChildsForParent(parent).second;
        fillUserData(UserViewWrapper.WrapperUserType.PARENT, parent);
        fillUserData(UserViewWrapper.WrapperUserType.LEFT_CHILD, left);
        fillUserData(UserViewWrapper.WrapperUserType.RIGHT_CHILD, right);

        currentParent = parent;
        currentLeft = left;
        currentRight = right;

        setCurrentsUsers(parent, left, right);

        UserViewWrapper.setOnViewsAreAtTheTopOfTheScreenListener(new UserViewWrapper.OnViewsAreAtTheTopOfTheScreenListener() {
            @Override
            public void onViewsAreAtTheTopOfTheScreen(UserViewWrapper.WrapperUserType wrapperUserType) {
                switch (wrapperUserType) {
                    case PARENT:
                        User parent = userTreeCache.getUserById(currentParent.parentId);
                        Pair<User, User> pairLeft = userTreeCache.getChildsForParent(parent);
                        fillLevel(parent, pairLeft.first, pairLeft.second);
                        setCurrentsUsers(parent, pairLeft.first, pairLeft.second);
                        break;
                }
            }
        });
        UserViewWrapper.setOnViewsAreAtTheBottomOfTheScreenListener(new UserViewWrapper.OnViewsAreAtTheBottomOfTheScreenListener() {
            @Override
            public void onViewsAreAtTheBottomOfTheScreen(UserViewWrapper.WrapperUserType wrapperUserType) {
                switch (wrapperUserType) {
                    case LEFT_CHILD:
                        try {
                            // Get childs for current hold LEFT user.
                            Pair<User, User> pairLeft = userTreeCache.getChildsForParent(currentLeft);
                            fillLevel(currentLeft, pairLeft.first, pairLeft.second);
                        } catch (Exception ignore) {
                        }
                        break;
                    case RIGHT_CHILD:
                        try {
                            // Get childs for current hold RIGHT user.
                            Pair<User, User> pairRight = userTreeCache.getChildsForParent(currentRight);
                            fillLevel(currentRight, pairRight.first, pairRight.second);
                        } catch (Exception ignore) {
                        }
                        break;
                }
            }
        });
    }

    private void fillLevel(User parent, User left, User right) {
        fillUserData(UserViewWrapper.WrapperUserType.PARENT, parent);
        fillUserData(UserViewWrapper.WrapperUserType.LEFT_CHILD, left);
        fillUserData(UserViewWrapper.WrapperUserType.RIGHT_CHILD, right);

        setCurrentsUsers(parent, left, right);
    }

    private void fillUserData(UserViewWrapper.WrapperUserType wrapperUserType, User user) {
        switch (wrapperUserType) {
            case LEFT_CHILD:
                leftFirstName.setText(user.firstName);
                leftLastName.setText(user.lastName);
                leftImage.setImageResource(user.image);
                leftDesc.setText(user.description);
                leftRole.setText(user.role);
                break;
            case RIGHT_CHILD:
                rightFirstName.setText(user.firstName);
                rightLastName.setText(user.lastName);
                rightImage.setImageResource(user.image);
                rightDesc.setText(user.description);
                rightRole.setText(user.role);
                break;
            case PARENT:
                parentFirstName.setText(user.firstName);
                parentLastName.setText(user.lastName);
                parentImage.setImageResource(user.image);
                parentDesc.setText(user.description);
                parentRole.setText(user.role);
                break;
        }
    }

    private void setCurrentsUsers(User parent, User left, User right) {
        currentParent = parent;
        currentLeft = left;
        currentRight = right;

        if (currentParent.parentId == -1) {
            parentLayoutWrapper.setMoveDirection(UserViewWrapper.MoveDirection.NONE);
        } else {
            parentLayoutWrapper.setMoveDirection(UserViewWrapper.MoveDirection.DOWN);
        }
    }
}
