package com.example.imageuploaderapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class RegisterFormFragment extends DialogFragment {

    private EditText editTextName;
    private EditText editTextAddress;
    private EditText editTextCrops;
    private EditText editTextLivestock;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_register_form, null);

        editTextName = view.findViewById(R.id.editTextName);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        editTextCrops = view.findViewById(R.id.editTextCrops);
        editTextLivestock = view.findViewById(R.id.editTextLivestock);
        Button buttonSubmit = view.findViewById(R.id.buttonSubmit);

        builder.setView(view)
                .setTitle("Farmer Registration");

        buttonSubmit.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String address = editTextAddress.getText().toString();
            String crops = editTextCrops.getText().toString();
            String livestock = editTextLivestock.getText().toString();

            // Handle the submitted data
            // TODO: Save data to a database or send it to a server

            dismiss(); // Close the dialog after submission
        });

        return builder.create();
    }
}
