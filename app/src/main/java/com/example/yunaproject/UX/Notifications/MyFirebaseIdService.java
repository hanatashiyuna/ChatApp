package com.example.yunaproject.UX.Notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseMessaging.getInstance().getToken().toString();
        if (fUser != null){
            updateToken(refreshToken);
        }
    }

    public void updateToken(String s){
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(s);
        assert fUser != null;
        reference.child(fUser.getUid()).setValue(token);
    }

}
