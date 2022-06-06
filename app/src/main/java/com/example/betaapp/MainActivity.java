package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * The type Main activity - login and signup activity.
 */
public class MainActivity extends AppCompatActivity {
    EditText mailET, phoneET, codeET;
    LinearLayout logInOptionLayout;
    Switch logInOption;
    TextView currentStateET;
    Button getSmsCodeBtn;
    CheckBox stayConnected;

    final Gson gson = new Gson();
    final boolean SIGN_UP_STATE = false;
    boolean currentState = SIGN_UP_STATE;

    private String storedVerificationId = null;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mailET = (EditText) findViewById(R.id.mailET);
        phoneET = (EditText) findViewById(R.id.phoneET);
        codeET = (EditText) findViewById(R.id.codeET);
        currentStateET = (TextView) findViewById(R.id.currentStateET);
        logInOptionLayout = (LinearLayout) findViewById(R.id.logInOptionLayout);
        logInOption = (Switch) findViewById(R.id.logInOption);
        getSmsCodeBtn = (Button) findViewById(R.id.getSmsCodeBtn);
        stayConnected = (CheckBox) findViewById(R.id.stayConnected);

        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);

        // Retrieve checkBox data (if want to stay connected)
        stayConnected.setChecked(signInState.getBoolean("wantStayConnected", false));

        // if mail was sent - try get the intent (the link redirects to this activity)
        if (signInState.getBoolean("isMailSent", false)) {
            finishEmailRegistration(signInState.getString("mail", ""));
        }
        // if the user saved the login credential - sign in the user
        else if (!signInState.getString("credential", "").equals(""))
        {
            if (signInState.getBoolean("isPhoneCredential", false))
                FBref.auth.signInWithCredential(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
            else
                FBref.auth.signInWithCredential(gson.fromJson(signInState.getString("credential", ""), EmailAuthCredential.class));
            switchRelevantActivity();
        }

        // the callbacks functions for the phone verification
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Toast.makeText(MainActivity.this, "התרחשה בעיה! אנא נסה שנית", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID so we can use it later
                storedVerificationId = verificationId;
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();

        // if the user is going to get into the app from email link for example, save the checkBox state
        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
        SharedPreferences.Editor editor = signInState.edit();
        editor.putBoolean("wantStayConnected", stayConnected.isChecked());
        editor.commit();
    }

    /**
     * If the user opens the link when he wants to signup - the function create the new user
     * If the user opens the link in login with mail option - the function moves the user to the next activity
     *
     * @param emailAddress the mail address that the link was sent to
     */
    private void finishEmailRegistration(String emailAddress)
    {
        Intent intent = getIntent();

        if (intent.getData() != null) {
            String emailLink = intent.getData().toString();

            // Confirm the link is a sign-in or sign-up email link(its the same function).
            if (FBref.auth.isSignInWithEmailLink(emailLink)) {
                FBref.auth.signInWithEmailLink(emailAddress, emailLink)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    // prevent log in with mail and then login with phone (or something else)
                                    SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = signInState.edit();
                                    editor.putBoolean("isMailSent", false);
                                    editor.commit();

                                    // if the link is not an sign-in link (so its sign-up)
                                    if(!signInState.getBoolean("isSignIn", SIGN_UP_STATE)) {
                                        // link the phone credential with the mail
                                        linkMailPhone(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
                                    }
                                    else { // sign in
                                        //  if its not a new user
                                        if (!(task.getResult().getAdditionalUserInfo().isNewUser())) {
                                            // if user wants to stay connected - save the email Credential
                                            if (stayConnected.isChecked())
                                                saveUserCredential(EmailAuthProvider.getCredentialWithLink(signInState.getString("mail", ""), emailLink), false);

                                            // move to the relevant activity (based on the user's role)
                                            switchRelevantActivity();
                                        }
                                        else
                                        {
                                            // because its new user - we need to delete it! (he created new user not from the signup page)
                                            FirebaseUser user = FBref.auth.getCurrentUser();
                                            user.delete();
                                            Toast.makeText(MainActivity.this, "חשבון לא נמצא!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else { // there was an error while trying to signup/login
                                    Toast.makeText(MainActivity.this, "התרחשה בעיה בעת ההתחברות! אנא נסה שנית", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    /**
     * Link phone credential with the email credential (to create just 1 account)
     *
     * @param credential the phone credential to link
     */
    private void linkMailPhone(PhoneAuthCredential credential) {
        FBref.auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // ask the user for additional info (name, gender)
                            askUserInfo();
                        } else {
                            Helper.removeUserCredential(getApplicationContext());
                            Toast.makeText(MainActivity.this, "התרחשה בעיה! אנא נסה שנית", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     *  After the registration finished - get info about the user (name and gender)
     */
    private void askUserInfo()
    {
        AlertDialog.Builder userInfoDialog = new AlertDialog.Builder(this);
        userInfoDialog.setTitle("מידע נוסף");
        userInfoDialog.setMessage("כדי לסיים את ההרשמה, אנא מלא את הפרטים הבאים:");

        final EditText firstName = new EditText(this);
        final EditText lastName = new EditText(this);
        final RadioGroup radioGroup = new RadioGroup(this);
        final RadioButton dadBtn = new RadioButton(this);
        final RadioButton momBtn = new RadioButton(this);

        firstName.setHint("שם פרטי");
        lastName.setHint("שם משפחה");
        dadBtn.setText("אני האבא של הילדים");
        momBtn.setText("אני האמא של הילדים");
        radioGroup.addView(dadBtn);
        radioGroup.addView(momBtn);
        radioGroup.check(dadBtn.getId());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(firstName);
        layout.addView(lastName);
        layout.addView(radioGroup);

        userInfoDialog.setView(layout);

        userInfoDialog.setPositiveButton("סיים רישום", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // if first name and last name are not empty and they are in hebrew
                if ((!TextUtils.isEmpty(firstName.getText().toString()) && !TextUtils.isEmpty(lastName.getText().toString())) && (Helper.isHebrew(firstName) && Helper.isHebrew(lastName)))
                {
                    // upload the new user to firebase database
                    User user = new User(FBref.auth.getCurrentUser().getUid(), 2, null, firstName.getText().toString(), lastName.getText().toString(), radioGroup.getCheckedRadioButtonId() == dadBtn.getId());
                    FBref.refUsers.child(FBref.auth.getCurrentUser().getUid()).setValue(user);

                    // if the user don't want to stay connected all time - remove the credential from SharedPreferences
                    if (!stayConnected.isChecked()) {
                        Helper.removeUserCredential(getApplicationContext());
                    }

                    // this function is called just when signup - so everyone here are parents (for now)
                    Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
                    startActivity(si);
                }
                // delete the user from firebase because he didn't finished the registration (there was an error with the inputs)
                else {
                    FirebaseUser user = FBref.auth.getCurrentUser();
                    user.delete();
                    Helper.removeUserCredential(getApplicationContext());
                    Toast.makeText(MainActivity.this, "אנא התחל את הרישום מחדש!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        userInfoDialog.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseUser user = FBref.auth.getCurrentUser();
                user.delete();
                Helper.removeUserCredential(getApplicationContext());
                Toast.makeText(MainActivity.this, "אנא התחל את הרישום מחדש!", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });

        userInfoDialog.show();
    }

    /**
     *  This function is called after the user got an sms code verification.
     *  If its signup - now the function send the mail verification
     *  If its login with mail - the function send the login link to the mail
     *  If its login with phone - the function signin the user with the inputed credential
     *
     * @param credential the phone credential to auth with
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // if its signup or if the user wants to signin with mail
        if ((currentState == SIGN_UP_STATE) || (logInOption.isChecked())) {
            // the settings for the email verafictaion with link
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    // URL to redirect back to.
                    .setUrl("https://betasignup.page.link/finishSignUp")
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(
                            "com.example.betaapp",
                            false,
                            null)
                    .build();

            FBref.auth.sendSignInLinkToEmail(mailET.getText().toString(), actionCodeSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "המייל נשלח", Toast.LENGTH_SHORT).show();

                                // Save the mail, the current phone credential
                                SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
                                SharedPreferences.Editor editor = signInState.edit();
                                editor.putBoolean("isMailSent", true);
                                editor.putString("mail", mailET.getText().toString());

                                // to know if to login/signup the user after back from the link
                                editor.putBoolean("isSignIn", currentState);

                                // if its signup - need to save phone credential
                                if (currentState == SIGN_UP_STATE) {
                                    final Gson gson = new Gson();
                                    String serializedObject = gson.toJson(credential);
                                    editor.putString("credential", serializedObject);
                                }
                                editor.commit();
                            } else {
                                Toast.makeText(MainActivity.this, "מייל לא נשלחץ אנא נסה שנית!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{ // we are in sign in with phone
            FBref.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // if its not new user
                                if (!task.getResult().getAdditionalUserInfo().isNewUser())
                                {
                                    // Sign in success, check if wants to stay connected
                                    if (stayConnected.isChecked())
                                        saveUserCredential(credential, true);

                                    switchRelevantActivity();
                                }
                                else
                                {
                                    // because its new user - we need to delete it! (he created new user not from the signup page)
                                    FirebaseUser user = FBref.auth.getCurrentUser();
                                    user.delete();
                                    Toast.makeText(MainActivity.this, "חשבון לא נמצא!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Sign in failed
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                    Toast.makeText(MainActivity.this, "קוד ההזדהות שהוזן שגוי! אנא נסה שנית", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Sent to the user the sms verification code
     *
     * @param view the view
     */
    public void getCode(View view) {
        // if the user entered text to phone field
        if (!phoneET.getText().toString().equals("")) {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FBref.auth)
                            .setPhoneNumber(phoneET.getText().toString())       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(callbacks)
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
        else
        {
            Toast.makeText(MainActivity.this, "אנא הכנס טלפון לשדה המתאים", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Finish register and signin\signup the user.
     *
     * @param view the view
     */
    public void finishRegister(View view) {
        // if its phone login or full signup
        if (!logInOption.isChecked() || currentState == SIGN_UP_STATE) {
            // if code sent (so we have the verification id)
            if ((storedVerificationId != null) && (!codeET.getText().toString().equals("")))
            {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(storedVerificationId, codeET.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
            else
                Toast.makeText(MainActivity.this, "בדוק שקיבלת קוד התחברות והזנת אותו בשדה המתאים", Toast.LENGTH_SHORT).show();
        }
        else // login with email
        {
            // if the user entered good text in the mail field
            if (EmailValidator.getInstance().isValid(mailET.getText().toString())) {
                signInWithPhoneAuthCredential(null);
            }
            else
            {
                Toast.makeText(MainActivity.this, "אנא הכנס מייל לשדה המתאים", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Change current view ui - swap between signup and login.
     *
     * @param view the view
     */
    public void changeCurrentState(View view) {
        // change the state of activity from sign up to log in
        currentState = !currentState;

        if (currentState == SIGN_UP_STATE){
            currentStateET.setText("TO LOG IN");
            logInOptionLayout.setVisibility(View.GONE);
            mailET.setVisibility(View.VISIBLE);
        }
        else { // else its log in state
            currentStateET.setText("TO SIGN UP");
            logInOptionLayout.setVisibility(View.VISIBLE);
            logInOption.setChecked(false); // by default be like user wants to log in with phone
            mailET.setVisibility(View.GONE);
        }
        phoneET.setVisibility(View.VISIBLE);
        getSmsCodeBtn.setVisibility(View.VISIBLE);
        codeET.setVisibility(View.VISIBLE);
    }

    /**
     * In the login view - change between login with phone and login with mail
     *
     * @param view the view
     */
    public void onSwitchOption(View view) {
        if (logInOption.isChecked()) // if its email
        {
            phoneET.setVisibility(View.GONE);
            mailET.setVisibility(View.VISIBLE);
            getSmsCodeBtn.setVisibility(View.GONE);
            codeET.setVisibility(View.GONE);
        }
        else // its phone
        {
            phoneET.setVisibility(View.VISIBLE);
            mailET.setVisibility(View.GONE);
            getSmsCodeBtn.setVisibility(View.VISIBLE);
            codeET.setVisibility(View.VISIBLE);
        }
    }

    /**
     *  This function saves the Credential if user wants to stay connected later
     *
     *  @param credential the credential to save
     *  @param isPhoneCredential if the inputted credential is a phone credential or email credential
     */
    private void saveUserCredential(AuthCredential credential, boolean isPhoneCredential)
    {
        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
        SharedPreferences.Editor editor = signInState.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(credential);
        editor.putString("credential", serializedObject);
        editor.putBoolean("isPhoneCredential", isPhoneCredential);
        editor.commit();
    }

    /**
     *  This function moves the user to the next activity (based of his role)
     */
    private void switchRelevantActivity()
    {
        FBref.refUsers.child(FBref.auth.getCurrentUser().getUid() + "/role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                Intent si;

                switch (dS.getValue(Long.class).intValue())
                {
                    case 0:
                        si = new Intent(MainActivity.this, AdminMainActivity.class);
                        si.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // make that the user could not return activities back (clear the stack)
                        startActivity(si);
                        break;
                    case 2:
                        si = new Intent(MainActivity.this, SelectChildActivity.class);
                        si.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // make that the user could not return activities back (clear the stack)
                        startActivity(si);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "התרחשה תקלה במסד הנתונים. אנא נסה שנית", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}