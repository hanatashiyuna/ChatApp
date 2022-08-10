package com.example.yunaproject.UI.Home;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yunaproject.R;
import com.example.yunaproject.UX.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseUser fUser;
    DatabaseReference reference;
    boolean hasFUser;
    CircleImageView avatar, editAvatar;
    ImageView back;
    TextView username;
    EditText ipEmail, ipUsername;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;

    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data != null && data.getData() != null){
                    imageUri = data.getData();

                    if (uploadTask != null && uploadTask.isInProgress()){
                        Toast.makeText(EditProfileActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                    }else{
                        uploadImage();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        avatar = findViewById(R.id.avatar);
        editAvatar = findViewById(R.id.edit_avatar);
        username = findViewById(R.id.tv_title_username);
        ipEmail = findViewById(R.id.ip_email);
        ipUsername = findViewById(R.id.ip_username);
        back = findViewById(R.id.back);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        hasFUser = fUser != null;

        if (hasFUser){
            reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")){
                        avatar.setImageResource(R.mipmap.ic_launcher);
                    }else {
                        Glide.with(EditProfileActivity.this).load(user.getImageURL()).into(avatar);
                    }
                    ipEmail.setHint(user.getEmail());
                    ipUsername.setHint(user.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            back.setOnClickListener(view -> {
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });


            //upload image if has firebase
            storageReference = FirebaseStorage.getInstance().getReference("Uploads");

            editAvatar.setOnClickListener(view -> {
                openImage();
            });
        }
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(intent);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        customProgressBar(EditProfileActivity.this, true, "Uploading...");

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", mUri);
                    reference.updateChildren(map);

                    customProgressBar(EditProfileActivity.this, false, "");
                }else {
                    Toast.makeText(EditProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    customProgressBar(EditProfileActivity.this, false, "");
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                customProgressBar(EditProfileActivity.this, false, "");
            });
        }else{
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void customProgressBar(Context context, boolean show, CharSequence message){
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        if (show){
            dialog.show();
        }else {
            dialog.dismiss();
        }
    }
}