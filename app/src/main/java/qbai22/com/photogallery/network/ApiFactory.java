package qbai22.com.photogallery.network;


import okhttp3.OkHttpClient;
import qbai22.com.photogallery.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFactory {

    private static OkHttpClient sClient;
    private static FlickrService sFlickrService;

    public static FlickrService getFlickrService() {
        FlickrService service = sFlickrService;
        if (service == null) {
            synchronized (ApiFactory.class) {
                service = sFlickrService;
                if (service == null) {
                    service = sFlickrService = createService();
                }
            }
        }
        return service;
    }

    public static FlickrService createService() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_ENDPOINT)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
      //          .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(FlickrService.class);
    }

    public static OkHttpClient getClient(){
        OkHttpClient client = sClient;
        if (client == null) {
            synchronized (ApiFactory.class) {
                client = sClient;
                if (client == null) {
                    client = sClient = buildClient();
                }
            }
        }
        return client;
    }

    public static OkHttpClient buildClient(){
        return new OkHttpClient.Builder()
                .addInterceptor(new FlickrInterceptor())
                .build();
    }

}
