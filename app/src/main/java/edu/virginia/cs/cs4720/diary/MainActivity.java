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
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.virginia.cs.cs4720.diary.myapplication.R;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public ArrayList<DiaryEntry> entryList;
    EntryAdapter adapter;
    enum Sort{
        NEWEST_FIRST, OLDEST_FIRST, TITLE_ASCENDING, TITLE_DESCENDING
    }
    Sort currentSort;

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

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new EntryAdapter(this,  entryList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiaryEntry entry = (DiaryEntry)parent.getItemAtPosition(position);
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
                    String[] parts = Entries[i].split(":");
                    DiaryEntry d = new DiaryEntry(parts[1], parts[0]);
                    if (parts.length > 2){
                        d.setGeocache(parts[2]);
                    }
                    entryList.add(d);
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

//        mSpinnerItem1 = menu.findItem( R.id.spinner);
//        View view1 = mSpinnerItem1.getActionView();
//        if (view1 instanceof Spinner)
//        {
//            final Spinner spinner = (Spinner) view1;
//            ad1 = ArrayAdapter.createFromResource(this.getActionBar()
//                            .getThemedContext(),
//                R.array.sort_type_array, android.R.layout.simple_spinner_item);
//            ad1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            spinner.setAdapter(ad1);
//
//
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//                @Override
//                public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                           int arg2, long arg3) {
//                    Log.d("Selected", arg0.getItemAtPosition(arg2).toString());
//
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> arg0) {
//                    // TODO Auto-generated method stub
//
//                }
//            });
//
//        }

       return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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
                    Entries=Entries + a.getTitle() + ":" + a.getEntry();
                    if (a.getGeocache() != null && a.getGeocache().length() > 0){
                        Entries += ":"+a.getGeocache();
                    }
                    Entries += "~";
                    Log.d("INFO", "WRITING");
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
                    Entries=Entries + a.getTitle() + ":" + a.getEntry();
                    if (a.getGeocache() != null && a.getGeocache().length() > 0){
                        Entries += ":"+a.getGeocache();
                    }
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d("selected", parent.getItemAtPosition(pos).toString());
        switch (parent.getItemAtPosition(pos).toString()){
            case "Oldest first":
                currentSort = Sort.OLDEST_FIRST;
                Collections.sort(entryList, new Comparator<DiaryEntry>() {
                public int compare (DiaryEntry d1, DiaryEntry d2){
                    return d1.getEntryDate().compareTo(d2.getEntryDate());
                }
                });
                adapter.notifyDataSetChanged();
                break;
            case "Newest first":
                currentSort = Sort.NEWEST_FIRST;
                Collections.sort(entryList, new Comparator<DiaryEntry>() {
                    public int compare (DiaryEntry d1, DiaryEntry d2){
                        return d2.getEntryDate().compareTo(d1.getEntryDate());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case "Alphabetical by Title (ascending)":
                currentSort = Sort.TITLE_ASCENDING;
                Collections.sort(entryList, new Comparator<DiaryEntry>() {
                    public int compare (DiaryEntry d1, DiaryEntry d2){
                        return d1.getTitle().compareTo(d2.getTitle());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case "Alphabetical by Title (descending)":
                currentSort = Sort.TITLE_DESCENDING;
                Collections.sort(entryList, new Comparator<DiaryEntry>() {
                    public int compare (DiaryEntry d1, DiaryEntry d2){
                        return d2.getTitle().compareTo(d1.getTitle());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


}
