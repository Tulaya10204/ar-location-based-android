package ng.dat.ar.dao;

import com.google.gson.annotations.SerializedName;

/**
 * Created by phatipan on 26/12/2017 AD.
 */

public class PlaceItemDao {

    @SerializedName("place_id")                private int placeId;
    @SerializedName("place_name")              private String placeName;
    @SerializedName("place_lat")               private Double placeLat;
    @SerializedName("place_lon")               private Double placeLon;
    @SerializedName("place_place_exitNumber")  private String placeExitNumber;
    @SerializedName("place_pic")               private String placePic;
    @SerializedName("station_id")                private int stationId;

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Double getPlaceLat() {
        return placeLat;
    }

    public void setPlaceLat(Double placeLat) {
        this.placeLat = placeLat;
    }

    public Double getPlaceLon() {
        return placeLon;
    }

    public void setPlaceLon(Double placeLon) {
        this.placeLon = placeLon;
    }

    public String getPlaceExitNumber() {
        return placeExitNumber;
    }

    public void setPlaceExitNumber(String placeExitNumber) {
        this.placeExitNumber = placeExitNumber;
    }

    public String getPlacePic() {
        return placePic;
    }

    public void setPlacePic(String placePic) {
        this.placePic = placePic;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}
