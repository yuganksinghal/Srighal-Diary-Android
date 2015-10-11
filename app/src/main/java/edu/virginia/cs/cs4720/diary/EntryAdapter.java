package edu.virginia.cs.cs4720.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;

import edu.virginia.cs.cs4720.diary.myapplication.R;

/**
 * Created by Mihir on 10/10/2015.
 */
public class EntryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DiaryEntry> list;

    private static LayoutInflater inflater = null;

    public EntryAdapter(Context context, ArrayList<DiaryEntry> list) {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row, null);
        TextView title = (TextView) vi.findViewById(R.id.title);
        title.setText(list.get(position).getTitle());
        TextView date = (TextView) vi.findViewById(R.id.date);
        date.setText(DateFormat.getDateInstance().format( list.get(position).getEntryDate() ) );

        TextView prev = (TextView) vi.findViewById(R.id.preview);
        String previewString = list.get(position).getEntry();
        if (previewString.length() > 100){
            previewString = previewString.substring(0, 100)+"\u2026";
        }
        prev.setText(previewString);
        return vi;
    }
}
