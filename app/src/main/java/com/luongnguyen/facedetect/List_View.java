package com.luongnguyen.facedetect;

import java.io.FilenameFilter;

import java.util.Arrays;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class List_View extends AppCompatActivity {


    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        mListView = findViewById(R.id.List);
        CustomAdaptor customAdaptor = new CustomAdaptor();
        mListView.setAdapter(customAdaptor);

    }
    class CustomAdaptor extends BaseAdapter{

        ArrayList images = ReadGallery().imagepath;
        ArrayList names = ReadGallery().imagename;

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.customlayout,null);
            ImageView mImageView = v.findViewById(R.id.Photo);
            TextView mTextView = v.findViewById(R.id.Name);
            mImageView.setImageBitmap(BitmapFactory.decodeFile(images.get(position).toString()));
            mTextView.setText(names.get(position).toString());
            return v;
        }

    }
    //--------------------------------------------------------------------------------------------//
    //                        METHOD READ ALL PHOTOS FROM GALLERY
    //--------------------------------------------------------------------------------------------//

    public FolderInfo ReadGallery()
    {
        FolderInfo outputinfo = new FolderInfo(new ArrayList(),new ArrayList());
        File RootPath = getExternalFilesDir(null);
        File GalleryPath = new File(RootPath.getAbsolutePath() +"/"+ Methods.GALLERY_FOLDER);
        File[] PicDirArray = GalleryPath.listFiles();
        GenericExtFilter PhotoFilter = new GenericExtFilter("jpg", "png", "bmp");

        if (PicDirArray != null) {
            for(File dir:PicDirArray) {
                if(dir.isDirectory()) {
                    File[] filelist = dir.listFiles(PhotoFilter);
                    Arrays.sort(filelist);

                    for (int i = 0; i < filelist.length; i++) {
                        File file = filelist[i];
                        String filePath = file.getPath();
                        outputinfo.imagepath.add(filePath);
                        String fileName = file.getName().substring(0, (file.getName().length() - 4));
                        outputinfo.imagename.add(fileName);
                    }
                }
            }
        }
        return outputinfo;
    }


    //----------------------------------------CLASSES -------------------------------------------//

    //A class filter to get photo files filter - for extensions of choice
    //--------------------------------------------------------------------------------------------//

    public class GenericExtFilter implements FilenameFilter {
        private String[] exts;

        public GenericExtFilter(String... exts) {
            this.exts = exts;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String ext : exts) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }

            return false;
        }
    }
    //--------------------------------------------------------------------------------------------//
    //A class to combine Imagepath and Imagename together for List View
    //--------------------------------------------------------------------------------------------//

    public class FolderInfo {
        public final ArrayList imagepath;
        public final ArrayList imagename;

        public FolderInfo(ArrayList imagepath, ArrayList imagename) {
            this.imagepath = imagepath;
            this.imagename = imagename;
        }
    }

}

