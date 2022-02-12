package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
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

    final Gson gson = new Gson();
    final boolean SIGN_UP_STATE = false;

    private String storedVerificationId;
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

        SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);

        if (signInState.getBoolean("isMailSent", false)) {
            Intent intent = getIntent();

            if (intent.getData() != null) {
                String emailLink = intent.getData().toString();

                // Confirm the link is a sign-in with email link.
                if (FBref.auth.isSignInWithEmailLink(emailLink)) {
                    FBref.auth.signInWithEmailLink(signInState.getString("mail", ""), emailLink)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if(!signInState.getBoolean("isSignIn", SIGN_UP_STATE)) {
                                            linkMailPhone(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
                                        }
                                        else {
                                            FirebaseUser u = FBref.auth.getCurrentUser();
                                            Toast.makeText(MainActivity.this, "SIGN IN WITH MAIL - SUCCESS!!!", Toast.LENGTH_SHORT).show();
                                            FBref.auth.signOut();
                                        }
                                    } else {
                                        // לערוך את ההודעה שקופצת TODO
                                        Toast.makeText(MainActivity.this, "error in isSignInWithEmailLink", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        }
        // if the user saved the login credential - sign in the user
        else if (!signInState.getString("credential", "").equals(""))
        {
            FBref.auth.signInWithCredential(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
            Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
            startActivity(si);
        }

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            // todo: האם להשאיר את הפונקציה הזאת בלי כלום בתוכה?
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID so we can use it later
                storedVerificationId = verificationId;
            }
        };
    }

    public void linkMailPhone(PhoneAuthCredential credential) {
        FBref.auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "LASTTTTTTT", Toast.LENGTH_SHORT).show();

                            // upload the new user to firebase database
                            User user = new User(FBref.auth.getCurrentUser().getUid(), 2, null);
                            FBref.refUsers.child(FBref.auth.getCurrentUser().getUid()).setValue(user);

                            Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
                            startActivity(si);
                        } else {
                            Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // if its signup or signin with mail
        if ((currentState == SIGN_UP_STATE) || (logInOption.isChecked())) {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    // URL I want to redirect back to.
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
                                Toast.makeText(MainActivity.this, "Email sent", Toast.LENGTH_SHORT).show();

                                // Save the mail, the current phone credential
                                SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
                                SharedPreferences.Editor editor = signInState.edit();
                                editor.putBoolean("isMailSent", true);
                                editor.putString("mail", mailET.getText().toString());
                                editor.putBoolean("isSignIn", currentState);

                                // if its signup - need to save phone credential
                                if (currentState == SIGN_UP_STATE) {
                                    final Gson gson = new Gson();
                                    String serializedObject = gson.toJson(credential);
                                    editor.putString("credential", serializedObject);
                                }
                                editor.commit();
                            } else {
                                Toast.makeText(MainActivity.this, "Email wasnt sent :( !!!!!!", Toast.LENGTH_SHORT).show();
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
                                // Sign in success
                                FirebaseUser user = task.getResult().getUser();
                                Toast.makeText(MainActivity.this, "SIGN IN WITH PHONE - SUCCESS!!!", Toast.LENGTH_SHORT).show();

                                SharedPreferences signInState = getSharedPreferences("States", MODE_PRIVATE);
                                SharedPreferences.Editor editor = signInState.edit();
                                final Gson gson = new Gson();
                                String serializedObject = gson.toJson(credential);
                                editor.putString("credential", serializedObject);

                                // if tried to log in with mail and then login with phone
                                editor.putBoolean("isMailSent", false);
                                editor.commit();

                                Intent si = new Intent(MainActivity.this, SelectChildActivity.class);
                                startActivity(si);

                                //todo: FBref.auth.signOut();
                            } else {
                                // Sign in failed
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                    Toast.makeText(MainActivity.this, "Bad varification code. try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public void getCode(View view) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FBref.auth)
                        .setPhoneNumber(phoneET.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void finishRegister(View view) {
        PhoneAuthCredential credential = null;

        // phone login or full signup
        if (!logInOption.isChecked() || currentState == SIGN_UP_STATE)
        {
            credential = PhoneAuthProvider.getCredential(storedVerificationId, codeET.getText().toString());
        }
        signInWithPhoneAuthCredential(credential);
    }

    public void changeCurrentState(View view) {
        // change the state of activity from sign up to log in
        currentState = !currentState;

        if (currentState == SIGN_UP_STATE){
            currentStateET.setText("TO LOG IN");
            logInOptionLayout.setVisibility(View.GONE);
            mailET.setVisibility(View.VISIBLE);
            phoneET.setVisibility(View.VISIBLE);
            getSmsCodeBtn.setVisibility(View.VISIBLE);
            codeET.setVisibility(View.VISIBLE);
        }
        else { // else its log in state
            currentStateET.setText("TO SIGN UP");
            logInOptionLayout.setVisibility(View.VISIBLE);
            logInOption.setChecked(false); // by default be like user wants to log in with phone
            mailET.setVisibility(View.GONE);
            phoneET.setVisibility(View.VISIBLE);
            getSmsCodeBtn.setVisibility(View.VISIBLE);
            codeET.setVisibility(View.VISIBLE);
        }
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
}