package ng.dat.ar.model.http;

import ng.dat.ar.dao.PlaceCollectionDao;
import ng.dat.ar.dao.PlaceItemDao;
import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by phatipan on 26/12/2017 AD.
 */

public interface ApiPlaceService {

    @POST("json_encode_place.php")
    Call<PlaceCollectionDao> loadData();
}
