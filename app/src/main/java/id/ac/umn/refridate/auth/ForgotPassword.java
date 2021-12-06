package id.ac.umn.refridate.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import id.ac.umn.refridate.R;

public class ForgotPassword extends AppCompatActivity {
    private EditText etRecoveryEmail;
    private Button btnResetPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etRecoveryEmail = findViewById(R.id.recoveryEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        btnResetPassword.setOnClickListener(view -> resetPassword());

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    private void resetPassword() {
        String email = etRecoveryEmail.getText().toString().trim();

        if(email.isEmpty()) {
            etRecoveryEmail.setError("Email is required!");
            etRecoveryEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRecoveryEmail.setError("Please provide a valid email!");
            etRecoveryEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPassword.this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ForgotPassword.this, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}