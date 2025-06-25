package com.example.faceit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Teacher Authentication - Using Django's existing JWT endpoint
    @FormUrlEncoded
    @POST("auth/login/")  // ⬅️ FIXED: Use existing Django endpoint
    Call<TeacherAuthResponse> authenticateTeacher(
            @Field("username") String username,
            @Field("password") String password
    );

    // Session Management
    @FormUrlEncoded
    @POST("sessions/start/")
    Call<SessionResponse> startAttendanceSession(
            @Field("course_id") String courseId,
            @Field("teacher_id") String teacherId
    );

    @FormUrlEncoded
    @POST("sessions/end/")
    Call<SessionResponse> endAttendanceSession(
            @Field("session_id") String sessionId
    );

    // Context-aware course selection
    @GET("teachers/{teacher_id}/current-courses/")
    Call<CurrentCoursesResponse> getCurrentScheduledCourses(
            @Path("teacher_id") String teacherId
    );

    // Session-based attendance
    @Multipart
    @POST("attendance/checkin/")
    Call<AttendanceResponse> recordAttendance(
            @Part("session_id") RequestBody sessionId,
            @Part("status") RequestBody status,
            @Part MultipartBody.Part faceImage
    );

    // Real-time session stats
    @GET("sessions/{session_id}/stats/")
    Call<SessionStatsResponse> getSessionStats(
            @Path("session_id") String sessionId
    );

    // Legacy endpoints (keep for backward compatibility)
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