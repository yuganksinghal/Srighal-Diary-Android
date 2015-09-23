package edu.virginia.cs.cs4720.diary;

import android.Manifest;
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

import java.util.ArrayList;

import edu.virginia.cs.cs4720.diary.myapplication.R;


public class MainActivity extends AppCompatActivity  {

    public ArrayList<DiaryEntry> entryList;
    ArrayAdapter<DiaryEntry> adapter;

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
        adapter = new ArrayAdapter<DiaryEntry>(this, android.R.layout.simple_list_item_1, entryList);

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

    public void createNewEntry(View v){
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
            }
        }

        if (requestCode == EDIT_ENTRY){
            if (resultCode == RESULT_OK){
                DiaryEntry entry = data.getParcelableExtra("entry");
                int pos = data.getIntExtra("position", 0);
                entryList.set(pos, entry);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(ENTRY_LIST_KEY, entryList);
    }

}
