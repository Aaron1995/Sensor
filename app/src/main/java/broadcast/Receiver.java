package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aaron on 2016/1/20 0020.
 */
public class Receiver extends BroadcastReceiver {

    private double latitude;
    private double longitude;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd日 HH:mm:ss");
    private LatLongListner latLongListner;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        } else {
            Bundle bundle = intent.getExtras();
            latitude = bundle.getDouble("纬度");
            longitude = bundle.getDouble("经度");
            String dateTime = dateFormat.format(new Date());
            latLongListner.OnReceived(latitude, longitude, dateTime);
            Log.d("msg", "经纬度：" + latitude + "  " + longitude + "  "+dateTime);
        }

    }

    public interface LatLongListner {
        void OnReceived(double latitude, double longitude,String date);

    }

    public void setOnReceivveLatLongListner(LatLongListner latLongListner) {
        this.latLongListner = latLongListner;
    }

}
