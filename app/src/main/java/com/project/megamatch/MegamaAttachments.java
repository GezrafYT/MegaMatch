package com.project.megamatch;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.megamatch.megamaCreate.Megama;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MegamaAttachments extends AppCompatActivity {
    private static final String TAG = "MegamaAttachments";

    // UI components
    private TextView greetingText, megamaText;
    private Button backButton, createMegamaButton, addUrlImageButton;
    private ImageButton expandImageSectionButton;
    private EditText imageUrlInput;
    private LinearLayout imageSection;
    private FrameLayout addImageButton;
    private RecyclerView selectedImagesRecyclerView;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore fireDB;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Data
    private String schoolId;
    private String username;
    private String megamaName;
    private String megamaDescription;
    private boolean requiresExam;
    private boolean requiresGradeAvg;
    private int requiredGradeAvg;
    private ArrayList<String> customConditions;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private ArrayList<String> uploadedImageUrls = new ArrayList<>();

    // Image handling
    private Uri currentPhotoUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ImagesAdapter imagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.megama_attachments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.megamaAttachments), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        fireDB = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI components
        initializeViews();
        setupImagePickers();
        setupStorageAndDatabase();
        setupListeners();
        
        // Check if we're in update mode with existing images
        boolean isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        if (isUpdate) {
            // Set button text to update
            createMegamaButton.setText("עדכון מגמה");
            
            // Load existing images
            ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    uploadedImageUrls.add(imageUrl);
                }
                imagesAdapter.notifyDataSetChanged();
                updateImageCountText();
                
                // Automatically expand the image section when there are existing images
                imageSection.setVisibility(View.VISIBLE);
                expandImageSectionButton.setImageResource(android.R.drawable.arrow_up_float);
            }
        }
    }

    private void initializeViews() {
        greetingText = findViewById(R.id.greetingText);
        megamaText = findViewById(R.id.megamaText);
        backButton = findViewById(R.id.backButton);
        createMegamaButton = findViewById(R.id.createMegamaButton);
        expandImageSectionButton = findViewById(R.id.expandImageSectionButton);
        imageSection = findViewById(R.id.imageSection);
        addImageButton = findViewById(R.id.addImageButton);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        addUrlImageButton = findViewById(R.id.addUrlImageButton);
        selectedImagesRecyclerView = findViewById(R.id.selectedImagesRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        selectedImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imagesAdapter = new ImagesAdapter(selectedImageUris);
        selectedImagesRecyclerView.setAdapter(imagesAdapter);
    }

    private void setupImagePickers() {
        // Image picker launcher (Gallery)
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            addImageToSelection(imageUri);
                        }
                    }
                });

        // Camera launcher
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoUri != null) {
                        addImageToSelection(currentPhotoUri);
                    }
                });
    }

    private void setupStorageAndDatabase() {
        // Get data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            schoolId = extras.getString("schoolId", "");
            username = extras.getString("username", "");
            megamaName = extras.getString("megamaName", "");
            megamaDescription = extras.getString("megamaDescription", "");
            requiresExam = extras.getBoolean("requiresExam", false);
            requiresGradeAvg = extras.getBoolean("requiresGradeAvg", false);
            requiredGradeAvg = extras.getInt("requiredGradeAvg", 0);
            customConditions = extras.getStringArrayList("customConditions");

            // If we're in update mode and any data seems missing, fetch it from Firestore
            boolean isUpdate = extras.getBoolean("isUpdate", false);
            if (isUpdate && (megamaName != null && !megamaName.isEmpty()) && 
                (megamaDescription.isEmpty() || customConditions == null || customConditions.isEmpty())) {
                fetchMegamaDataFromFirestore(megamaName);
            } else {
                // Update the UI with megama details from intent
                updateUIWithMegamaDetails();
            }
        }
    }

    private void fetchMegamaDataFromFirestore(String megamaName) {
        fireDB.collection("schools").document(schoolId)
              .collection("megamot").document(megamaName)
              .get()
              .addOnSuccessListener(documentSnapshot -> {
                  if (documentSnapshot.exists()) {
                      // Get data from Firestore document
                      if (megamaDescription.isEmpty()) {
                          String description = documentSnapshot.getString("description");
                          if (description != null) {
                              megamaDescription = description;
                          }
                      }
                      
                      // Get requires exam value
                      Boolean examValue = documentSnapshot.getBoolean("requiresExam");
                      if (examValue != null) {
                          requiresExam = examValue;
                      }
                      
                      // Get requires grade avg value
                      Boolean gradeAvgValue = documentSnapshot.getBoolean("requiresGradeAvg");
                      if (gradeAvgValue != null) {
                          requiresGradeAvg = gradeAvgValue;
                      }
                      
                      if (requiredGradeAvg == 0 && requiresGradeAvg) {
                          Long requiredGradeAvgLong = documentSnapshot.getLong("requiredGradeAvg");
                          requiredGradeAvg = requiredGradeAvgLong != null ? requiredGradeAvgLong.intValue() : 0;
                      }
                      
                      if (customConditions == null || customConditions.isEmpty()) {
                          customConditions = (ArrayList<String>) documentSnapshot.get("customConditions");
                      }
                      
                      // Now get image URLs if they aren't already set
                      ArrayList<String> fetchedImageUrls = (ArrayList<String>) documentSnapshot.get("imageUrls");
                      if (fetchedImageUrls != null && !fetchedImageUrls.isEmpty() && uploadedImageUrls.isEmpty()) {
                          uploadedImageUrls.addAll(fetchedImageUrls);
                          imagesAdapter.notifyDataSetChanged();
                          updateImageCountText();
                      }
                  }
                  
                  // Update UI after fetching data
                  updateUIWithMegamaDetails();
              })
              .addOnFailureListener(e -> {
                  Log.e(TAG, "Error fetching megama data: " + e.getMessage());
                  // Continue with UI update even if fetch fails
                  updateUIWithMegamaDetails();
              });
    }

    private void updateUIWithMegamaDetails() {
        fireDB.collection("schools").document(schoolId)
                .collection("rakazim").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // טעינת שם פרטי
                        String firstName = documentSnapshot.getString("firstName");
                        if (firstName != null && !firstName.isEmpty()) {
                            greetingText.setText("שלום " + firstName);
                        } else {
                            greetingText.setText("שלום " + username);
                        }

                        // טעינת שם מגמה
                        megamaName = documentSnapshot.getString("megama");
                        if (megamaName != null && !megamaName.isEmpty()) {
                            megamaText.setText("מגמת " + megamaName + "!");
                        } else {
                            megamaText.setText("יצירת מגמה חדשה!");
                        }
                    } else {
                        greetingText.setText("שלום " + username);
                        megamaText.setText("יצירת מגמה חדשה!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MegamaCreate", "Error loading rakaz details: " + e.getMessage());
                    greetingText.setText("שלום " + username);
                    megamaText.setText("יצירת מגמה חדשה!");
                });
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            goBackToMegamaCreate();
        });

        // Expand/collapse image section
        expandImageSectionButton.setOnClickListener(v -> {
            if (imageSection.getVisibility() == View.VISIBLE) {
                imageSection.setVisibility(View.GONE);
                expandImageSectionButton.setImageResource(android.R.drawable.arrow_down_float);
            } else {
                imageSection.setVisibility(View.VISIBLE);
                expandImageSectionButton.setImageResource(android.R.drawable.arrow_up_float);
            }
        });

        // Add image button (shows bottom sheet dialog)
        addImageButton.setOnClickListener(v -> {
            showImageSourceOptions();
        });

        // Add URL image button
        addUrlImageButton.setOnClickListener(v -> {
            String imageUrl = imageUrlInput.getText().toString().trim();
            if (!imageUrl.isEmpty()) {
                if (isValidImageUrl(imageUrl)) {
                    Uri imageUri = Uri.parse(imageUrl);
                    addImageToSelection(imageUri);
                    imageUrlInput.setText("");
                } else {
                    Toast.makeText(this, "נא להזין כתובת תקינה של תמונה", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "נא להזין כתובת לתמונה", Toast.LENGTH_SHORT).show();
            }
        });

        // Create megama button
        createMegamaButton.setOnClickListener(v -> {
            createNewMegama();
        });
    }

    private void showImageSourceOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);
        dialog.setContentView(bottomSheetView);

        View cameraOption = bottomSheetView.findViewById(R.id.camera_option);
        View galleryOption = bottomSheetView.findViewById(R.id.gallery_option);

        cameraOption.setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });

        galleryOption.setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialog.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "שגיאה ביצירת קובץ תמונה", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.project.megamatch.fileprovider",
                        photoFile);
                takePictureLauncher.launch(currentPhotoUri);
            }
        } else {
            Toast.makeText(this, "אין אפליקציית מצלמה זמינה", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void addImageToSelection(Uri imageUri) {
        selectedImageUris.add(imageUri);
        imagesAdapter.notifyItemInserted(selectedImageUris.size() - 1);
    }

    private boolean isValidImageUrl(String url) {
        // Basic validation for image URLs
        return url.matches(".*\\.(jpeg|jpg|png|gif|bmp)$") || 
               url.startsWith("http") || 
               url.startsWith("https");
    }

    private void goBackToMegamaCreate() {
        Intent intent = new Intent();
        intent.putExtra("shouldPreserveData", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void createNewMegama() {
        if (!validateImageUrls()) return;

        // שינוי כפתור והוספת לואדר
        createMegamaButton.setEnabled(false);
        createMegamaButton.setText("");
        progressBar.setVisibility(View.VISIBLE);

        // Check if we're in update mode
        boolean isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        
        // Create Megama object with all parameters
        Megama megama = new Megama();
        megama.setName(megamaName);
        megama.setDescription(megamaDescription);
        megama.setImageUrls(uploadedImageUrls);
        megama.setRequiresExam(requiresExam);
        megama.setRequiresGradeAvg(requiresGradeAvg);
        megama.setRequiredGradeAvg(requiredGradeAvg);
        megama.setCustomConditions(customConditions);

        // Save to Firestore
        CollectionReference megamotRef = fireDB.collection("schools").document(schoolId)
                                              .collection("megamot");
        
        // Update or create based on mode
        if (isUpdate) {
            megamotRef.document(megamaName)
                .set(megama)
                .addOnSuccessListener(aVoid -> {
                    // Update rakaz megama reference
                    updateRakazMegama();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    createMegamaButton.setText("עדכן מגמה");
                    createMegamaButton.setEnabled(true);
                    Toast.makeText(MegamaAttachments.this, "שגיאה בעדכון מגמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            // Check if megama already exists
            megamotRef.document(megamaName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        createMegamaButton.setText("המשך ליצירת מגמה");
                        createMegamaButton.setEnabled(true);
                        Toast.makeText(MegamaAttachments.this, "מגמה בשם זה כבר קיימת", Toast.LENGTH_SHORT).show();
                    } else {
                        megamotRef.document(megamaName)
                            .set(megama)
                            .addOnSuccessListener(aVoid -> {
                                // Update rakaz megama reference
                                updateRakazMegama();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                createMegamaButton.setText("המשך ליצירת מגמה");
                                createMegamaButton.setEnabled(true);
                                Toast.makeText(MegamaAttachments.this, "שגיאה ביצירת מגמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    createMegamaButton.setText("המשך ליצירת מגמה");
                    createMegamaButton.setEnabled(true);
                    Toast.makeText(MegamaAttachments.this, "שגיאה בבדיקת מגמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private boolean validateImageUrls() {
        if (selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty()) {
            Toast.makeText(this, "נא להוסיף תמונות למגמה", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateImageCountText() {
        int totalImages = selectedImageUris.size() + uploadedImageUrls.size();
        String imageCountText = "תמונות נבחרות: " + totalImages;
        
        // Find the TextView by its text content since we don't have a direct reference
        for (int i = 0; i < ((ViewGroup) imageSection).getChildCount(); i++) {
            View child = ((ViewGroup) imageSection).getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (textView.getText().toString().startsWith("תמונות נבחרות")) {
                    textView.setText(imageCountText);
                    break;
                }
            }
        }
        
        // Show empty state message if no images
        if (totalImages == 0) {
            // We could add an empty state view here if needed
        }
    }

    private void updateRakazMegama() {
        // Update the rakaz document to reference this megama
        fireDB.collection("schools").document(schoolId)
              .collection("rakazim").document(username)
              .update("megama", megamaName)
              .addOnSuccessListener(aVoid -> {
                  // Success - navigate to success page or back to rakaz home
                  progressBar.setVisibility(View.GONE);
                  Toast.makeText(MegamaAttachments.this, 
                          "מגמה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show();
                  
                  // Navigate back to rakaz page
                  Intent intent = new Intent(MegamaAttachments.this, rakazPage.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  startActivity(intent);
                  finish();
              })
              .addOnFailureListener(e -> {
                  // Failed to update rakaz
                  progressBar.setVisibility(View.GONE);
                  createMegamaButton.setEnabled(true);
                  createMegamaButton.setText("נסה שוב");
                  
                  Toast.makeText(MegamaAttachments.this, 
                          "שגיאה בהגדרת רכז למגמה: " + e.getMessage(), 
                          Toast.LENGTH_SHORT).show();
              });
    }

    // Adapter for the images RecyclerView
    private class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {
        private final List<Uri> imageUris;
        private final List<String> uploadedUrls;

        public ImagesAdapter(List<Uri> imageUris) {
            this.imageUris = imageUris;
            this.uploadedUrls = uploadedImageUrls; // Reference the class member
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_selected_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            if (position < imageUris.size()) {
                // This is a local Uri from selectedImageUris
                Uri imageUri = imageUris.get(position);
                
                // Load image using Glide
                Glide.with(MegamaAttachments.this)
                        .load(imageUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(holder.imageView);
                
                // Remove image button
                holder.removeButton.setOnClickListener(v -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < imageUris.size()) {
                        imageUris.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        updateImageCountText();
                    }
                });
            } else {
                // This is a remote URL from uploadedImageUrls
                int uploadedPosition = position - imageUris.size();
                if (uploadedPosition < uploadedUrls.size()) {
                    String imageUrl = uploadedUrls.get(uploadedPosition);
                    
                    // Load image using Glide
                    Glide.with(MegamaAttachments.this)
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(holder.imageView);
                    
                    // Remove image button
                    holder.removeButton.setOnClickListener(v -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            int uploadedIndex = adapterPosition - imageUris.size();
                            if (uploadedIndex >= 0 && uploadedIndex < uploadedUrls.size()) {
                                uploadedUrls.remove(uploadedIndex);
                                notifyItemRemoved(adapterPosition);
                                updateImageCountText();
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return imageUris.size() + uploadedUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageButton removeButton;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.selected_image);
                removeButton = itemView.findViewById(R.id.remove_image_button);
            }
        }
    }
} 