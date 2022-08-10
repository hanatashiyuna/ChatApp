package com.example.yunaproject.UI.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yunaproject.R;
import com.example.yunaproject.UX.Adapter.UserAdapter;
import com.example.yunaproject.UX.Model.Chat;
import com.example.yunaproject.UX.Model.User;
import com.example.yunaproject.UX.Notifications.Token;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    View view;
    RecyclerView recChats;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fUser;
    DatabaseReference reference;
    List<String> usersList;
    boolean hasGg, hasFirebase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats, container, false);

        recChats = view.findViewById(R.id.rec_chats);
        recChats.setHasFixedSize(true);
        recChats.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        hasFirebase = fUser != null;

        //google api
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        hasGg = account != null && !account.isExpired();

        if(hasGg){
            //TODO
        }else if(hasFirebase){
            usersList = new ArrayList<>();

            reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    usersList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chat chat = snapshot.getValue(Chat.class);

                        assert chat != null;
                        if (chat.getSender().equals(fUser.getUid())){
                            usersList.add(chat.getReceiver());
                        }
                        if (chat.getReceiver().equals(fUser.getUid())){
                            usersList.add(chat.getSender());
                        }
                    }

                    readChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String tokenInstance = task.getResult();
                updateToken(tokenInstance);
            }
        });

        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fUser.getUid()).setValue(token1);
    }

    private void readChats() {
        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for (String id : usersList){
                        assert user != null;
                        if(user.getId().equals(id)){
                            if(mUsers.size() != 0){
                                for (User userl : mUsers){
                                    if (!user.getId().equals(userl.getId())){
                                        //mUsers.add(userl);
                                        //Toast.makeText(requireActivity(), userl.getUsername().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else {
                                mUsers.add(user);
                            }
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recChats.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}