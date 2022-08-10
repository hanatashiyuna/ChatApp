package com.example.yunaproject.UI.Sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yunaproject.R;
import com.example.yunaproject.UI.Home.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText usn, pass, email;
    Button signUp;
    FirebaseAuth auth;
    DatabaseReference reference;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView btnGG;

    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent intent = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                try {
                    task.getResult(ApiException.class);
                    navigateToSecondActivity();
                } catch (ApiException e) {
                    Toast.makeText(SignUpActivity.this, "Something went wrong?", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usn = findViewById(R.id.usn);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        signUp = findViewById(R.id.button);
        btnGG = findViewById(R.id.btn_gg_sign_in);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean hasGg = account != null && !account.isExpired();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        btnGG.setOnClickListener(view -> ggSignIn());

        if(hasGg){
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        auth = FirebaseAuth.getInstance();
        signUp.setOnClickListener(view -> {
            String getUserName = usn.getText().toString();
            String getEmail = email.getText().toString();
            String getPass = pass.getText().toString();
            if (TextUtils.isEmpty(getUserName) || TextUtils.isEmpty(getUserName) || TextUtils.isEmpty(getUserName)){
                Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            }else if (getPass.length() < 6){
                Toast.makeText(SignUpActivity.this, "Password must be least 6 characters", Toast.LENGTH_SHORT).show();
            }else{
                SignUp(getUserName, getEmail, getPass);
            }
        });
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
                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }else {
                        Toast.makeText(SignUpActivity.this, "you can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void ggSignIn(){
        Intent intent = gsc.getSignInIntent();
        mActivityResultLauncher.launch(intent);
    }

    public void navigateToSecondActivity(){
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
    }
}