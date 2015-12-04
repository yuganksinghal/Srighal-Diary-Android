package edu.virginia.cs.cs4720.diary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Callback;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

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

    static final String FIELD_DELIM = "\u001f";
    static final String ENTRY_DELIM = "\u001e";

    private SharedPreferences mPrefs;

    Spinner spinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            entryList = savedInstanceState.getParcelableArrayList(ENTRY_LIST_KEY);
            currentSort = Sort.values()[savedInstanceState.getInt("s")];
        }
        else{
            entryList = new ArrayList<DiaryEntry>();
        }

        mPrefs = getPreferences(MODE_PRIVATE);
        String storedVal = mPrefs.getString("sort", "OLDEST_FIRST");
        switch (storedVal){
            case "OLDEST_FIRST":
                currentSort = Sort.OLDEST_FIRST;
                break;
            case "NEWEST_FIRST":
                currentSort = Sort.NEWEST_FIRST;
                break;
            case "TITLE_ASCENDING":
                currentSort = Sort.TITLE_ASCENDING;
                break;
            case "TITLE_DESCENDING":
                currentSort = Sort.TITLE_DESCENDING;
                break;
        }

        setContentView(R.layout.activity_main);



        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new EntryAdapter(this,  entryList);
        TextView emptyView = (TextView) findViewById(R.id.empty);
        listView.setEmptyView(emptyView);

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

        listView.setLongClickable(true);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final DiaryEntry entry = (DiaryEntry) parent.getItemAtPosition(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Entry")
                        .setMessage("Do you want to delete the entry titled " + "\"" + entry.getTitle() + "\"?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){

                                //delete post request here.
                                final OkHttpClient client = new OkHttpClient();
                                RequestBody emptyBody = RequestBody.create(null, new byte[0]);
                                Request request = new Request.Builder()
                                        .url("http://srighal-diary.herokuapp.com/entry/"+entry.getId()+"/delete")
                                        .post(emptyBody)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(Request request, IOException throwable) {
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                                    }
                                });

                                entryList.remove(entry);
                                adapter.notifyDataSetChanged();
                                writeToFile();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
                return true;
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

                String[] Entries = buffer.split(ENTRY_DELIM);
                Log.d("LENGTH", "" + Entries.length);
                for (int i = 0; i < Entries.length; i++){
                    Log.d("ENTRY", Entries[i]);
                    if(Entries[i].isEmpty()){
                        continue;
                    }
                    String[] parts = Entries[i].split(FIELD_DELIM);
                    DiaryEntry d = new DiaryEntry(parts[1], parts[0], parts[6]);
                    if ((parts[2].length()>0) && parts[2] != null){
                        d.setGeocache(parts[2]);
                    }

                    if ((parts[3].length()>0) && parts[3] != null){
                        d.setPicture(parts[3]);
                    }

                    if ((parts[4].length()>0) && parts[4] != null){
                        d.setVoice(parts[4]);
                    }

                    if ((parts[5].length()>0) && parts[5] != null){
                        long t = Long.parseLong(parts[5]);
                        d.setEntryDate(new Date(t));
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


        }

        if (currentSort != null) {
            switch (currentSort) {
                case NEWEST_FIRST:
                    sortNewestFirst();
                    break;
                case OLDEST_FIRST:
                    sortOldestFirst();
                    break;
                case TITLE_ASCENDING:
                    sortTitleAscending();
                    break;
                case TITLE_DESCENDING:
                    sortTitleDescending();
                    break;
            }
        }
        else{
            sortOldestFirst();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

       spinner = (Spinner)menu.findItem(R.id.menuSort).getActionView();

       // spinner = (Spinner)v.findViewById(R.id.spinner); //(Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_type_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);


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

        if (id == R.id.restore_from_web){
            try {
                getEntriesFromWeb();
            }
            catch(Exception e){
                e.printStackTrace();
            }
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
                switch (currentSort){
                    case NEWEST_FIRST:
                        sortNewestFirst();
                        break;
                    case OLDEST_FIRST:
                        sortOldestFirst();
                        break;
                    case TITLE_ASCENDING:
                        sortTitleAscending();
                        break;
                    case TITLE_DESCENDING:
                        sortTitleDescending();
                        break;
                }
                //adapter.notifyDataSetChanged();

                writeToFile();

            }
        }

        if (requestCode == EDIT_ENTRY){
            if (resultCode == RESULT_OK){
                DiaryEntry entry = data.getParcelableExtra("entry");
                int pos = data.getIntExtra("position", 0);
                entryList.set(pos, entry);

                switch (currentSort){
                    case NEWEST_FIRST:
                        sortNewestFirst();
                        break;
                    case OLDEST_FIRST:
                        sortOldestFirst();
                        break;
                    case TITLE_ASCENDING:
                        sortTitleAscending();
                        break;
                    case TITLE_DESCENDING:
                        sortTitleDescending();
                        break;
                }

                //adapter.notifyDataSetChanged();

                writeToFile();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(ENTRY_LIST_KEY, entryList);
        Bundle s = new Bundle();
        savedInstanceState.putInt("s", currentSort.ordinal());
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        Log.d("selected", parent.getItemAtPosition(pos).toString());
        switch (parent.getItemAtPosition(pos).toString()){
            case "Oldest first":
                sortOldestFirst();
                break;
            case "Newest first":
                sortNewestFirst();
                break;
            case "Title ↑":
                sortTitleAscending();
                break;
            case "Title ↓":
                sortTitleDescending();
                break;
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void sortOldestFirst(){
        currentSort = Sort.OLDEST_FIRST;
        Collections.sort(entryList, new Comparator<DiaryEntry>() {
            public int compare (DiaryEntry d1, DiaryEntry d2){
                return d1.getEntryDate().compareTo(d2.getEntryDate());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sortNewestFirst(){
        currentSort = Sort.NEWEST_FIRST;
        Collections.sort(entryList, new Comparator<DiaryEntry>() {
            public int compare (DiaryEntry d1, DiaryEntry d2){
                return d2.getEntryDate().compareTo(d1.getEntryDate());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sortTitleAscending(){
        currentSort = Sort.TITLE_ASCENDING;
        Collections.sort(entryList, new Comparator<DiaryEntry>() {
            public int compare(DiaryEntry d1, DiaryEntry d2) {
                return d1.getTitle().compareTo(d2.getTitle());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sortTitleDescending(){
        currentSort = Sort.TITLE_DESCENDING;
        Collections.sort(entryList, new Comparator<DiaryEntry>() {
            public int compare(DiaryEntry d1, DiaryEntry d2) {
                return d2.getTitle().compareTo(d1.getTitle());
            }
        });
        adapter.notifyDataSetChanged();
    }

    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("sort", currentSort.toString());
        ed.commit();
    }

    public void getEntriesFromWeb() throws Exception{

        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://srighal-diary.herokuapp.com/entry/all")
                .build();

        client.newCall(request).enqueue(new Callback() {
            Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

            @Override
            public void onFailure(Request request, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String json = response.body().string();

                Gson gson = new Gson();

                Type collectionType = new TypeToken<Collection<DiaryEntry>>() {
                }.getType();
                Collection<DiaryEntry> entries = gson.fromJson(json, collectionType);
                Iterator<DiaryEntry> i = entryList.iterator();

                while (i.hasNext()){
                   DiaryEntry local = i.next();
                   boolean found = false;
                    for (DiaryEntry e : entries){
                        if (e.getId().equals(local.getId())){
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        i.remove();
                    }
                }

                for (DiaryEntry e : entries) {
                    boolean found = false;
                    for (DiaryEntry local : entryList){
                        if (e.getId().equals(local.getId())){
                            local.setEntry(e.getEntry());
                            local.setEntryDate(e.getEntryDate());
                            local.setGeocache((e.getGeocache()));
                            local.setTitle(e.getTitle());
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        entryList.add(e);
                }

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (currentSort) {
                            case NEWEST_FIRST:
                                sortNewestFirst();
                                break;
                            case OLDEST_FIRST:
                                sortOldestFirst();
                                break;
                            case TITLE_ASCENDING:
                                sortTitleAscending();
                                break;
                            case TITLE_DESCENDING:
                                sortTitleDescending();
                                break;
                        }
                    }
                });

                writeToFile();
            }
        });
    }

    private void writeToFile(){
        String FILENAME = "Diary_Entries";
        String Entries = "";
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.d("ERROR", "ISSUE SAVING FILE TO MEMORY");
        }
        for(DiaryEntry a: entryList){
            Entries=Entries + a.getTitle() + FIELD_DELIM + a.getEntry() + FIELD_DELIM + a.getGeocache() + FIELD_DELIM + a.getPicture() + FIELD_DELIM + a.getVoice()+FIELD_DELIM+a.getEntryDate().getTime()+FIELD_DELIM+a.getId();
            Entries += ENTRY_DELIM;
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
