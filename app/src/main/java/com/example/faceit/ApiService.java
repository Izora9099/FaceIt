package com.example.faceit;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @Multipart
    @POST("api/register/")
    Call<ApiResponse> registerStudent(
            @Part("name") RequestBody name,
            @Part("matric_number") RequestBody matricNumber,
            @Part MultipartBody.Part image
    );


    @Multipart
    @POST("api/attendance/")
    Call<ApiResponse> takeAttendance(
            @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("api/post/")
    Call<ApiResponse> postStudentInfo(
            @Field("name") String name,
            @Field("matric_number") String matricNumber
    );

    @GET("api/notify/")
    Call<ApiResponse> getNotification(@Query("message") String message);
}
