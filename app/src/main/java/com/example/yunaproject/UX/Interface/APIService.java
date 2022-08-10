package com.example.yunaproject.UX.Interface;

import com.example.yunaproject.UX.Notifications.MyResponse;
import com.example.yunaproject.UX.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAkofGweM:APA91bGV_Pwtk_rU39Q__euYGR46a83cYordPiVR4TzmqKU49bc0ljwMwC_e_VmTaoV8SGvFi_HlufThR2VOfqLralIO2QsXutnNhE-BVYEdHoWnRG2tzxCq4EbRkTO4gRyrdsMymD4X"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
