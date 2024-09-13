package com.example.imageuploaderapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFormFragment extends DialogFragment {

    private EditText editTextName;
    private EditText editTextAddress;
    private EditText editTextCropType;
    private EditText editTextLivestockType;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_register_form, null);

        editTextName = view.findViewById(R.id.editTextName);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        editTextCropType = view.findViewById(R.id.editTextCrops);
        editTextLivestockType = view.findViewById(R.id.editTextLivestock);
        Button buttonSubmit = view.findViewById(R.id.buttonSubmit);
        progressBar = view.findViewById(R.id.progressBar);

        builder.setView(view)
                .setTitle("Farmer Registration");

        buttonSubmit.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String address = editTextAddress.getText().toString();
            String cropType = editTextCropType.getText().toString();
            String livestockType = editTextLivestockType.getText().toString();

            // Show progress bar and disable the submit button
            progressBar.setVisibility(View.VISIBLE);
            buttonSubmit.setEnabled(false);

            FarmerData farmerData = new FarmerData(name, address, cropType, livestockType);

            db.collection("farmers")
                    .add(farmerData)
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId(); // Get the document ID

                        // Save the document ID in SharedPreferences
                        getActivity().getSharedPreferences("userPrefs", 0)
                                .edit()
                                .putString("documentId", documentId)
                                .apply();

                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        buttonSubmit.setEnabled(true); // Re-enable the submit button
                        Toast.makeText(getContext(), "Data submitted successfully!", Toast.LENGTH_SHORT).show();
                        dismiss(); // Close the dialog after submission
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        buttonSubmit.setEnabled(true); // Re-enable the submit button
                        Toast.makeText(getContext(), "Failed to submit data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        return builder.create();
    }
}
