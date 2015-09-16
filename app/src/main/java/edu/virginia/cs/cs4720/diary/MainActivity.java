package edu.virginia.cs.cs4720.diary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.virginia.cs.cs4720.diary.myapplication.R;


public class MainActivity extends AppCompatActivity implements LocationListener {

    public ArrayList<DiaryEntry> entryList = new ArrayList<DiaryEntry>();
    ArrayAdapter<DiaryEntry> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        Log.d("MAIN ACTIVITY","past registration");

/*        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new ArrayAdapter<DiaryEntry>(this, android.R.layout.simple_list_item_1, entryList);

        listView.setAdapter(adapter);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Repeat(View view){
        EditText editText = (EditText)findViewById(R.id.editText);
        TextView repeat =  (TextView) findViewById(R.id.Repeat);
        repeat.setText(editText.getText().toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView Lat = (TextView) findViewById(R.id.Latitude);
        TextView Long = (TextView) findViewById(R.id.Longitude);
        Lat.setText("Latitude: " + location.getLatitude());
        Long.setText("Longitude: " + location.getLongitude());
        Log.d("LOCATION", "" + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        TextView Lat = (TextView) findViewById(R.id.Latitude);
        TextView Long = (TextView) findViewById(R.id.Longitude);
        Lat.setText("OFF");
        Long.setText("OFF");
    }
}
