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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;

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

        SharedPreferences signInState = getSharedPreferences("Sign_In_State", MODE_PRIVATE);

        if (signInState.getBoolean("isMailSent", false)) {
            Intent intent = getIntent();
            String emailLink = intent.getData().toString();

            // put the isMailSent var back to false (so it would not read the file every time)
            SharedPreferences.Editor editor = signInState.edit();
            editor.putBoolean("isMailSent", false);
            editor.commit();

            // Confirm the link is a sign-in with email link.
            if (FBref.auth.isSignInWithEmailLink(emailLink)) {
                FBref.auth.signInWithEmailLink(signInState.getString("mail", ""), emailLink)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    linkMailPhone(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
                                } else {
                                    // לערוך את ההודעה שקופצת TODO
                                    Toast.makeText(MainActivity.this, "error in isSignInWithEmailLink", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        else{
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
                    // Save verification ID so we can use them later
                    storedVerificationId = verificationId;
                }
            };
        }
    }

    public void linkMailPhone(PhoneAuthCredential credential) {
        FBref.auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "LASTTTTTTT", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
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
                            SharedPreferences signInState = getSharedPreferences("Sign_In_State", MODE_PRIVATE);
                            SharedPreferences.Editor editor = signInState.edit();
                            editor.putBoolean("isMailSent", true);
                            editor.putString("mail", mailET.getText().toString());

                            final Gson gson = new Gson();
                            String serializedObject = gson.toJson(credential);
                            editor.putString("credential", serializedObject);
                            editor.commit();
                        } else {
                            Toast.makeText(MainActivity.this, "Email wasnt sent :( !!!!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        if (currentState == SIGN_UP_STATE) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(storedVerificationId, codeET.getText().toString());
            signInWithPhoneAuthCredential(credential);
            // todo: signInWithPhoneAuthCredential האם צריך פעמיים בקוד קריאה ל ?
        }
        else { // its login state
            if (logInOption.isChecked()) { // login with email
            }
            else { // login with phone
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