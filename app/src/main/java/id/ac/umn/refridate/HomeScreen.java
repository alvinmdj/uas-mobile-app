package id.ac.umn.refridate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import id.ac.umn.refridate.auth.LoginActivity;
import id.ac.umn.refridate.model.User;

public class HomeScreen extends AppCompatActivity {
    private ImageButton ibTopDoor, ibBottomDoor;
    private Button btnLogout;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Remove title bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ibTopDoor = findViewById(R.id.btnTopDoor);
        ibBottomDoor = findViewById(R.id.btnBottomDoor);
        btnLogout = findViewById(R.id.logout);

        ibTopDoor.setOnClickListener(view -> {
            // open activity untuk view list item pintu yang atas
            Intent testIntent = new Intent(HomeScreen.this, FreezerActivity.class);
            startActivity(testIntent);
            Toast.makeText(this, "Membuka freezer", Toast.LENGTH_SHORT).show();
        });

        ibBottomDoor.setOnClickListener(view -> {
            // open activity untuk view list item di pintu bawah
            Intent testIntent = new Intent(HomeScreen.this, PendinginBawahActivity.class);
            startActivity(testIntent);
            Toast.makeText(this, "Membuka pendingin bawah", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(view -> {
            Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeScreen.this, LoginActivity.class));
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userName = snapshot.getValue(User.class);

                if (userName != null) {
                    String name = userName.name;
                    Toast.makeText(HomeScreen.this, "Selamat datang, " + name + "!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeScreen.this, "Terjadi kesalahan. Mohon coba kembali.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
