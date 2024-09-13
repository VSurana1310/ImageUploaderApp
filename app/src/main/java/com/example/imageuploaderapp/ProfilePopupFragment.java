package com.example.imageuploaderapp;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePopupFragment extends DialogFragment {

    private TextView nameTextView;
    private TextView addressTextView;
    private TextView cropTextView;
    private TextView livestockTextView;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_popup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameTextView = view.findViewById(R.id.tv_farmer_name);
        addressTextView = view.findViewById(R.id.tv_farmer_address);
        cropTextView = view.findViewById(R.id.tv_farmer_crop);
        livestockTextView = view.findViewById(R.id.tv_farmer_livestock);
        progressBar = view.findViewById(R.id.progressBar);

        // Show progress bar while loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);  // or another action
        } else {
            Log.e("ProfilePopupFragment", "ProgressBar is null");
        }
        progressBar.setVisibility(View.VISIBLE);

        // Fetch user profile data from Firestore
        fetchProfileData();
    }

    private void fetchProfileData() {
        // Fetch the stored document ID from SharedPreferences
        String documentId = getActivity().getSharedPreferences("userPrefs", 0)
                .getString("documentId", null);

        if (documentId != null) {
            db.collection("farmers").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String address = documentSnapshot.getString("address");
                            String crop = documentSnapshot.getString("cropType");
                            String livestock = documentSnapshot.getString("livestockType");

                            // Set data to views
                            nameTextView.setText(name);
                            addressTextView.setText(address);
                            cropTextView.setText(crop);
                            livestockTextView.setText(livestock);
                            Toast.makeText(getContext(), "Profile data found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide progress bar after loading
                        } else {
                            progressBar.setVisibility(View.GONE); // Hide progress bar
                            Toast.makeText(getContext(), "Profile data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        Toast.makeText(getContext(), "Error fetching profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE); // Hide progress bar if document ID not found
            Toast.makeText(getContext(), "Profile data not found", Toast.LENGTH_SHORT).show();
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("User Profile");
        return dialog;
    }
}
