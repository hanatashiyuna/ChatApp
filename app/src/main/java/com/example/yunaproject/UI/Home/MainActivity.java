package com.example.yunaproject.UI.Home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.yunaproject.R;
import com.example.yunaproject.UI.Sign.LoginActivity;
import com.example.yunaproject.UX.Adapter.ViewPager2Adapter;
import com.example.yunaproject.UX.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    boolean hasGg, hasFirebase;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout mTablaLayout = findViewById(R.id.tab_layout);
        ViewPager2 mViewPager2 = findViewById(R.id.view_pager2);

        //tab layout
        ViewPager2Adapter mPager2Adapter = new ViewPager2Adapter(this);
        mViewPager2.setAdapter(mPager2Adapter);

        new TabLayoutMediator(mTablaLayout, mViewPager2, (tab, position) -> {
           switch (position){
               case 0:
                   tab.setText("Chats");
                   break;
               case 1:
                   tab.setText("Friends");
                   break;
               case 2:
                   tab.setText("My Profile");
                   break;
           }
        }).attach();

        //firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hasFirebase = firebaseUser != null;

        //google api
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        hasGg = account != null && !account.isExpired();

        if(hasGg){
            //TODO
        }else if(hasFirebase){
            //TODO
        }
    }

    private void status(String status){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hasFirebase = firebaseUser != null;
        if (hasFirebase){
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);

            reference.updateChildren(hashMap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}