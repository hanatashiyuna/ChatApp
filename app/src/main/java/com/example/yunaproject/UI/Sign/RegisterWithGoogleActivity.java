package com.example.yunaproject.UI.Sign;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yunaproject.R;
import com.example.yunaproject.UI.Home.MainActivity;
import com.example.yunaproject.UX.Adapter.UserAdapter;
import com.example.yunaproject.UX.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterWithGoogleActivity extends AppCompatActivity {

    DatabaseReference reference;
    FirebaseAuth auth;
    boolean hasGg, hasAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_with_google);

        EditText pass = findViewById(R.id.password);
        Button btn_sign_in = findViewById(R.id.button);

        auth = FirebaseAuth.getInstance();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(RegisterWithGoogleActivity.this);
        hasGg = account != null && !account.isExpired();

        if (hasGg){
            String email = account.getEmail();
            String usn = account.getDisplayName();

            reference = FirebaseDatabase.getInstance().getReference("Users");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        hasAccount = user.getEmail().equals(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RegisterWithGoogleActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            if (hasAccount){
                btn_sign_in.setOnClickListener(view -> {
                    String password = pass.getText().toString();
                    SignUp(usn, email, password);
                });
            }else{
                btn_sign_in.setOnClickListener(view -> {
                    customProgressBar(RegisterWithGoogleActivity.this, true);
                    String password = pass.getText().toString();
                    if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                        Toast.makeText(RegisterWithGoogleActivity.this, "All fields are required" + password.toString(), Toast.LENGTH_SHORT).show();
                        customProgressBar(RegisterWithGoogleActivity.this, false);
                    }else {
                        assert email != null;
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        customProgressBar(RegisterWithGoogleActivity.this, false);
                                        Intent intent = new Intent(RegisterWithGoogleActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        customProgressBar(RegisterWithGoogleActivity.this, false);
                                        Toast.makeText(RegisterWithGoogleActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }
    }

    public void customProgressBar(Context context, boolean show){
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Wait a minute...");
        if (show){
            dialog.show();
        }else {
            dialog.dismiss();
        }
    }

    private void SignUp(String usn, String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("email", email);
                        hashMap.put("id", userid);
                        hashMap.put("username", usn);
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("search", usn.toLowerCase());

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Intent intent = new Intent(RegisterWithGoogleActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }else {
                        Toast.makeText(RegisterWithGoogleActivity.this, "you can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}