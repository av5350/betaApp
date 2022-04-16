package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText mailET, phoneET, codeET;
    LinearLayout logInOptionLayout;
    Switch logInOption;
    TextView currentStateET;
    Button getSmsCodeBtn;
    CheckBox stayConnected;

    final Gson gson = new Gson();
    final boolean SIGN_UP_STATE = false;

    private String storedVerificationId = null;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    boolean currentState = SIGN_UP_STATE;

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

        // Retrieve checkBox data
        stayConnected.setChecked(signInState.getBoolean("wantStayConnected", false));

        // if mail was sent - get the intent (the link redirects to the app)
        if (signInState.getBoolean("isMailSent", false)) {
            Intent intent = getIntent();

            if (intent.getData() != null) {
                String emailLink = intent.getData().toString();

                // Confirm the link is a sign-in or sign-up email link(its the same function).
                if (FBref.auth.isSignInWithEmailLink(emailLink)) {
                    FBref.auth.signInWithEmailLink(signInState.getString("mail", ""), emailLink)
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
                                            // if user want to stay connected - save the email Credential
                                            if (stayConnected.isChecked())
                                                saveUserCredential(EmailAuthProvider.getCredentialWithLink(signInState.getString("mail", ""), emailLink), false);

                                            // move to the relevant activity (based on the user's role)
                                            switchRelevantActivity();
                                        }
                                    } else { // there was an error while trying to signup/login
                                        Toast.makeText(MainActivity.this, "התרחשה בעיה בעת ההתחברות! אנא נסה שנית", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
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

            // todo: האם להשאיר את הפונקציה הזאת בלי כלום בתוכה?
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

        // if the user get into the app from email link for example, save the checkBox state
        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
        SharedPreferences.Editor editor = signInState.edit();
        editor.putBoolean("wantStayConnected", stayConnected.isChecked());
        editor.commit();
    }

    public void linkMailPhone(PhoneAuthCredential credential) {
        FBref.auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // upload the new user to firebase database
                            User user = new User(FBref.auth.getCurrentUser().getUid(), 2, null);
                            FBref.refUsers.child(FBref.auth.getCurrentUser().getUid()).setValue(user);

                            // if the user dont want to stay connected all time - remove the credential from SharedPreferences
                            if (!stayConnected.isChecked()) {
                                removeUserCredential();
                            }

                            // this function is called just when signup - so everyone here are parents (for now)
                            Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
                            startActivity(si);
                        } else {
                            Toast.makeText(MainActivity.this, "התרחשה בעיה! אנא נסה שנית", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // if its signup or if the user wants to signin with mail
        if ((currentState == SIGN_UP_STATE) || (logInOption.isChecked())) {
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
                                // Sign in success, check if wants to stay connected
                                if (stayConnected.isChecked())
                                    saveUserCredential(credential, true);
                                switchRelevantActivity();
                            } else {
                                // Sign in failed
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                    Toast.makeText(MainActivity.this, "קוד הזדהות שגוי. אנא נסה שנית!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

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

    public void finishRegister(View view) {
        PhoneAuthCredential credential = null;

        // phone login or full signup
        if (!logInOption.isChecked() || currentState == SIGN_UP_STATE) {
            // if code sent (so we have the verification id)
            if ((storedVerificationId != null) && (!codeET.getText().toString().equals("")))
            {
                credential = PhoneAuthProvider.getCredential(storedVerificationId, codeET.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
            else
                Toast.makeText(MainActivity.this, "בדוק שקיבלת קוד התחברות והזנת אותו בשדה המתאים", Toast.LENGTH_SHORT).show();
        }
        else // login with email
        {
            // if the user entered text in the mail field
            if (!mailET.getText().toString().equals("")) {
                signInWithPhoneAuthCredential(credential);
            }
            else
            {
                Toast.makeText(MainActivity.this, "אנא הכנס מייל לשדה המתאים", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    // if user wants to stay connected - save the Credential
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

    // if the user dont wants to stay connected, and he signin/signup using mail - remove the Credential
    private void removeUserCredential()
    {
        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
        SharedPreferences.Editor editor = signInState.edit();
        editor.putString("credential", "");
        editor.commit();
    }

    private void switchRelevantActivity()
    {
        FBref.refUsers.child(FBref.auth.getCurrentUser().getUid() + "/role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                if (dS.getValue(Long.class) == 2)
                {
                    Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
                    startActivity(si);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "IDK :(", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}