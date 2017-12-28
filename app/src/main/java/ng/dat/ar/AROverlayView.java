package ng.dat.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ng.dat.ar.helper.LocationHelper;
import ng.dat.ar.model.ARPoint;

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;

    public AROverlayView(final Context context) {
        super(context);

        this.context = context;

        //Demo points
            arPoints = new ArrayList<ARPoint>() {{
                add(new ARPoint(context, "Central Plaza", 13.7255, 100.7751, 0, R.drawable.wathua));
                add(new ARPoint(context, "Hua Lumphong Railway Station", 13.7274, 100.7725, 0, R.drawable.trainstation));
                add(new ARPoint(context, "Terminal 21", 13.7732, 100.8123, 0, R.drawable.terminal21));
                add(new ARPoint(context, "Wat Hua Lumphong", 13.7569, 100.7976, 0, R.drawable.wathua));
            }};
        }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(40);

        for (int i = 0; i < arPoints.size(); i ++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                //canvas.drawCircle(x, y, radius, paint);

                double distance = LocationHelper.Calc(arPoints.get(i).getLat(),arPoints.get(i).getLon(),currentLocation.getLatitude(),currentLocation.getLongitude(),1000.0);
                if (distance < 1000) {
                    canvas.drawBitmap(arPoints.get(i).getPoiBitmap(), x, y - 500, paint);
                    //canvas.drawText(arPoints.get(i).getName(), x - (-55 * arPoints.get(i).getName().length() / 2), y - 60, paint);
                }
            }
        }
    }
}
