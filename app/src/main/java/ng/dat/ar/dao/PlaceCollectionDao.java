package ng.dat.ar.dao;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by phatipan on 26/12/2017 AD.
 */

public class PlaceCollectionDao {

    @SerializedName("item") private List<PlaceItemDao> itemDaos;

    public List<PlaceItemDao> getItemDaos() {
        return itemDaos;
    }

    public void setItemDaos(List<PlaceItemDao> itemDaos) {
        this.itemDaos = itemDaos;
    }
}
