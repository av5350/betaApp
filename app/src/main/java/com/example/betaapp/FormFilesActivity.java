package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The type Form files activity.
 */
public class FormFilesActivity extends AppCompatActivity {
    Uri photoUri;
    Button clickedBtn;
    SeekBar seekbarState;
    boolean nowUploading = false;
    HashMap<String, Integer> buttons = new HashMap<>();

    // the view of the button that was clicked (to know what name of file it would be in firebase)
    View selectedView;

    // every item in a set can be once (number of green buttons)
    Set<String> greenButtonsRequired = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_files);

        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        // init all buttons (later we will change their color to green/red)
        buttons.put("last_year_certificate_A", R.id.lastYearCertificateA);
        buttons.put("last_year_certificate_B", R.id.lastYearCertificateB);
        buttons.put("two_year_certificate_B", R.id.twoYearCertificateB);
        buttons.put("parent_id", R.id.parentIdBtn);
        buttons.put("student_picture", R.id.studentPicture);
        buttons.put("more_files", R.id.moreFiles);

        // the user could not change the seekbar state by clicking
        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
        initUI();
    }

    /**
     * Init the ui by changing the color (to green) of the button next to field
     * that there is a file for it in the db
     */
    private void initUI() {
        FBref.storageRef.child("files/" + Helper.currentStudentId).listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Button button;
                        for (StorageReference item : listResult.getItems()) {
                            // get the button from its name in the db and set its color to green
                            // (for this field, file was uploaded)
                            button = (Button) findViewById(buttons.get(item.getName()));
                            button.setBackgroundColor(Color.GREEN);

                            // we don't want the more_files to count in the set because its not a must field
                            if (!(item.getName()).equals("more_files"))
                                greenButtonsRequired.add(item.getName());
                        }
                    }
                });
    }

    /**
     * This function check if new file can by selected and be uploaded from the user's phone
     * (If there is no another file that is uploading rignt now to the firebase,
     * and that the user grant permission for writing to the storage)
     * If all good, the function calls to a function that creates the chooser intent of files
     * If not, the function informs the user
     *
     * @param view the view
     */
    public void getFileFromUser(View view) {
        // don't let user to start another upload before the current upload was done
        if (!nowUploading) {
            selectedView = view;

            // check if we have permission of saving the captured images
            if (ContextCompat.checkSelfPermission(FormFilesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(FormFilesActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            else {
                // if there is a permission already
                openFileChooserIntent();
            }
        }
        else
            Toast.makeText(FormFilesActivity.this, "חכה עד שנסיון ההעלאה הנכוחי יסתיים", Toast.LENGTH_SHORT).show();
    }

    /**
     * This function called after the user agree/not agree to let the application a permission
     * In our situation, if the user granted a permission to storage writing, the function
     *  would open the file chooser intent.
     * If the user didn't granted the permission - he would be inform about this
     *
     * @param requestCode the request code
     * @param permissions the requested permissions
     * @param grantResults if the permissions WERE granted (0) or denied (-1) [FOR EVERY PERMISSION]
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // if the permission granted - open the intent of choosing/capturing the file
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooserIntent();
            } else {
                Toast.makeText(this, "בשביל להמשיך יש לאשר הרשאת אחסון לאפליקציה!", Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * This function opens a chooser intent between capturing photo from camera or get file from phone's files.
     * After that, the function shows to the user the chooser.
     */
    private void openFileChooserIntent()
    {
        // Create a new intent and set its type to image
        Intent pickIntent = new Intent();
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        // just student_picture field needs to open image selector (the others are also with pdf chooser)
        if (!selectedView.getTag().equals("student_picture")) {
            pickIntent.setType("*/*");
            String[] extraMimeTypes = {"application/pdf", "image/*"}; // allowed types of files
            pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
        } else { // if want just image and not pdf
            pickIntent.setType("image/*");
        }

        // Intent for camera activity to capture a new picture
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "image.jpg");

        photoUri = Uri.fromFile(tempFile);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        // Title of the popup
        String pickTitle = "בחר קובץ";
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePhotoIntent}
                );

        // the button that was clicked (there is there a tag)
        clickedBtn = (Button) selectedView;

        startActivityForResult(chooserIntent, 123);
    }

    /**
     * This function is called after a file was selected from the local phone storage
     * Or after an image was captured from camera
     * This function upload the received photo/file to firebase for the student's form
     *
     * @param requestCode the request code
     * @param resultCode the result code (ok or canceled)
     * @param imageReturnedIntent there is the uri for the selected file
     *                            (if we go to this function after capturing a picture -
     *                                  this parameter would be null)
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                // make that another upload can start now
                nowUploading = true;

                // if returned From Camera (there is no returned intent)
                if ((imageReturnedIntent == null) || (imageReturnedIntent.getData() == null))
                {
                    uploadFileToFirebase(photoUri, (String) clickedBtn.getTag());
                }
                else // from storage
                {
                    Uri selectedFile = imageReturnedIntent.getData();
                    uploadFileToFirebase(selectedFile, (String) clickedBtn.getTag());
                }
            }
        }
    }

    /**
     * This function upload a file to firebase storage under custom name
     *
     * @param file the uri to the file to upload
     * @param fileName the name of the file in firebase
     **/
    private void uploadFileToFirebase(Uri file, String fileName)
    {
        // we save the file under a folder with the id of the student
        UploadTask uploadTask = FBref.storageRef.child("files/"+ Helper.currentStudentId + "/" + fileName).putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // if there is a file for this field that is already in the db: just make message to user
                if (((ColorDrawable)clickedBtn.getBackground()).getColor() == Color.GREEN)
                {
                    Toast.makeText(FormFilesActivity.this, "הקובץ שנבחר לא הועלה, לא נמחק הקובץ האחרון במערכת", Toast.LENGTH_SHORT).show();
                }
                else {
                    clickedBtn.setBackgroundColor(Color.RED);
                    Toast.makeText(FormFilesActivity.this, "הקובץ שנבחר לא הועלה", Toast.LENGTH_SHORT).show();
                }
                nowUploading = false;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                clickedBtn.setBackgroundColor(Color.GREEN);
                Toast.makeText(FormFilesActivity.this, "הקובץ שנבחר הועלה", Toast.LENGTH_SHORT).show();
                nowUploading = false;

                // we dont want the more_files to count in the set (of the Required files that were uploaded)
                if (!(clickedBtn.getTag()).equals("more_files"))
                    greenButtonsRequired.add((String) clickedBtn.getTag());
            }
        });
    }

    /**
     * This function is called after the user clicked on the finish form button
     * If for all the required fields there is a file that was uploaded in firebase,
     *  the function would submit the form and move the user to the select child activity.
     * Otherwise, the function would inform the user that he must upload files for all the fields
     *
     * @param view the view
     */
    public void finishForm(View view) {
        // if all the *must* fields are completed
        if (greenButtonsRequired.size() == 5)
        {
            FBref.refStudents.child(Helper.currentStudentId).child("formState").setValue(1);
            FBref.refStudents.child(Helper.currentStudentId).child("status").setValue(1);
            Toast.makeText(FormFilesActivity.this, "השאלון התקבל בהצלחה", Toast.LENGTH_SHORT).show();

            Intent si = new Intent(FormFilesActivity.this, SelectChildActivity.class);
            si.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // make that the user could not return activities back (clear the stack)
            startActivity(si);
        }
        else
        {
            Toast.makeText(FormFilesActivity.this, "חובה להעלות קובץ לכל השדות המסומנים", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Go back to the last screen (mom's information activity)
     *
     * @param view the view
     */
    public void back(View view) {
        finish();
    }

    /**
     * Create the options menu
     *
     * @param menu the menu
     * @return ture if success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Where to go when the menu item was selected
     *
     * @param item The menu item that was selected.
     * @return true - if it success
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // log out the user
        if (id == R.id.logout)
        {
            Helper.logout(getApplicationContext());
        }

        return true;
    }
}