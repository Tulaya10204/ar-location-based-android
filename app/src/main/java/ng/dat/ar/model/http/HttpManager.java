package ng.dat.ar.model.http;

import android.content.Context;

import ng.dat.ar.model.Contextor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by phatipan on 26/12/2017 AD.
 */

public class HttpManager {
    private static HttpManager instance;

    public static HttpManager getInstance() {
        if (instance == null){
            instance = new HttpManager();
        }
        return instance;
    }

    private Context mContext;
    private ApiPlaceService service;

    private HttpManager() {
        mContext = Contextor.getInstance().getContext();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://161.246.58.241/")
                .addConverterFactory(GsonConverterFactory.create()) //convert String from Retrofit to json obj
                .build();

        service = retrofit.create(ApiPlaceService.class);
    }

    public ApiPlaceService getService(){
        return service;
    }
}
