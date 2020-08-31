package com.luongnguyen.facedetect;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.opencsv.CSVWriter;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.opencv.core.CvType.CV_32SC1;

public class Methods extends AppCompatActivity {


    private static final String TAG = ".Methods";
    static final String LIST_FOLDER = "LIST";
    static final String GALLERY_FOLDER = "Gallery";
    static final String CLASSLIST_FILE = "ClassList.csv";
    private static final String EMAIL_RECIPIENT = "lnguy223@uwo.ca";
    private static String ATTENDANCELIST_FILE = "AttendanceList.csv";
    public static final int imgwidth = 200;
    public static final int imgheight = 200;
    public static final String LBPH_CLASSIFIER = "lbphClassifier.xml";
    public static  List<StudentInfo> TempList = new ArrayList<>();
    public static List<StudentInfo> AttendanceList = new ArrayList<>();
    public static boolean trainflag;

    //--------------------------------------------------------------------------------------------//
    //Method for face recognition model training --> return trainflag as true if success
    //--------------------------------------------------------------------------------------------//

     static public void train(Context context) {

        File RootPath = context.getExternalFilesDir(null);
        File GalleryPath = new File(RootPath.getAbsolutePath() +"/"+ GALLERY_FOLDER);
        File[] PicDirArray = GalleryPath.listFiles();

        if ((PicDirArray != null)&&(NumofPics(context)>0)) {
            int intLabel = 0;
            int counter = 0;
            int size = NumofPics(context);
            opencv_core.MatVector PicMatVect = new opencv_core.MatVector(size);
            opencv_core.Mat labels = new opencv_core.Mat(size, 1, CV_32SC1);

            //Creating, training and saving LBPH face recognizer
            opencv_face.FaceRecognizer mLBPHFaceRecognizer = opencv_face.LBPHFaceRecognizer.create();
            //filter photos from folder
            GenericExtFilter photoFilter = new GenericExtFilter("jpg", "png", "bmp");
            IntBuffer LabelsBuff = labels.createBuffer();

            //start scanning Gallery for each Pic Folder
            for (File dir : PicDirArray) {
                // Read images inside each folder under Gallery
                if (dir.isDirectory()) {
                    File PicsPath = dir;
                    File[] ImageArray = new File[0];
                    if (PicsPath != null) {
                        ImageArray = PicsPath.listFiles(photoFilter);
                    }
                    //Image processing and labels update
                    for (File image : ImageArray) {
                        //Reading the images in grayscale
                        opencv_core.Mat photo = imread(image.getAbsolutePath(),
                                opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                        //Resizing input images
                        resize(photo, photo, new opencv_core.Size(imgwidth, imgheight));
                        //Histogram equalizing
                        equalizeHist(photo, photo);
                        PicMatVect.put(counter, photo);
                        Log.i(TAG, (counter) + " times of buffer");
                        LabelsBuff.put(counter, intLabel);
                        counter++;
                    }
                    //Associate image label with ImageName as reference info
                    String ImageName = PicsPath.getName();
                    mLBPHFaceRecognizer.setLabelInfo(intLabel, ImageName);
                    intLabel++;// 1 label for each folder

                }//end if

            }//end for scanning folder

            //Start training with Images and Labels as inputs, then save training classifier in XML file.
            mLBPHFaceRecognizer.train(PicMatVect, labels);
            File trainedFaceRecognizerModel = new File(RootPath, LBPH_CLASSIFIER);
            try {
                trainedFaceRecognizerModel.createNewFile();
            } catch (IOException e) {
                Toast.makeText(context, "cannot create classifier file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            mLBPHFaceRecognizer.write(trainedFaceRecognizerModel.getAbsolutePath());
            trainflag = true;
            Toast.makeText(context, "training is complete", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "There is no photo to train. Input photos", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Cannot find Gallery");
        }

    }


    //--------------------------------------------------------------------------------------------//
    //METHOD to count all number of files in Gallery to initialize Photo ArrayList in Training()
    //--------------------------------------------------------------------------------------------//

    static public int NumofPics(Context context ) {
        int numpic = 0;
        File RootPath = context.getExternalFilesDir(null);
        File GalleryPath = new File(RootPath.getAbsolutePath() +"/"+GALLERY_FOLDER);
        File[] PicDirArray = GalleryPath.listFiles();
        GenericExtFilter photoFilter = new GenericExtFilter("jpg", "png", "bmp");

        for (File dir : PicDirArray) {
            if (dir.isDirectory()) {
                File[] Picspath = dir.listFiles(photoFilter);
                numpic = numpic + Picspath.length;
            }
        }
        return numpic;
    }

    //--------------------------------------------------------------------------------------------//
    //              Method to initialize first sample photo items to Gallery
    //--------------------------------------------------------------------------------------------//

    static public void InitializeGallery(Context context) {
        java.lang.reflect.Field[] fields = R.raw.class.getFields();
        File RootPath = context.getExternalFilesDir(null);
        File GalleryPath = new File(RootPath.getAbsolutePath() + "/" + GALLERY_FOLDER);
        if (!GalleryPath.exists()) {
            GalleryPath.mkdir();
            File dir1 = new File((GalleryPath.getAbsolutePath()+"/scarlet"));
            dir1.mkdir();
            File dir2 = new File((GalleryPath.getAbsolutePath()+"/brad"));
            dir2.mkdir();
            File dir3 = new File((GalleryPath.getAbsolutePath()+"/chadwick"));
            dir3.mkdir();
            for (int count = 0; count < fields.length; count++) {
                try {
                    OutputStream out = null;
                    int resourceID = fields[count].getInt(fields[count]);
                    InputStream in = context.getResources().openRawResource(resourceID);
                    Log.d(TAG,"here is the filename " +fields[count].getName());
                    if (fields[count].getName().substring(0, fields[count].getName().length() - 1).equals("scarlet")) {
                        File  file = new File(RootPath + "/" + GALLERY_FOLDER + "/" + "scarlet" + "/" + fields[count].getName() + ".jpg");
                        out = new FileOutputStream(file);
                    } else if (fields[count].getName().substring(0, fields[count].getName().length() - 1).equals("brad")) {
                        File  file = new File(RootPath + "/" + GALLERY_FOLDER + "/" + "brad" + "/" + fields[count].getName() + ".jpg");
                        out = new FileOutputStream(file);
                    } else if (fields[count].getName().substring(0, fields[count].getName().length() - 1).equals("chadwick")) {
                        File  file = new File(RootPath + "/" + GALLERY_FOLDER + "/" + "chadwick" + "/" + fields[count].getName() + ".jpg");
                        out = new FileOutputStream(file);
                    }
                    byte[] buf = new byte[2048];
                    int len;
                    if(out!=null) {
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        in.close();
                        out.close();
                    }

                }
                    catch(IllegalArgumentException e){
                        e.printStackTrace();
                    } catch(IllegalAccessException e){
                        e.printStackTrace();
                    } catch(FileNotFoundException e){
                        e.printStackTrace();
                    } catch(IOException e){
                        e.printStackTrace();
                    }

            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    //Method to initialize ClassList CSV file, in case of 1st time app installation
    //--------------------------------------------------------------------------------------------//


    static public void InitializeClassList(Context context){

        File RootPath = context.getExternalFilesDir(null);
        File ListPath = new File(RootPath.getAbsolutePath() +"/"+ Methods.LIST_FOLDER);
        Log.d(TAG,"reading LIST path");
        String filepath = ListPath +"/"+ Methods.CLASSLIST_FILE;
        File file = new File(filepath);
        //Create LIST folder if not exists, then copy a template from Resource

        if((!ListPath.exists())||(!file.exists())){
            ListPath.mkdir();
            Log.d(TAG,"created LIST folder");

            try {
                InputStream in = context.getResources().openRawResource(R.raw.classlist);
                FileOutputStream out = null;
                out = new FileOutputStream(filepath);
                byte[] buff = new byte[1024];
                int read = 0;
                Log.d(TAG, "copying files from resource");
                try {
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                } finally {
                    in.close();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Got classlist.csv from template");
            Methods.ReadClasslist(context);
        }else{
            Methods.ReadClasslist(context);
        }
    }

    //--------------------------------------------------------------------------------------------//
    //           Method to read ClassList CSV file for List of students and IDs.
    //--------------------------------------------------------------------------------------------//

    static public void ReadClasslist(Context context) {
        boolean flag = false;
        File RootPath = context.getExternalFilesDir(null);
        ATTENDANCELIST_FILE = ATTENDANCELIST_FILE.replaceAll(".csv","-Date: "+getDate() +".csv" );
        String filepath = RootPath.getAbsolutePath() +"/"+LIST_FOLDER + "/" + ATTENDANCELIST_FILE;
        Log.d(TAG,"Attendance list file path looks like:"+filepath);
        File listfile = new File(filepath);
        if(!listfile.exists()){
            Log.d(TAG,"attendancelist not exists, copy from Classlist");
            filepath = RootPath.getAbsolutePath() +"/"+LIST_FOLDER + "/" + CLASSLIST_FILE;
            flag = true;
        }else{
            Log.d(TAG,"attendancelist exists, copy from Attendancelist");
        }
        Log.d(TAG,"filepath is now"+ filepath);
        //Read all info from Classlist.csv or AttendanceList.csv file into ArrayList TempList
        BufferedReader Reader = null;
        try {
            Reader = new BufferedReader(new FileReader(filepath));
        } catch (FileNotFoundException e) {
            Log.d("Main Activity :","Cannot find Classlist.csv");
            e.printStackTrace();
        }
        String line = "";
        int counter =0;
        try {
            while ((line = Reader.readLine()) != null) {
                //Split by comma ,
                String[] tokens = line.split(",");
                //Read data
                StudentInfo info = new StudentInfo();
                info.setID(tokens[0]);
                info.setName(tokens[1]);

                if(!flag){
                    info.setStatus(tokens[2]);
                    info.setDate(tokens[3]);
                    info.setID(info.getID().replaceAll("^\"+|\"+$", ""));
                    info.setName(info.getName().replaceAll("^\"+|\"+$", ""));
                    info.setDate(info.getDate().replaceAll("^\"+|\"+$", ""));
                    info.setStatus(info.getStatus().replaceAll("^\"+|\"+$", ""));
                }
                TempList.add(info);
                Log.d(TAG,"Just created info " + TempList.get(counter));
                counter++;
            }
        }catch(IOException e){
            Log.d(TAG,"Error reading data file on line" + line,e);
            e.printStackTrace();
        }

        //Make AttendanceList a copy of ClassList
        AttendanceList = TempList;

        //Update attendance list with default "absent" status for all students and current Date
        counter =0;
        for(StudentInfo info:AttendanceList){
            if(counter==0){
                info.setStatus("Status");
                info.setDate("Date");
                counter++;
            }else if(flag) {
                    info.setStatus("absent");
                    info.setDate(getDate());
                    }

        }

    }
    //--------------------------------------------------------------------------------------------//
    // Method to print test the Attendance ArrayList
    //--------------------------------------------------------------------------------------------//

    static public void Printest(){
        for(StudentInfo info:AttendanceList){
            Log.d(TAG,"just update info" + info);
        }
    }


    //--------------------------------------------------------------------------------------------//
    // Method to save all Student attendance info from temporary ArrayList to CSV file inside LIST_FOLDER
    //--------------------------------------------------------------------------------------------//

    static public void WriteCSV(List<StudentInfo> list,Context context ){

        File RootPath = context.getExternalFilesDir(null);
        String filepath = RootPath.getAbsolutePath()+"/"+LIST_FOLDER + "/" +  ATTENDANCELIST_FILE ;

        List<String[]> entries = new ArrayList<>();
        for(StudentInfo info:list){
            String[] line = {info.getID(),info.getName(),info.getStatus(),info.getDate()};
            entries.add(line);
        }

        try (FileOutputStream fos = new FileOutputStream(filepath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw))
        {
            writer.writeAll(entries);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //--------------------------------------------------------------------------------------------//

    // Method to email CSV file to Email Recipient of choice
    //--------------------------------------------------------------------------------------------//

    static public void EmailCSV(Context context ){

        //Prepare URI
        File RootPath = context.getExternalFilesDir(null);
        String filepath = RootPath.getAbsolutePath()+"/"+LIST_FOLDER;
        File filelocation = new File(filepath,ATTENDANCELIST_FILE);
        Uri uricontent = FileProvider.getUriForFile(context,
                "com.luongnguyen.facedetect.provider", filelocation);
        //Initialize email intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        String to[] = {EMAIL_RECIPIENT};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // add the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, uricontent);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance list update - Date: "+getDate());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "- Sent from FaceDetect app - ");

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uricontent, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Log.d(TAG, "here is your file location : " + filelocation );
        Log.d(TAG, "here is your URI: " + uricontent );

        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.startActivity(Intent.createChooser(emailIntent , "Sending Attendance CSV file"));
    }
    //--------------------------------------------------------------------------------------------//
    //Method to get the date
    //--------------------------------------------------------------------------------------------//
    static private String getDate(){
        //Get the current date with specific format
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat Formater = new SimpleDateFormat("MMM-dd-yyyy");
        String formateddate = Formater.format(date);
        return formateddate;
    }






    //----------------------------------------CLASSES -------------------------------------------//
    //A class filter to get photo files filter - for extensions of choice
    static public class GenericExtFilter implements FilenameFilter {
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




}
