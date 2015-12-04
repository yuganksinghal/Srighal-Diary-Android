package edu.virginia.cs.cs4720.diary;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Call;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.virginia.cs.cs4720.diary.myapplication.R;

public class CreateEntry extends AppCompatActivity implements LocationListener {

    private DiaryEntry entry;
    private int pos;
    private double lat, longi;
    static final int SELECT_PICTURE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    String AudioFile = null;
    String PictureFile = null;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        Bundle b = this.getIntent().getExtras();
        if (b != null){
            setTitle("Edit Entry");
            entry = b.getParcelable("existing_entry");
            ((EditText) (findViewById(R.id.entryText))).setText(entry.getEntry());
            ((EditText) (findViewById(R.id.titleText))).setText(entry.getTitle());

            PictureFile = entry.getPicture();
            AudioFile = entry.getVoice();

            if (PictureFile != null) {
                File imgFile = new File(PictureFile);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, (int) (myBitmap.getWidth() * 0.3), (int) (myBitmap.getHeight() * 0.3), true);
                    ImageView myImage = (ImageView) findViewById(R.id.imageView);
                    myImage.setImageBitmap(scaled);
                }
            }

            if(AudioFile != null && !AudioFile.equals("null")){
                Log.d("audio file", AudioFile);
                ((Button)(findViewById(R.id.PlayButton))).setVisibility(View.VISIBLE);
            }

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


        return super.onOptionsItemSelected(item);
    }

    public void onSave(View v) throws IOException{
        String entryText = ((EditText) (findViewById(R.id.entryText))).getText().toString();
        String titleText = ((EditText) (findViewById(R.id.titleText))).getText().toString();
        if (entryText.length() == 0  || titleText.length() == 0 ){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Title and entry required");
            alertDialog.setMessage("You must fill in both title and entry if you'd like to save.");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            //alertDialog.setIcon(R.drawable.icon);
            alertDialog.show();
            return;
        }
        Intent returnIntent = new Intent();
        boolean editing = false;
        if (entry != null){
            entry.setEntry(entryText);
            entry.setTitle(titleText);
            editing = true;
            returnIntent.putExtra("position", pos);
        }
        else {
            entry = new DiaryEntry(entryText, titleText);
        }
        entry.setEntryDate(new Date());
        entry.setGeocache("" + lat + "," + longi);
        entry.setPicture(PictureFile);
        entry.setVoice(AudioFile);

        Gson gson = new Gson();

        String json = gson.toJson(entry);
        String url;
        if (editing){
            url = "http://srighal-diary.herokuapp.com/entry/" + entry.getId()+"/save";
        }
        else{
            url = "http://srighal-diary.herokuapp.com/entry/new";
        }
        post(url , json, new Callback(){
            @Override
            public void onFailure(Request request, IOException i) {
                Log.d("post failed", i.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("response string", responseStr);
                } else {
                    Log.d("response failed", "on response");
                }
            }

        });

        returnIntent.putExtra("entry", entry);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    Call post(String url, String json, Callback callback) throws IOException {
        final OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView loc = (TextView) findViewById(R.id.location);
        lat = location.getLatitude();
        longi = location.getLongitude();
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> address = geoCoder.getFromLocation(lat, longi, 1);
            String locality = address.get(0).getLocality();
            loc.setText("Location: "+locality);
        } catch (IOException e){}
        catch (NullPointerException e){}
        catch (IndexOutOfBoundsException e) {
        }
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
        TextView loc = (TextView) findViewById(R.id.location);
        loc.setText("Location is off");
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex){}
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("RESULT", "" + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new  File(PictureFile);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Bitmap scaled = Bitmap.createScaledBitmap(myBitmap,(int)(myBitmap.getWidth()*0.3), (int)(myBitmap.getHeight()*0.3), true);
                ImageView myImage = (ImageView) findViewById(R.id.imageView);
                myImage.setImageBitmap(scaled);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        PictureFile = image.getAbsolutePath();
        return image;
    }

    public void onRecord(View v){
        AudioFile = getExternalFilesDir(null) + "/" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".3gp";
        //AudioFile = getFilesDir() + "PREETAMSUX.3gp";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile( AudioFile);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e("MICROPHONE", e.getMessage());
        }

        Button Record = (Button) findViewById(R.id.RecordButton);
        Button Play = (Button) findViewById(R.id.PlayButton);

        Record.setText("Stop Recording");
        Record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording(v);
            }
        });

        Play.setVisibility(View.GONE);
    }

    public void stopRecording(View v) {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        Button Record = (Button) findViewById(R.id.RecordButton);
        Button Play = (Button) findViewById(R.id.PlayButton);

        Record.setText("Record");
        Record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRecord(v);
            }
        });

        Play.setVisibility(View.VISIBLE);
    }

    public void onPlay(View v) {
        mPlayer = new MediaPlayer();
        final Button Record = (Button) findViewById(R.id.RecordButton);
        final Button Play = (Button) findViewById(R.id.PlayButton);

        try {
            mPlayer.setDataSource(AudioFile);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Play.setText("Play");
                    Play.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            onPlay(v);
                        }
                    });

                    Record.setVisibility(View.VISIBLE);
                }
            });
            Play.setText("Stop Playing");
            Play.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stopPlaying(v);
                }
            });
            Record.setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            Log.e( "AUDIO", "prepare() failed");
        }
    }


    public void stopPlaying(View v) {
        mPlayer.release();
        mPlayer = null;

        Button Record = (Button) findViewById(R.id.RecordButton);
        Button Play = (Button) findViewById(R.id.PlayButton);

        Play.setText("Play");
        Play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlay(v);
            }
        });

        Record.setVisibility(View.VISIBLE);
    }

}
