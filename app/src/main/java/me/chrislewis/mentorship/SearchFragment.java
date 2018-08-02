package me.chrislewis.mentorship;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.chrislewis.mentorship.models.User;

public class SearchFragment extends Fragment {

    private User currentUser;

    private SharedViewModel model;

    private List<String> currentCategories;
    private List<String> allCategories;
    private GridAdapter gridAdapter;
    private ArrayList<ParseUser> users;
    private RecyclerView rvUsers;
    private ProgressBar pb;
    private SwipeRefreshLayout swipeContainer;
    private DrawerLayout mDrawerLayout;
    private List<ParseUser> sameCategoryUsers;
    private Button btnOpenDrawer;
    MenuItem checkedItem;
    ParseGeoPoint currentParseLocation;
    SubMenu menuGroup;
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        ParseUser.getCurrentUser().fetchInBackground();
        currentUser = new User(ParseUser.getCurrentUser());
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        setupNavigationDrawer(view);

        rvUsers = view.findViewById(R.id.rvGrid);

        users = new ArrayList<>();
        gridAdapter = new GridAdapter(users, model);
        pb = view.findViewById(R.id.pbLoading);

        rvUsers.setLayoutManager(new GridLayoutManager(this.getActivity(), 2));
        rvUsers.setAdapter(gridAdapter);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (checkedItem == null) {
                    getAllUsers();
                }
                else {
                    String category = checkedItem.toString();
                    if (category == "All Categories") {
                        getAllUsers();
                    }
                    else {
                        filterByCategory(category);
                    }
                }
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //setupLocationServices();

        getAllUsers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_calendar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getAllUsers() {
        pb.setVisibility(ProgressBar.VISIBLE);
        gridAdapter.clear();
        ParseUser.getCurrentUser().fetchInBackground();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isMentor", true);
        query.whereNotEqualTo("objectId", currentUser.getObjectId());


        try {
            List<ParseUser> users = query.find();
            sameCategoryUsers = new ArrayList<>(users);

            for (ParseUser user : users) {
                User mUser = new User(user);
                List<String> common = mUser.getCategories();
                common.retainAll(currentUser.getCategories());

                if (common.size() == 0) {
                    sameCategoryUsers.remove(user);
                    Log.i("common", "Common is null");
                }
            }

            for(int i = 0; i < sameCategoryUsers.size(); i++) {
                User user = new User (sameCategoryUsers.get(i));
                user.setRank(calculateRank(user));
            }
            Collections.sort(sameCategoryUsers, new Comparator<ParseUser>() {
                @Override
                public int compare(ParseUser o1, ParseUser o2) {
                    return (int)(o1.getDouble("rank") - (o2.getDouble("rank")));
                }
            });
            gridAdapter.addAll(sameCategoryUsers);

        }
        catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        pb.setVisibility(ProgressBar.INVISIBLE);
        swipeContainer.setRefreshing(false);

    }

    public double calculateRank(User user) {
        double distanceRank = 0;
        double organizationRank = 0;
        double educationRank = 0;
        double ratingRank = 0;
        if(!user.getOrganization().equals(currentUser.getOrganization())) {
            organizationRank = 4;
        }
        if(!user.getEducation().equals(currentUser.getOrganization())) {
            educationRank = 5;
        }

        User mUser = new User(ParseUser.getCurrentUser());
        currentParseLocation = mUser.getCurrentLocation();
        if (currentParseLocation != null) {
            Location currentLocation = new Location("MainActivity");
            currentLocation.setLongitude(currentParseLocation.getLongitude());
            currentLocation.setLatitude(currentParseLocation.getLatitude());

            Location otherLocation = new Location("parse other user");
            otherLocation.setLongitude(user.getCurrentLocation().getLongitude());
            otherLocation.setLatitude(user.getCurrentLocation().getLatitude());

            double distanceInMeters = otherLocation.distanceTo(currentLocation);
            double distanceInMiles = distanceInMeters * 0.000621371192;
            double distance = Math.round(distanceInMiles * 10) / 10;

            distanceRank = 20 * distance;
            user.setRelDistance(distance);
            user.saveInBackground();

        }

        double rating = currentUser.getOverallRating();
        if (rating != 0.0) {
            ratingRank = 10 / 0.2;
        } else if (rating == 0.0) {
            ratingRank = 5.0;
        }
        return distanceRank + organizationRank + educationRank + ratingRank;
    }

    public void filterByCategory(String category) {
        mDrawerLayout.closeDrawers();

        pb.setVisibility(ProgressBar.VISIBLE);
        gridAdapter.clear();
        ParseUser.getCurrentUser().fetchInBackground();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isMentor", true);
        query.whereNotEqualTo("objectId", currentUser.getObjectId());
        query.whereContains("categories", category);

        try {
            List<ParseUser> users = query.find();
            sameCategoryUsers = new ArrayList<>(users);

            int numUsers = sameCategoryUsers.size();
            if (numUsers > 0) {
                for (int i = 0; i < numUsers; i++) {
                    User user = new User(sameCategoryUsers.get(i));
                    user.setRank(calculateRank(user));
                }
                Collections.sort(sameCategoryUsers, new Comparator<ParseUser>() {
                    @Override
                    public int compare(ParseUser o1, ParseUser o2) {
                        return (int) (o1.getDouble("rank") - (o2.getDouble("rank")));
                    }
                });
                gridAdapter.addAll(sameCategoryUsers);
            }
            else {
                Toast.makeText(getActivity(), "There are no mentors in this category", Toast.LENGTH_LONG).show();
            }
        }
        catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        pb.setVisibility(ProgressBar.INVISIBLE);
        swipeContainer.setRefreshing(false);
    }

    public void uncheckOtherItems(MenuItem item, Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (i != item.getItemId()) {
                menu.getItem(i).setChecked(false);
            }
        }
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void setupNavigationDrawer(View view) {
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        NavigationView navigationView = view.findViewById(R.id.drawer_view);
        final Menu menu = navigationView.getMenu();

        menuGroup = menu.addSubMenu("My Categories");
        currentCategories = currentUser.getCategories();

        int categoryId = 0;
        if (currentCategories != null) {
            for (String category : currentCategories) {
                categoryId = currentCategories.indexOf(category);
                menuGroup.add(Menu.NONE, categoryId, Menu.NONE, category);
            }
        }
        categoryId = categoryId + 1;
        menuGroup.add(Menu.NONE, categoryId, Menu.NONE, "All Categories");

        MenuItem last = menuGroup.getItem(menuGroup.size() - 1);
        last.setChecked(true);
        uncheckOtherItems(last, menuGroup);
        checkedItem = last;


        SubMenu menuOthers = menu.addSubMenu("Other Categories");
        allCategories = Arrays.asList("English", "History", "Science", "Math", "Art", "Languages");
        List<String> otherCategories = new ArrayList<>(allCategories);
        otherCategories.removeAll(currentCategories);

        if (otherCategories.size() > 0) {
            for (String category : otherCategories) {
                categoryId = otherCategories.indexOf(category);
                menuOthers.add(Menu.NONE, categoryId, Menu.NONE, category);
            }
        }

        SubMenu menuAdd = menu.addSubMenu("Add Categories");
        menuAdd.add(Menu.NONE, Menu.NONE, Menu.NONE, "Go to Profile");


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                String category = menuItem.toString();
                if (category != "Add Categories") {
                    menuItem.setChecked(true);
                    checkedItem = menuItem;
                    uncheckOtherItems(menuItem, menuGroup);
                    Toast.makeText(getActivity(),"Showing mentors for " + menuItem, Toast.LENGTH_SHORT).show();

                    if (category == "All Categories") {
                        getAllUsers();
                    } else {
                        filterByCategory(category);
                    }

                    mDrawerLayout.closeDrawers();

                } else {
                    Toast.makeText(getActivity(), "add new categories", Toast.LENGTH_SHORT).show();
                    FragmentTransaction fragmentTransaction = model.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.flContainer, profileFragment).commit();
                }
                return true;
            }
        });
    }
}