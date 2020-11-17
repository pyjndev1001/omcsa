package logo.omcsa_v9.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import logo.omcsa_v9.model.CategoryInfoResponse;
import logo.omcsa_v9.model.GeneralResponse;
import logo.omcsa_v9.model.LoginResponse;
import logo.omcsa_v9.model.ModifiedDateResponse;
import logo.omcsa_v9.model.OrderHistoryResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;


/**
 * Created by silve on 10/30/2016.
 */
public class ApiClient {

    public static String API_MAIN_ROOT = "https://omcsa.org/imagescript/index.php/Backend/";

    private static ApiInterface apiMainService;

    public static ApiInterface getMainApiClient() {
        if (apiMainService == null) {

            OkHttpClient client = getUnsafeOkHttpClient();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_MAIN_ROOT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            apiMainService = retrofit.create(ApiInterface.class);
        }

        return apiMainService;
    }

    public static OkHttpClient getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(300, TimeUnit.SECONDS);
            builder.connectTimeout(300, TimeUnit.SECONDS);
            builder.writeTimeout(300, TimeUnit.SECONDS);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public interface ApiInterface {

        //------------User Related Api-------------------
        @FormUrlEncoded
        @POST("login")
        public Call<LoginResponse> login(@FieldMap Map<String, String> options);

        @FormUrlEncoded
        @POST("forget_password")
        public Call<GeneralResponse> forgetPassword(@FieldMap Map<String, String> options);

        @FormUrlEncoded
        @POST("update_userinfo")
        public Call<GeneralResponse> updateUserInfo(@FieldMap Map<String, String> options);

        @GET("get_order_history")
        public Call<OrderHistoryResponse> getOrderHistory(@QueryMap Map<String, String> options);

    }
}
