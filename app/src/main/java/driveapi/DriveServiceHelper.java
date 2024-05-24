package driveapi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.Formatter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    public static final int REQUEST_CODE_WRITE_PERMISSION = 1001;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive driveService;
    private Context context = null;

    public interface DriveServiceInitialization {
        void initializeDriveService();
    }
    public DriveServiceHelper(Drive driveService, Context context) {
        this.driveService = driveService;
        this.context = context;
    }

    public void listFiles(int idGrupo) {
        String folderId = "1mgmVsFktA71OVL2NkcgbSopkfDZofbna";
        String query = "'" + folderId + "' in parents and trashed = false";
        FileList result = null;
        try {
            result = driveService.files().list()
                    .setQ(query)
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, webViewLink)")
                    .execute();
            for (File file : result.getFiles()) {
                Log.e("hey", "Found file: "+file.getName()+file.getId() + "link     " + file.getWebViewLink());

//                Uri contentUri = Uri.parse("content://com.mariana.myapplication/files/" + file.getId());
//                Log.i("uri", contentUri.toString());
                if (archivoNecesario(file.getName(), idGrupo)) {
                    downloadFileToAppDataDirectory(file.getId(), file.getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public DatosArchivo uploadImageFile(Uri fileUri, int idGrupo, int idAlbum) {
        String folderId = "1mgmVsFktA71OVL2NkcgbSopkfDZofbna";
        File googleFile = null;
        try {
            // Get the MIME type of the file
            String mimeType = context.getContentResolver().getType(fileUri);
            Log.e("ID", mimeType);
            if (null != mimeType && (mimeType.equals("image/png") || mimeType.equals("image/jpeg"))) {
                // Get the file name
                String fileName = String.format("%03d", idGrupo) + String.format("%05d", idAlbum) + generateTimeHash() + "." + mimeType.substring(6);

                // Create a temporary file
                java.io.File tempFile = java.io.File.createTempFile("temp", null, context.getCacheDir());
                try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                     FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                    if (inputStream != null) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                // Create the file metadata
                File metadata = new File()
                        .setName(fileName)
                        .setMimeType(mimeType)
                        .setParents(Collections.singletonList(folderId));

                // Create a FileContent object with the temporary file
                FileContent fileContent = new FileContent(mimeType, tempFile);

                // Upload the file
                googleFile = driveService.files().create(metadata, fileContent).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }
                else {
                    downloadFileToAppDataDirectory(googleFile.getId(), googleFile.getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new DatosArchivo(googleFile.getId(), googleFile.getName());
    }


    public Task<Void> deleteFile(String fileId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        mExecutor.execute(() -> {
            try {
                driveService.files().delete(fileId).execute();
                taskCompletionSource.setResult(null);
            } catch (Exception e) {
                taskCompletionSource.setException(e);
            }
        });
        return taskCompletionSource.getTask();
    }


    public void downloadFileToAppDataDirectory(String fileId, String fileName) throws RuntimeException {
        try {
            // Get the application's internal files directory
            java.io.File directory = context.getExternalFilesDir(null);
            java.io.File file = new java.io.File(directory, fileName);

            // Create a new FileOutputStream for the file
            FileOutputStream fos = new FileOutputStream(file);

            // Download the file content from Drive
            InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();

            // Read from the InputStream and write to the FileOutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            // Close the streams
            fos.close();
            is.close();

        } catch (IOException e) {
            Log.e("Error", "Failed to download file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private Task<Void> downloadFileUsingMediaStore(Drive driveService, String fileId, String fileName) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        mExecutor.execute(() -> {
            // Define the ContentValues with metadata about the file
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream"); // MIME type of the file, change accordingly
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            // Insert the metadata to MediaStore and get the Uri to write the file
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            }
            if (uri != null) {
                try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
                     OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    Log.e("URII", uri.toString() );
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                } catch (Exception e) {
                    Log.e("Error", "Failed to download file: " + e.getMessage());
                    // Optionally, delete the incomplete entry in MediaStore on failure
                    context.getContentResolver().delete(uri, null, null);
                    context.getContentResolver().delete(uri, null, null);
                    taskCompletionSource.setException(e);
                }
            } else {
                taskCompletionSource.setException(new Exception("Failed to create MediaStore entry"));
                Log.e("Error", "Could not create MediaStore entry for the download.");
            }
        });
        return taskCompletionSource.getTask();
    }


    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        return intent;
    }



    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    public boolean archivoNecesario(String str, int number) {
        // Check if the string has at least three characters
        if (str != null && str.length() >= 3) {
            // Extract the first three characters as a substring
            String firstThreeChars = str.substring(0, 3);
            try {
                // Parse the substring as an integer and compare with the specified number
                return Integer.parseInt(firstThreeChars) == number;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public static String generateTimeHash() throws NoSuchAlgorithmException {
        long currentTimeMillis = System.currentTimeMillis();
        String input = String.valueOf(currentTimeMillis);

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());

        // Convert bytes to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    // Métodos auxiliares

//    private void downloadFile(Drive driveService, String fileId, String fileName) {
//        try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
//            // OutputStream os = new FileOutputStream(new java.io.File(context.getFilesDir(), fileName))) {
//            OutputStream os = Files.newOutputStream(new java.io.File(context.getFilesDir(), fileName).toPath())) {
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = is.read(buffer)) != -1) {
//                os.write(buffer, 0, read);
//            }
//        } catch (Exception e) {
//            Log.e("Error", "Failed to download file: " + e.getMessage());
//        }
//    }






//    public Task<Void> downloadFile(Context context, String fileId, String fileName) {
//        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
//        mExecutor.execute(() -> {
//            Uri uri = getOutputUri(fileName);
//            requestWriteAccess(uri);
//            try {
//                try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
//                     OutputStream os = context.getContentResolver().openOutputStream(uri)) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = is.read(buffer)) != -1) {
//                        os.write(buffer, 0, bytesRead);
//                    }
//                }
//                taskCompletionSource.setResult(null);
//            } catch (Exception e) {
//                taskCompletionSource.setException(e);
//            }
//        });
//        return taskCompletionSource.getTask();
//    }


//    public interface PermissionResultListener {
//        void onRequestPermissionsResult(int requestCode, int resultCode);
//    }
}
