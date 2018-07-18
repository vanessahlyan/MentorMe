package me.chrislewis.mentorship;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.io.File;

public class ProfileFragment extends Fragment {

    final static String NAME_KEY = "name";
    final static String JOB_KEY = "job";
    final static String PROFILE_IMAGE_KEY = "profileImage";
    final static String SKILLS_KEY = "skills";
    final static String SUMMARY_KEY = "summary";
    final static String EDUCATION_KEY = "education";
    final static String FAVORITE_KEY = "favorites";

    ImageView ivProfile;
    TextView tvName;
    TextView tvJob;
    TextView tvSkills;
    TextView tvSummary;
    TextView tvEdu;
    Button bLogOut;
    Button bUploadProfileImage;
    Button bTakePhoto;
    File photoFile;
    Bitmap photoBitmap;

    ParseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvName = view.findViewById(R.id.tvName);
        tvJob = view.findViewById(R.id.tvJob);
        tvSkills = view.findViewById(R.id.tvSkills);
        tvSummary = view.findViewById(R.id.tvSummary);
        tvEdu = view.findViewById(R.id.tvEdu);
        ivProfile = view.findViewById(R.id.ivProfile);

        user = ParseUser.getCurrentUser();
        populateInfo(user);
        bLogOut = view.findViewById(R.id.bLogOut);
        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        bTakePhoto = view.findViewById(R.id.bTakePhoto);
        bTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                activity.launchCamera();
            }
        });

        bUploadProfileImage = view.findViewById(R.id.bUploadProfileImage);
        bUploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                activity.launchPhotos();

            }
        });
    }

    // method that takes a string file path
    public void processImageString(String uri) {
        Bitmap takenImage = BitmapFactory.decodeFile(uri);
        photoFile = new File(uri);
        Glide.with(this).load(takenImage).into(ivProfile);
    }

    public void processImageBitmap(Bitmap takenImage) {
        photoBitmap = takenImage;
        Glide.with(this).load(takenImage).into(ivProfile);
    }

    private void populateInfo(ParseUser user) {
        tvName.setText(user.getString(NAME_KEY));
        if (user.getString(JOB_KEY) != null ) {
            tvJob.setText(String.format("Job: %s", user.getString(JOB_KEY)));
        }
        if (user.getString(SKILLS_KEY) != null ) {
            tvSkills.setText(String.format("Skills: %s", user.getString(SKILLS_KEY)));
        }
        if (user.getString(SUMMARY_KEY) != null ) {
            tvSummary.setText(String.format("Summary: %s", user.getString(SUMMARY_KEY)));
        }
        if (user.getString(EDUCATION_KEY) != null ) {
            tvSummary.setText(String.format("Education: %s", user.getString(EDUCATION_KEY)));
        }
        JSONArray favArray = ParseUser.getCurrentUser().getJSONArray(FAVORITE_KEY);

        if (user.getParseFile(PROFILE_IMAGE_KEY) != null) {
            Glide.with(getContext())
                    .load(user.getParseFile(PROFILE_IMAGE_KEY).getUrl())
                    .apply(new RequestOptions().circleCrop())
                    .into(ivProfile);
        }
    }

}
