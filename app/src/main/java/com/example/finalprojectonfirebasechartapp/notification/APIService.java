package com.example.finalprojectonfirebasechartapp.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAeyovy7s:APA91bFM7ruM36neR0wrO8N2kGuoXNRS-gJqd_IDk_7bruggBjOx_zyuXWiOcqeDdZT1Vsx2GpEYiaHTfSN3OpaV2CPbQdS73ihyzA49Jf9p8YWMpwKhXMGMevBDt9qiaWCeB6eu-J4y"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
