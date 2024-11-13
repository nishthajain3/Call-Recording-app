package com.example.callrecording;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private static final String TAG = "UploadActivity";
    private Drive googleDriveService;
    private Handler mainHandler;
    private Set<String> existingFiles;
    private int totalFiles;
    private int checkedFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mainHandler = new Handler(Looper.getMainLooper());

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            initializeDriveService();
        }
    }

    private void initializeDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singletonList(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            try {
                googleDriveService = new Drive.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential)
                        .setApplicationName("My Drive App")
                        .build();
                uploadMusicFiles();  // Start uploading files after initializing the Drive service
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "Failed to create Google Drive service", e);
            }
        } else {
            Log.e(TAG, "GoogleSignInAccount is null");
            Toast.makeText(this, "Failed to get Google account", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadMusicFiles() {
        Log.d(TAG, "Starting to upload music files");
        java.io.File musicDir = new java.io.File("/storage/emulated/0/Recordings");
        if (musicDir.exists() && musicDir.isDirectory()) {
            java.io.File[] files = musicDir.listFiles();
            if (files != null) {
                totalFiles = files.length;
                checkedFiles = 0;
                existingFiles = new HashSet<>();

                for (java.io.File file : files) {
                    if (file.isFile() && (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav"))) {
                        Log.d(TAG, "Found file to upload: " + file.getName());
                        checkAndUploadFile(file);
                    } else {
                        totalFiles--;
                    }
                }
            } else {
                Log.d(TAG, "No files found in directory");
            }
        } else {
            Log.d(TAG, "Music directory does not exist or is not a directory");
        }
    }


    private void checkAndUploadFile(final java.io.File file) {
        new Thread(() -> {
            try {
                String query = "name = '" + file.getName() + "' and trashed = false";
                FileList result = googleDriveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute();
                if (result.getFiles().isEmpty()) {
                    uploadFileToDrive(file);
                } else {
                    synchronized (existingFiles) {
                        existingFiles.add(file.getName());
                    }
                }
                checkCompletion();
            } catch (IOException e) {
                Log.e(TAG, "Error checking file existence: " + e.getMessage());
            }
        }).start();
    }

    private void uploadFileToDrive(final java.io.File file) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Uploading file: " + file.getName());
                File fileMetadata = new File();
                fileMetadata.setName(file.getName());

                FileContent mediaContent = new FileContent("audio/wav", file);

                File uploadedFile = googleDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                Log.i(TAG, "File uploaded: " + uploadedFile.getId());

                mainHandler.post(() -> Toast.makeText(UploadActivity.this, "File uploaded: " + file.getName(), Toast.LENGTH_SHORT).show());
            } catch (UserRecoverableAuthIOException e) {
                Log.e(TAG, "UserRecoverableAuthIOException: " + e.getMessage());
                startActivityForResult(e.getIntent(), REQUEST_CODE_PERMISSIONS);
            } catch (IOException e) {
                Log.e(TAG, "File upload failed: " + e.getMessage());
            }
            checkCompletion();
        }).start();
    }


    private void checkCompletion() {
        synchronized (this) {
            checkedFiles++;
            if (checkedFiles == totalFiles) {
                mainHandler.post(() -> {
                    if (existingFiles.size() == totalFiles) {
                        Toast.makeText(UploadActivity.this, "All files already exist in your Drive.", Toast.LENGTH_SHORT).show();
                    } else if (totalFiles == 0) {
                        Toast.makeText(UploadActivity.this, "No files to upload.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeDriveService();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
