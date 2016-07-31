package com.example.lordone.picturegroups.BaseClasses;

/**
 * Created by Lord One on 7/8/2016.
 */

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lordone.picturegroups.BaseClasses.Item;
import com.example.lordone.picturegroups.R;


public class FileArrayAdapter extends ArrayAdapter<Item>{

    private Context c;
    private int id;
    private List<Item>items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }
    public Item getItem(int i)
    {
        return items.get(i);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

               /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        final Item o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.TextView01);
            TextView t2 = (TextView) v.findViewById(R.id.TextView02);
            TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);
                       /* Take the ImageView from layout and set the city's image */
            ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
            String uri = "drawable/" + o.getImage();
            int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
//            Drawable image = c.getResources().getDrawable(imageResource);
            String ii = o.getImage();
            Drawable image = null;
            if (o.getImage().compareTo("directory_icon") == 0) {
                image = ContextCompat.getDrawable(c, R.drawable.directory_icon);
                imageCity.setImageDrawable(image);
            }
            else if(o.getImage().compareTo("file_icon") == 0) {
                image = ContextCompat.getDrawable(c, R.drawable.file_icon);
                imageCity.setImageDrawable(image);
            }
            else {
                String _file = o.getPath();
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(_file), 66, 66);
                imageCity.setImageBitmap(thumbImage);
            }

            if(t1!=null)
                t1.setText(o.getName());
            if(t2!=null)
                t2.setText(o.getData());
            if(t3!=null)
                t3.setText(o.getDate());

        }
        return v;
    }

}

