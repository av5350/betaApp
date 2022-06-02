package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

public class FormFilesActivity extends AppCompatActivity {
    Uri photoUri;
    Button clickedBtn;
    boolean nowUploading = false;
    HashMap<String, Integer> buttons = new HashMap<>();
    Set<String> greenButtons = new HashSet<String>(); // every item in a set can be once (number of green buttons)
    SeekBar seekbarState;

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

                            // we dont want the more_files to count in the set
                            if (!(item.getName()).equals("more_files"))
                                greenButtons.add(item.getName());
                        }
                    }
                });
    }

    public void getFileFromUser(View view) {
        // don't let user to start another upload before the current upload was done
        if (!nowUploading) {
            // Create a new intent and set its type to image
            Intent pickIntent = new Intent();
            pickIntent.setAction(Intent.ACTION_GET_CONTENT);

            if (!view.getTag().equals("student_picture")) {
                pickIntent.setType("*/*");
                String[] extraMimeTypes = {"application/pdf", "image/*"}; // allowed types of files
                pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
            } else { // if want just image and not pdf
                pickIntent.setType("image/*");
            }

            // Intent for camera activity to capture a new picture
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "aabb.jpg");

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

            clickedBtn = (Button) view; // the button that was clicked
            startActivityForResult(chooserIntent, 123);
        }
        else
            Toast.makeText(FormFilesActivity.this, "חכה עד שנסיון ההעלאה הנכוחי יסתיים", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
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

    private void uploadFileToFirebase(Uri file, String fileName)
    {
        UploadTask uploadTask = FBref.storageRef.child("files/"+ Helper.currentStudentId + "/" + fileName).putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // if there is a file in the db: just make message to user
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

                // we dont want the more_files to count in the set
                if (!(clickedBtn.getTag()).equals("more_files"))
                    greenButtons.add((String) clickedBtn.getTag());
            }
        });
    }

    public void back(View view) {
        finish();
    }

    public void finishForm(View view) {
        // if all the *must* fields are completed
        if (greenButtons.size() == 5)
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
}