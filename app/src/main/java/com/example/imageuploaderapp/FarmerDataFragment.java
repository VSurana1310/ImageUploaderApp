package com.example.imageuploaderapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerDataFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView textViewName, textViewAddress, textViewCropType, textViewLivestockType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
//        textViewName = view.findViewById(R.id.textViewName);
//        textViewAddress = view.findViewById(R.id.textViewAddress);
//        textViewCropType = view.findViewById(R.id.textViewCropDetails);
//        textViewLivestockType = view.findViewById(R.id.textViewLivestock);

        // Fetch user data and display it
        fetchUserData("J8j8Om4ZpmkX3VQMrWlV"); // Pass the appropriate userId or document reference

        return view;
    }

    private void fetchUserData(String userId) {
        // Fetch data from Firestore by document ID
        db.collection("farmers").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Map the document to FarmerData
                        FarmerData farmer = documentSnapshot.toObject(FarmerData.class);

                        if (farmer != null) {
                            // Update the UI with the fetched data
                            textViewName.setText("Name: " + farmer.getName());
                            textViewAddress.setText("Address: " + farmer.getAddress());
                            textViewCropType.setText("Crop Type: " + farmer.getCropType());
                            textViewLivestockType.setText("Livestock Type: " + farmer.getLivestockType());
                        }
                    } else {
                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
