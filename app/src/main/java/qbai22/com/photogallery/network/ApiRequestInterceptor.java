package qbai22.com.photogallery.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import qbai22.com.photogallery.BuildConfig;

public class ApiRequestInterceptor implements Interceptor{
    private static final String TAG = " API KEY INTER ";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder()
                .addQueryParameter("api_key", BuildConfig.API_KEY)
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "1")
                .addQueryParameter("extras", "url_s")
                .build();
        request = request.newBuilder().url(url).build();

        return chain.proceed(request);
    }
}
