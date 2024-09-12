package com.example.imageuploaderapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_CAPTURE_CODE = 101;
    private static final int GALLERY_PICK_CODE = 102;
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 200;

    ImageView imageView;
    Uri imageUri;

    private TextView textViewVoiceInput;
    private EditText editTextSymptoms;
    private SpeechRecognizer speechRecognizer;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private FirebaseFirestore firestore;
    private CollectionReference collectionRef;

    private Button buttonUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button buttonImage = findViewById(R.id.button_image);
        Button buttonVoice = findViewById(R.id.button_voice);
        textViewVoiceInput = findViewById(R.id.textView_voice_input);
        editTextSymptoms = findViewById(R.id.editText_symptoms);
        buttonUpload = findViewById(R.id.button_upload);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();
        firestore = FirebaseFirestore.getInstance();
        collectionRef = firestore.collection("documents");

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Request permissions
        requestPermissions();

        // Button for image selection or capture
        buttonImage.setOnClickListener(v -> showImageChoiceDialog());

        // Button for voice input
        buttonVoice.setOnClickListener(v -> startVoiceRecognition());

        // Set recognition listener for voice input
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(MainActivity.this, "Error recognizing speech", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String recognizedText = matches.get(0);
                    textViewVoiceInput.setText(recognizedText);
                    editTextSymptoms.setText(recognizedText);

                    // Enable the upload button
                    enableUploadButton();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Handle button click for combined upload
        buttonUpload.setOnClickListener(v -> uploadDataToFirestore());
    }

    // Request necessary permissions
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_CODE_AUDIO_PERMISSION);
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Show dialog to choose between taking a photo or selecting an image from gallery
    private void showImageChoiceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Action");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.create().show();
    }

    // Open the camera to take a photo
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    // Open the gallery to choose an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_PICK_CODE);
    }

    // Start the voice recognition process
    private void startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");

            // Start listening for speech
            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "Audio permission not granted!", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result of image capture or selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                imageView.setImageURI(imageUri);
                enableUploadButton();
            } else if (requestCode == GALLERY_PICK_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
                imageUri = selectedImageUri; // Update imageUri
                enableUploadButton();
            }
        }
    }

    // Upload image and text to Firestore
    private void uploadDataToFirestore() {
        if (imageUri != null && !editTextSymptoms.getText().toString().isEmpty()) {
            // Upload image to Firebase Storage
            StorageReference fileRef = storageRef.child("images/" + imageUri.getLastPathSegment());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        String imageUrl = downloadUrl.toString();
                        String text = editTextSymptoms.getText().toString();

                        // Create a new document in Firestore
                        Map<String, Object> document = new HashMap<>();
                        document.put("imageUrl", imageUrl);
                        document.put("text", text);

                        collectionRef.add(document)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(MainActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to upload data", Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please provide both image and text", Toast.LENGTH_SHORT).show();
        }
    }

    // Enable the upload button when both image and text are available
    private void enableUploadButton() {
        if (imageUri != null && !editTextSymptoms.getText().toString().isEmpty()) {
            buttonUpload.setEnabled(true);
        } else {
            buttonUpload.setEnabled(false);
        }
    }
}
