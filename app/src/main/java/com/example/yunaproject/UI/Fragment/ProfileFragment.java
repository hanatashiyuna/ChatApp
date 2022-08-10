package com.example.yunaproject.UI.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yunaproject.R;
import com.example.yunaproject.UI.Home.EditProfileActivity;
import com.example.yunaproject.UI.Home.MessengerActivity;
import com.example.yunaproject.UI.Sign.LoginActivity;
import com.example.yunaproject.UI.Sign.RegisterWithGoogleActivity;
import com.example.yunaproject.UX.Adapter.MessageAdapter;
import com.example.yunaproject.UX.Model.Chat;
import com.example.yunaproject.UX.Model.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    View view;
    List<Chat> mChat;
    TextView totalMessage;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    AccessToken accessToken;
    int messageCount = 0;
    boolean hasGg, hasFirebase, hasFb, isAvatarFb = false;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        CircleImageView avatar = view.findViewById(R.id.avatar);
        TextView name = view.findViewById(R.id.name);
        TextView email = view.findViewById(R.id.email);
        TextView btnSignOut = view.findViewById(R.id.btn_log_out);
        totalMessage = view.findViewById(R.id.tv_total_message);
        ImageView editProfile = view.findViewById(R.id.iv_edit);

        //firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hasFirebase = firebaseUser != null;
        //facebook token
        accessToken = AccessToken.getCurrentAccessToken();
        hasFb = accessToken != null && !accessToken.isExpired();
        //google api
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        hasGg = account != null && !account.isExpired();

        if(hasFirebase){
            //display profile
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    name.setText(user.getUsername());
                    email.setText(user.getEmail());
                    if (user.getImageURL().equals("default")){
                        avatar.setImageResource(R.drawable.ic_ganyu);
                    }else{
                        Glide.with(requireActivity()).load(user.getImageURL()).into(avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            btnSignOut.setOnClickListener(view ->{
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(requireActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            });

            editProfile.setOnClickListener(view1 -> {
                Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                startActivity(intent);
            });

            //properties display
            totalMessages();
        }else if(hasFb){
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    (object, response) -> {
                        try {
                            assert object != null;
                            String url = object.getJSONObject("picture").getJSONObject("data").getString("url");
                            String fullName = object.getString("name");
                            name.setText(fullName);
                            Picasso.get().load(url).into(avatar);
                            isAvatarFb = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();

            btnSignOut.setOnClickListener(view -> {
                LoginManager.getInstance().logOut();
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            });
        }else if(hasGg){
            //gg
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            gsc = GoogleSignIn.getClient(requireActivity(), gso);

            //display profile
            if (hasFirebase){
                reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        name.setText(user.getUsername());
                        email.setText(user.getEmail());
                        if (user.getImageURL().equals("default")){
                            avatar.setImageResource(R.drawable.ic_ganyu);
                        }else{
                            Glide.with(requireActivity()).load(user.getImageURL()).into(avatar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            editProfile.setOnClickListener(view1 -> {
                Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                startActivity(intent);
            });
            //properties display
            totalMessages();

            btnSignOut.setOnClickListener(view -> {
                FirebaseAuth.getInstance().signOut();
                gsc.signOut().addOnCompleteListener(task -> startActivity(new Intent(getContext(), LoginActivity.class)));
            });
        }
        return view;
    }

    private void totalMessages() {
        try {
//            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
//
//            reference = FirebaseDatabase.getInstance().getReference("Chats");
//            reference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                        Chat chat = dataSnapshot.getValue(Chat.class);
//                        assert chat != null;
//                        assert fUser != null;
//                        if(chat.getSender().equals(fUser.getUid())){
//                            messageCount += 1;
//                        }
//                    }
//                    totalMessage.setText(String.valueOf(messageCount));
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
        }catch (Exception e){
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}