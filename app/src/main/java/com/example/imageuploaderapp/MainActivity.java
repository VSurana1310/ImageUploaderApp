package com.example.imageuploaderapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;


//    private static final int CAMERA_REQUEST_CODE = 100;
//    private static final int IMAGE_CAPTURE_CODE = 101;
//    private static final int GALLERY_PICK_CODE = 102;
//    private static final int REQUEST_CODE_AUDIO_PERMISSION = 200;
    private static final int REQUEST_CODE_IMAGE = 1;
    private static final int REQUEST_CODE_CAMERA = 3;
    private static final int REQUEST_CODE_VOICE_INPUT = 2;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int AUDIO_REQUEST_CODE = 101;

    Uri imageUri;
    private Button buttonVoice;
    private LinearLayout linearLayoutImages;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private ImageView imageView;


    private TextView textViewVoiceInput;
    private EditText editTextSymptoms;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isRecording = false;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private FirebaseFirestore firestore;
    private CollectionReference collectionRef;

    private Button buttonUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button to open the registration form
        Button buttonRegisterFarmer = findViewById(R.id.openRegisterFormButton);
        buttonRegisterFarmer.setOnClickListener(v -> {
            RegisterFormFragment registerFormFragment = new RegisterFormFragment();
            registerFormFragment.show(getSupportFragmentManager(), "RegisterFormFragment");
        });

        //basic layout
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);

        }
        replaceFragment(new HomeFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.shorts) {
                replaceFragment(new ShortsFragment());
            } else if (itemId == R.id.subscriptions) {
                replaceFragment(new SubscriptionFragment());
            } else if (itemId == R.id.library) {
                replaceFragment(new LibraryFragment());
            }

            return true;
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });



        imageView = findViewById(R.id.imageView);
//        buttonImage = findViewById(R.id.button_image);
        buttonVoice = findViewById(R.id.button_voice);
//        textViewVoiceInput = findViewById(R.id.textView_voice_input);
        editTextSymptoms = findViewById(R.id.editText_symptoms);
        linearLayoutImages = findViewById(R.id.linearLayout_images);
        Button buttonChooseImage = findViewById(R.id.button_choose_image);

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
//        requestPermissions();

        // Button for image selection or capture
//        buttonImage.setOnClickListener(v -> showImageChoiceDialog());

        // Button for voice input
//        buttonVoice.setOnClickListener(v -> toggleVoiceRecording());

        // Set recognition listener for voice input
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

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
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    String currentText = editTextSymptoms.getText().toString();
                    editTextSymptoms.setText(currentText + " " + recognizedText);
                    // Restart listening for continuous input
                    if (isRecording) {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                }
            }


            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Handle button click for combined upload
        buttonUpload.setOnClickListener(v -> uploadDataToFirestore());
        // Prepare the speech recognizer intent
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        buttonVoice.setOnClickListener(v -> toggleVoiceRecording());

        // Choose image button listener for both camera and gallery
        buttonChooseImage.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                showImageOptions();
            } else {
                requestCameraPermission();
            }
        });
    }

    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);  // dialog thoda dekh ke
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  // windwo bhi
        dialog.setContentView(R.layout.bottom_header);

        LinearLayout videoLayout = dialog.findViewById(R.id.layoutVideo);
        LinearLayout shortsLayout = dialog.findViewById(R.id.layoutShorts);
        LinearLayout liveLayout = dialog.findViewById(R.id.layoutLive);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(MainActivity.this,"Upload a Video is clicked",Toast.LENGTH_SHORT).show();

            }
        });

        shortsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(MainActivity.this,"Create a short is Clicked",Toast.LENGTH_SHORT).show();

            }
        });

        liveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(MainActivity.this,"Go live is Clicked",Toast.LENGTH_SHORT).show();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }




    private void toggleVoiceRecording() {
        if (isRecording) {
            stopRecording();
            buttonVoice.setText("Start Recording");
        } else {
            startRecording();
            buttonVoice.setText("Stop Recording");
        }
        isRecording = !isRecording;
    }

    private void startRecording() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechRecognizerIntent);
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    // Show options to choose between camera and gallery
    private void showImageOptions() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create chooser intent
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select or Capture Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        startActivityForResult(chooserIntent, REQUEST_CODE_IMAGE);
    }


    // Add selected images to horizontal scroll view
    private void addImageToScrollView(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        // Set the desired size for the ImageView
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(800, 800); // Increase size as needed
        layoutParams.setMargins(10, 10, 10, 10);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Maintain aspect ratio
        imageView.setImageURI(imageUri);
        linearLayoutImages.addView(imageView);
    }

    // Handle the result of image capture or selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        // Multiple images selected
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            addImageToScrollView(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // Single image selected or captured
                        Uri imageUri = data.getData();
                        if (imageUri != null) {
                            if (imageUri.toString().contains("capture")) {
                                // Image captured from camera
                                addImageToScrollView(imageUri);
                            } else {
                                // Image selected from gallery
                                addImageToScrollView(imageUri);
                            }
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Action canceled", Toast.LENGTH_SHORT).show();
        }
        enableUploadButton();
    }

    // Upload images and text to Firestore
    private void uploadDataToFirestore() {
        if (!imageUris.isEmpty()) {
            final ArrayList<String> uploadedImageUrls = new ArrayList<>();
            final int totalImages = imageUris.size(); // Total number of images
            final int[] uploadCounter = {0}; // Counter to track the number of uploads

            for (Uri imageUri : imageUris) {
                StorageReference fileRef = storageRef.child("images/" + imageUri.getLastPathSegment());

                fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            uploadedImageUrls.add(downloadUrl.toString());

                            // Increment the counter when each image is uploaded
                            uploadCounter[0]++;

                            // If all images are uploaded, upload text and image URLs to Firestore
                            if (uploadCounter[0] == totalImages) {
                                uploadToFirestore(uploadedImageUrls);
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                ).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            Toast.makeText(this, "Please provide both images and text", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload image URLs and text to Firestore
    private void uploadToFirestore(ArrayList<String> imageUrls) {
        String text = editTextSymptoms.getText().toString();

        // Create a new document in Firestore
        Map<String, Object> document = new HashMap<>();
        document.put("imageUrls", imageUrls); // List of image URLs
        document.put("text", text);

        collectionRef.add(document)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to upload data", Toast.LENGTH_SHORT).show();
                });
    }


    // Enable the upload button when both images and text are available
    private void enableUploadButton() {
        if (!imageUris.isEmpty()) {
            buttonUpload.setEnabled(true);
        } else {
            buttonUpload.setEnabled(false);
        }
    }
//    private Uri imageUri;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.imageuploaderapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }


    // Check for camera permission
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    // Check for audio permission
    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageOptions();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleVoiceRecording();
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
