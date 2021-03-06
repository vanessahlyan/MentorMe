package me.chrislewis.mentorship.models;

import android.text.format.DateUtils;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User{
    private final static String NAME_KEY = "name";
    private final static String USERNAME_KEY = "username";
    private final static String JOB_KEY = "job";
    private final static String PROFILE_IMAGE_KEY = "profileImage";
    private final static String SKILLS_KEY = "skills";
    private final static String SKILLS_LIST_KEY = "skillStrings";
    private final static String SUMMARY_KEY = "summary";
    private final static String EDUCATION_KEY = "education";
    private final static String DESCRIPTION_KEY = "description";
    private final static String CATEGORY_KEY = "category";
    private final static String ORGANIZATION_KEY = "organization";
    private final static String RANK_KEY = "rank";
    private final static String FAVORITE_KEY = "favorites";
    private final static String REVIEWER_KEY = "reviewers";
    private final static String LOCATION_KEY = "location";
    private final static String DISTANCE_KEY = "distance";
    private final static String REL_DISTANCE_KEY = "relativeDistance";
    private final static String CATEGORIES_KEY = "categories";
    private final static String PASSWORD_KEY = "password";
    private final static String EMAIL_KEY = "email";
    private final static String OVERALL_RATING_KEY = "overallRating";
    private final static String REVIEWS_KEY = "reviews";
    private final static String NUM_RATINGS_KEY = "numRatings";
    private final static String SYNC_KEY = "allowSync";
    private final static String IS_MENTOR_KEY = "isMentor";
    private final static String CONNECTION_KEY = "connects";
    private final static String CHAT_KEY = "chats";

    private ParseUser user;
    private ArrayList<Chat> chats;
    private ArrayList<User> favorites;
    private ArrayList<Match> matches;

    public User(ParseUser user){
        try {
            this.user = user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ParseUser getParseUser() {
        return user;
    }

    public String getObjectId() {
        return user.getObjectId();
    }

    public String getUsername() {
        return user.getString(USERNAME_KEY);
    }

    public void setUsername(String username) {
        user.put(USERNAME_KEY, username);
    }

    public String getName() {
        return user.getString(NAME_KEY);
    }

    public void setName(String name) {
        user.put(NAME_KEY, name);
    }

    public String getPassword() { return user.getString(PASSWORD_KEY); }

    public void setPassword(String password) {
        user.put(PASSWORD_KEY, password);
    }

    public String getEmail() { return user.getString(EMAIL_KEY); }

    public void setEmail(String email) {
        user.put(EMAIL_KEY, email);
    }

    public String getJob() {
        return user.getString(JOB_KEY);
    }

    public void setJob(String job) {
        user.put(JOB_KEY, job);
    }

    public ParseFile getProfileImage() {
        return user.getParseFile(PROFILE_IMAGE_KEY);
    }

    public void setProfileImage(ParseFile image) {
        user.put(PROFILE_IMAGE_KEY, image);
    }

    public String getSkills() {
        return user.getString(SKILLS_KEY);
    }

    public void setSkills(String skills) {
        user.put(SKILLS_KEY, skills);
    }

    public List<String> getSkillsList() {
        return user.getList(SKILLS_LIST_KEY);
    }

    public String getSummary() {
        return user.getString(SUMMARY_KEY);
    }

    public void setSummary(String summary) {
        user.put(SUMMARY_KEY, summary);
    }

    public String getEducation() {
        return user.getString(EDUCATION_KEY);
    }

    public void setEducation(String education) {
        user.put(EDUCATION_KEY, education);
    }

    public String getDescription() {
        return user.getString(DESCRIPTION_KEY);
    }

    public void setDescription(String description) {
        user.put(DESCRIPTION_KEY, description);
    }

    public String getOrganization() {
        return user.getString(ORGANIZATION_KEY);
    }

    public void setOrganization(String organization) {
        user.put(ORGANIZATION_KEY, organization);
    }

    public double getRank() {
        return user.getDouble(RANK_KEY);
    }

    public void setRank(double rank) {
        user.put(RANK_KEY, rank);
    }

    public double getOverallRating() {return user.getDouble(OVERALL_RATING_KEY);}
    public void setOverallRating(double rating) { user.put(OVERALL_RATING_KEY, rating); }

    public Integer getNumRatings() {return user.getInt(NUM_RATINGS_KEY);}
    public void setNumRatings(Integer numRatings) { user.put(NUM_RATINGS_KEY, numRatings); }

    public boolean getIsMentor() { return user.getBoolean(IS_MENTOR_KEY); }

    public Double getRelDistance() {
        return user.getDouble(REL_DISTANCE_KEY);
    }

    public void setRelDistance(double distance) {
        user.put(REL_DISTANCE_KEY, distance);
    }

    public List<User> getFavorites() {
        if (favorites == null) {
            favorites = new ArrayList<>();
            List<ParseUser> parseUsers = (List<ParseUser>) user.get(FAVORITE_KEY);
            if (parseUsers != null) {
                for (ParseUser i : parseUsers) {
                    favorites.add(new User(i));
                }
                return favorites;
            } else {
                return null;
            }
        } else {
            return favorites;
        }
    }

    public void clearFavorites() {
        favorites.clear();
        user.remove(FAVORITE_KEY);
    }

    public void addFavorite(User user) {
        user.getParseUser().revert();
        favorites.add(user);
        this.user.add(FAVORITE_KEY, user.getParseUser());
    }

    public void removeFavorite(User user) {
        this.user.removeAll(FAVORITE_KEY, Collections.singleton(user.getParseUser()));
        favorites.remove(user);
    }

    public List<Match> getMatches() {
        if (matches == null) {
            matches = new ArrayList<>();
            List<Match> holder = (List<Match>) user.get(CONNECTION_KEY);
            if (holder != null) {
                for (Match i : holder) {
                    try {
                        i.fetchIfNeeded();
                        matches.add(i);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return matches;
            } else {
                return null;
            }
        } else {
            return matches;
        }
    }

    public void setMatches(List<Match> matches) {
        user.remove(CONNECTION_KEY);
        this.matches = (ArrayList<Match>) matches;
        user.addAllUnique(CONNECTION_KEY, matches);
    }

    public void clearMatch() {
        matches.clear();
    }

    public void addMatch(Match match) {
        matches.add(match);
        this.user.addUnique(CONNECTION_KEY, match);
    }

    public void removeMatch(Match match) {
        this.user.removeAll(CONNECTION_KEY, Collections.singleton(match));
        matches.remove(match);
    }

    public List<Review> getReviews() {
        return (List<Review>) user.get(REVIEWS_KEY);
    }

    public void addReview(Review review) {
        this.user.addUnique(REVIEWS_KEY, review);
    }

    public List<String> getCategories() {
        return (List<String>) user.get(CATEGORIES_KEY);
    }

    public void addCategory(String category) {
        this.user.addUnique(CATEGORIES_KEY, category);
    }

    public void setCategories(List<String> categories) {
        user.put(CATEGORIES_KEY, categories);
    }

    public ParseGeoPoint getCurrentLocation() {
        return user.getParseGeoPoint(LOCATION_KEY);
    }

    public void setLocation(ParseGeoPoint location) {
        user.put(LOCATION_KEY, location);
    }

    public void setDistance(String distance) {
        user.put(DISTANCE_KEY, distance);
    }

    public Boolean getSync() { return user.getBoolean(SYNC_KEY); }

    public void setSync(Boolean isSync) { user.put(SYNC_KEY, isSync); }

    public void invite(User user) {

    }

    public Chat findChat(List<User> users) {
        ArrayList<ParseUser> holder = new ArrayList<>();
        for (User i: users) {
            holder.add(i.getParseUser());
        }
        for (Chat chat : chats) {
            if (chat.getParseUsers().equals(holder)) {
                return chat;
            }
        }
        return null;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public ArrayList<Chat> getChats() {
        if (chats == null) {
            chats = new ArrayList<>();
            List<Chat> holder = (List<Chat>) user.get(CHAT_KEY);
            if (holder != null) {
                for (Chat i : holder) {
                    try {
                        i.fetchIfNeeded();
                        chats.add(i);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return chats;
            } else {
                return null;
            }
        } else {
            return chats;
        }
    }

    public void addChats(List<Chat> chat) {
        chats = (ArrayList<Chat>) chat;
        this.user.remove(CHAT_KEY);
        this.user.addAllUnique(CHAT_KEY, chat);
    }

    public void removeChat(Chat chat) {
        this.user.removeAll(CHAT_KEY, Collections.singleton(chat));
        chats.remove(chat);
    }


    public void saveInBackground() {
        user.saveInBackground();
    }

    public void saveInBackground(SaveCallback callback) {
        user.saveInBackground(callback);
    }

    public void fetchInBackground(GetCallback<ParseObject> callback) {
        user.fetchInBackground(callback);
    }

    public void fetchIfNeed() throws ParseException {
        user = user.fetchIfNeeded();
    }

    public String getRelativeTimeAgo() {
        String relativeDate;
        long dateMillis = user.getCreatedAt().getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        return relativeDate;
    }

    public static class Query extends ParseQuery<ParseUser> {
        public Query() {
            super(ParseUser.class);
        }

        public Query getUser(String objectId){
            whereEqualTo("objectId", objectId);
            return this;
        }

    }

}