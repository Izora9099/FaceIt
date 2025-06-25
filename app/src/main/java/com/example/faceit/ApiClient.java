package com.example.faceit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit;

    // ====================================================================
    // 🌐 NETWORK CONFIGURATION - UPDATE WHEN CHANGING WIFI NETWORKS
    // ====================================================================
    //
    // CURRENT COMPUTER IP: 172.30.62.139
    //
    // ⚠️ IMPORTANT: When you change WiFi networks, you MUST:
    //
    // 1. Find your computer's new IP address:
    //    Windows: Open CMD and run "ipconfig"
    //    Mac/Linux: Open Terminal and run "ifconfig" or "ip addr"
    //
    // 2. Look for the IP address under your WiFi adapter
    //
    // 3. Update the DEFAULT_BASE_URL below with the new IP
    //    Format: "http://YOUR_NEW_IP:8000/"
    //
    // 4. Also update the same IP in MainActivity.java
    //
    // 5. Make sure Django is running with: python manage.py runserver 0.0.0.0:8000
    //
    // ====================================================================

    private static final String DEFAULT_BASE_URL = "http://172.30.62.139:8000/"; // ⬅️ UPDATE THIS IP WHEN CHANGING NETWORKS

    // Alternative URLs for different network scenarios (uncomment as needed):
    // private static final String DEFAULT_BASE_URL = "http://192.168.1.XXX:8000/";   // Common home WiFi range
    // private static final String DEFAULT_BASE_URL = "http://192.168.0.XXX:8000/";   // Common home WiFi range
    // private static final String DEFAULT_BASE_URL = "http://10.0.0.XXX:8000/";      // Corporate/office WiFi range
    // private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8000/";        // Android Emulator only
    // private static final String DEFAULT_BASE_URL = "http://192.168.43.XXX:8000/";  // Phone hotspot range

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            // Add logging interceptor for debugging network requests
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)    // Increased timeout for slow networks
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            String finalUrl = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : DEFAULT_BASE_URL;

            retrofit = new Retrofit.Builder()
                    .baseUrl(finalUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Log the configuration for debugging
            android.util.Log.d("ApiClient", "🔧 Retrofit configured with URL: " + finalUrl);
        }
        return retrofit;
    }

    // Convenience method to get client with default URL
    public static Retrofit getClient() {
        return getClient(DEFAULT_BASE_URL);
    }

    // Method to reset the client (useful when IP changes)
    public static void resetClient() {
        retrofit = null;
        android.util.Log.d("ApiClient", "🔄 ApiClient reset - will use new configuration on next call");
    }
}