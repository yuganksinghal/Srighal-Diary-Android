package edu.virginia.cs.cs4720.diary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.virginia.cs.cs4720.diary.myapplication.R;

public class CreateEntry extends AppCompatActivity implements LocationListener {

    private DiaryEntry entry;
    private int pos;
    private double lat, longi;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        Bundle b = this.getIntent().getExtras();
        if (b != null){
            entry = b.getParcelable("existing_entry");
            ((EditText) (findViewById(R.id.entryText))).setText(entry.getEntry());
            ((EditText) (findViewById(R.id.titleText))).setText(entry.getTitle());
            pos = this.getIntent().getIntExtra("position", 0);
        }
        else{
            entry = null;
            pos = -1;
        }

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_entry, menu);
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

    public void onSave(View v){
        String entryText = ((EditText) (findViewById(R.id.entryText))).getText().toString();
        String titleText = ((EditText) (findViewById(R.id.titleText))).getText().toString();
        Intent returnIntent = new Intent();
        if (entry != null){
            entry.setEntry(entryText);
            entry.setTitle(titleText);
            returnIntent.putExtra("position", pos);
        }
        else {
            entry = new DiaryEntry(entryText, titleText);
        }
        //if (lat!=null && longi !=null) {
            entry.setGeocache("" + lat + "," + longi);
        //}

        returnIntent.putExtra("entry", entry);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        //TextView lat = (TextView) findViewById(R.id.Latitude);
        //TextView longi = (TextView) findViewById(R.id.Longitude);
        //lat.setText("Latitude: " + location.getLatitude());
        //longi.setText("Longitude: " + location.getLongitude());
        TextView loc = (TextView) findViewById(R.id.location);
        lat = location.getLatitude();
        longi = location.getLongitude();
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> address = geoCoder.getFromLocation(lat, longi, 1);
            String locality = address.get(0).getLocality();
            loc.setText("Last saved at: "+locality);
        } catch (IOException e){}
        catch (NullPointerException e){}
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
        //TextView Lat = (TextView) findViewById(R.id.Latitude);
        //TextView Long = (TextView) findViewById(R.id.Longitude);
        //Lat.setText("OFF");
        //Long.setText("OFF");
        TextView loc = (TextView) findViewById(R.id.location);
        loc.setText("Location is off");
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView) findViewById(R.id.imageView);
            mImageView.setImageBitmap(imageBitmap);
        }
    }
}
