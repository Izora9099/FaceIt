package com.example.faceit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Teacher Authentication - Using Django's existing JWT endpoint
    @FormUrlEncoded
    @POST("auth/login/")
    Call<TeacherAuthResponse> authenticateTeacher(
            @Field("username") String username,
            @Field("password") String password
    );

    // ✅ REAL COURSE SELECTION - Using existing Django CourseViewSet
    @GET("courses/")
    Call<CoursesListResponse> getTeacherCourses(
            @Header("Authorization") String authorization,
            @Query("active_only") boolean activeOnly
    );

    // ✅ Dashboard stats from existing Django endpoint
    @GET("dashboard/stats/")
    Call<DashboardStatsResponse> getDashboardStats(
            @Header("Authorization") String authorization
    );

    // ✅ EXISTING FACE RECOGNITION - Updated to include course_id
    @Multipart
    @POST("recognize-face/")
    Call<ApiResponse> takeAttendance(
            @Part MultipartBody.Part image,
            @Part("course_id") RequestBody courseId
    );

    // ✅ EXISTING STUDENT REGISTRATION
    @Multipart
    @POST("register-student/")
    Call<ApiResponse> registerStudent(
            @Part("name") RequestBody name,
            @Part("matric_number") RequestBody matricNumber,
            @Part MultipartBody.Part image
    );

    // ===================================================================
    // 🚧 FUTURE SESSION-BASED ENDPOINTS (when you add them to Django)
    // ===================================================================

    // Session Management (TODO: Add these to Django later)
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

    // Session-based attendance (TODO: Add to Django later)
    @Multipart
    @POST("attendance/checkin/")
    Call<AttendanceResponse> recordAttendance(
            @Part("session_id") RequestBody sessionId,
            @Part("status") RequestBody status,
            @Part MultipartBody.Part faceImage
    );

    // Real-time session stats (TODO: Add to Django later)
    @GET("sessions/{session_id}/stats/")
    Call<SessionStatsResponse> getSessionStats(
            @Path("session_id") String sessionId
    );
}