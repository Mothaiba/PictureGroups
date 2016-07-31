package com.example.lordone.picturegroups;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lordone.picturegroups.BaseClasses.FileArrayAdapter;
import com.example.lordone.picturegroups.BaseClasses.GV;
import com.example.lordone.picturegroups.BaseClasses.Item;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lord One on 7/8/2016.
 */
public class FileBrowserActivity extends ListActivity {

    private String _path;
    TextView title;
    Button mSelectButton;
    FileArrayAdapter adapter;
    int function_this;
    public static int train_func = 0,
                      train_100_func = 1,
                      static_test_func = 2,
                      test_accuracy_func = 3,
                      group_picture_func = 4;
    HashMap mapName = new HashMap();
    HashMap mapActivity = new HashMap();
    TextView folder_path;

    void initMaps() {
        mapName.put(train_func, "Train");
        mapName.put(train_100_func, "Train & Test");
        mapName.put(static_test_func, "Static Test");
        mapName.put(test_accuracy_func, "Test Accuracy");
        mapName.put(group_picture_func, "Group Pictures");

        mapActivity.put(train_func, TrainActivity.class);
        mapActivity.put(train_100_func, Train100RandomActivity.class);
        mapActivity.put(static_test_func, StaticTestActivity.class);
        mapActivity.put(test_accuracy_func, TestAccuracyActivity.class);
        mapActivity.put(group_picture_func, GroupPicturesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);

        function_this = getIntent().getIntExtra("function", 0);

        initMaps();
        title = (TextView) findViewById(R.id.description_browser);
        title.setText((String) mapName.get(function_this));

        // Use the current directory as title
        _path = "/";
        if (getIntent().hasExtra("path")) {
            _path = getIntent().getStringExtra("path");
        }
        setTitle(_path);

        folder_path = (TextView) findViewById(R.id.folder_path);
        folder_path.setText(_path);

        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();

        // Read all files sorted into the values-array
        File subDirList = new File(_path);
        if (!subDirList.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] _list = subDirList.list();
        if (_list != null) {
            for (String _file : _list) {
                if (!_file.startsWith(".")) {
                    File file;
                    if(_path.endsWith(File.separator))
                        file = new File(_path + _file);
                    else
                        file = new File(_path + File.separator + _file);

                    Date lastModDate = new Date(file.lastModified());
                    DateFormat formater = DateFormat.getDateTimeInstance();
                    String date_modify = formater.format(lastModDate);

                    if (file.isDirectory()) {
                        String[] _subList = file.list();
                        String num_items;
                        if (_subList == null)
                            num_items = "0 item";
                        else if (_subList.length <= 1)
                            num_items = _subList.length + " item";
                        else
                            num_items = _subList.length + " items";

                        dir.add(new Item(file.getName(), num_items, date_modify, file.getAbsolutePath(), "directory_icon"));
                    }
                    else if (file.isFile()) {
                        String file_length;
                        if(file.length() <= 1)
                            file_length = file.length() + " Byte";
                        else
                            file_length = file.length() + " Bytes";

                        if(_file.endsWith(".jpg") || _file.endsWith(".jpeg") || _file.endsWith(".png") || _file.endsWith(".gif"))
                            fls.add(new Item(file.getName(), file_length, date_modify, file.getAbsolutePath(), "image_icon"));
                        else
                            fls.add(new Item(file.getName(), file_length, date_modify, file.getAbsolutePath(), "file_icon"));
                    }
                }
            }
        }
        Collections.sort(dir);
        Collections.sort(fls);

        dir.addAll(fls);

        adapter = new FileArrayAdapter(FileBrowserActivity.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);

        mSelectButton = (Button) findViewById(R.id.selectButton);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileBrowserActivity.this, (Class) mapActivity.get(function_this));
                intent.putExtra("path", _path);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Item it =  adapter.getItem(position);
        String _filename = it.getName();
        String _backup_filename = _filename;

        if (_path.endsWith(File.separator)) {
            _filename = _path + _filename ;
        } else {
            _filename = _path + File.separator + _filename;
        }

        File file = new File(_filename);

        if (file.isDirectory()) {
            Intent intent = new Intent(this, FileBrowserActivity.class);
            intent.putExtra("path", _filename);
            intent.putExtra("function", function_this);
            startActivity(intent);
        }
        // not a directory, so we assume this is a file
        else if (function_this ==  static_test_func || function_this == group_picture_func) {
                if(_filename.endsWith(".jpg") || _filename.endsWith(".jpeg") || _filename.endsWith(".png") || _filename.endsWith(".gif")) {
                    Intent intent = new Intent(FileBrowserActivity.this, (Class) mapActivity.get(function_this));
                    intent.putExtra("path", _path);
                    intent.putExtra("file_name", _backup_filename);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(this, "We do not support this file extension!", Toast.LENGTH_LONG).show();
                }
        }
        else {
                Toast.makeText(this,"Choosing a file is not allowed for this function!", Toast.LENGTH_LONG).show();
        }
    }

}
