package edu.virginia.cs.cs4720.diary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.virginia.cs.cs4720.diary.myapplication.R;


public class MainActivity extends AppCompatActivity  {

    public ArrayList<DiaryEntry> entryList;
    EntryAdapter adapter;


    static final int GET_ENTRY = 1;
    static final int EDIT_ENTRY = 2;

    static final String ENTRY_LIST_KEY = "Entry list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            entryList = savedInstanceState.getParcelableArrayList(ENTRY_LIST_KEY);
        }
        else{
            entryList = new ArrayList<DiaryEntry>();
        }

        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new EntryAdapter(this,  entryList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiaryEntry entry = (DiaryEntry) parent.getItemAtPosition(position);
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelable("existing_entry", entry);
                intent.putExtras(b);
                intent.putExtra("position", position);
                intent.setClass(MainActivity.this, CreateEntry.class);
                startActivityForResult(intent, EDIT_ENTRY);
            }
        });
        if(entryList.isEmpty()) {
            String FILENAME = "Diary_Entries";
            FileInputStream fis = null;
            try {
                fis = openFileInput(FILENAME);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String buffer = "";
                int a;
                while ((a = br.read()) != -1) {
                    buffer = buffer + (char) a;
                }

                Log.d("BUFFER", buffer);

                String[] Entries = buffer.split("~");
                Log.d("LENGTH", "" + Entries.length);
                for (int i = 0; i < Entries.length; i++){
                    Log.d("ENTRY", Entries[i]);
                    if(Entries[i].isEmpty()){
                        continue;
                    }
                    String[] parts = Entries[i].split("`");
                    DiaryEntry d = new DiaryEntry(parts[1], parts[0]);
                    if ((parts[2].length()>0) && parts[2] != null){
                        d.setGeocache(parts[2]);
                    }

                    if ((parts[3].length()>0) && parts[3] != null){
                        d.setPicture(parts[3]);
                    }

                    if ((parts[4].length()>0) && parts[4] != null){
                        d.setVoice(parts[4]);
                    }

                    Log.d("ENTRY", d.getPicture());

                    entryList.add(d);

                    Log.d("ENTRYSAVE", d.toString());
                }

            } catch (FileNotFoundException e) {
                Log.d("INFO", "NO FILE");
            } catch (IOException e) {
                Log.d("INFO", e.getStackTrace().toString());
            }

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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

        if (id == R.id.action_add_entry){
            createNewEntry();
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNewEntry(){
        Intent intent = new Intent(this, CreateEntry.class);
        startActivityForResult(intent, GET_ENTRY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == GET_ENTRY){
            if (resultCode == RESULT_OK){
                DiaryEntry entry = data.getParcelableExtra("entry");
                entryList.add(entry);
                adapter.notifyDataSetChanged();

                String FILENAME = "Diary_Entries";
                String Entries = "";
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    Log.d("ERROR", "ISSUE SAVING FILE TO MEMORY");
                }
                for(DiaryEntry a: entryList){
                    Entries=Entries + a.getTitle() + "`" + a.getEntry() + "`" + a.getGeocache() + "`" + a.getPicture() + "`" + a.getVoice();
                    Entries += "~";
                    Log.d("INFO", "WRITING");
                    Log.d("ENTRY", Entries);
                }
                try {
                    fos.write(Entries.getBytes());
                } catch (IOException e) {
                    Log.d("ERROR", "ISSUE WRITING");
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.d("ERROR", "ISSUE CLOSING FILE");
                }

            }
        }

        if (requestCode == EDIT_ENTRY){
            if (resultCode == RESULT_OK){
                DiaryEntry entry = data.getParcelableExtra("entry");
                int pos = data.getIntExtra("position", 0);
                entryList.set(pos, entry);
                adapter.notifyDataSetChanged();

                String FILENAME = "Diary_Entries";
                String Entries = "";
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for(DiaryEntry a: entryList){
                    Entries=Entries + a.getTitle() + "`" + a.getEntry() + "`" + a.getGeocache() + "`" + a.getPicture() + "`" + a.getVoice();
                    Entries += "~";
                }
                try {
                    fos.write(Entries.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(ENTRY_LIST_KEY, entryList);
    }

}
