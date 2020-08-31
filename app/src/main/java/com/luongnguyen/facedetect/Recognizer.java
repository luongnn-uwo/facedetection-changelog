package com.luongnguyen.facedetect;

import android.content.pm.PackageManager;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static com.luongnguyen.facedetect.Methods.GALLERY_FOLDER;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;



public class Recognizer extends AppCompatActivity implements View.OnClickListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG ="recognizer" ;
    public static final int imgwidth = 100;
    public static final int imgheight = 100;
    public static final double Threshold = 115.0D;
    public static final String LBPH_CLASSIFIER = "lbphClassifier.xml";

    String PredictedName;
    boolean checktrain = Methods.trainflag;
    opencv_face.FaceRecognizer mLBPHFaceRecognizer = opencv_face.LBPHFaceRecognizer.create();


    // Screen items on XML
    Button ConfirmButton;
    ImageView HeadImage;
    TextView  Recognized,RecogName;
    JavaCameraView RecogFaceView;

    // Source variables
    File cascFile1;
    CascadeClassifier faceDetector1;
    Mat mRgba1, mGrey1;




    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba1 = new Mat();
        mGrey1 = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba1.release();
        mGrey1.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba1 = inputFrame.rgba();
        mGrey1 = inputFrame.gray();

        //FACE DETECTION AND RECTANGLE
        MatOfRect faceDetections = new MatOfRect();
        faceDetector1.detectMultiScale(mRgba1,faceDetections);
        Rect[] facesArray = faceDetections.toArray();

        for(Rect rect:facesArray){
            Imgproc.rectangle(mRgba1, new Point(rect.x,rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0,0,255));
        }

        //FACE PREDICTION ACTIVITY  ----- > TODO:Rename Variable
        if(facesArray.length==1){
            try {

                //Conversion from OpenCV Mat to JavaCV Mat
                opencv_core.Mat javaCvMat = new opencv_core.Mat((Pointer) null) {{
                    address = mGrey1.getNativeObjAddr();
                }};
                //Picture resizing
                resize(javaCvMat, javaCvMat, new opencv_core.Size(imgwidth, imgheight));
                //Histogram equalizing
                equalizeHist(javaCvMat, javaCvMat);
                IntPointer label = new IntPointer(1);
                DoublePointer confidence = new DoublePointer(1);
                mLBPHFaceRecognizer.predict(javaCvMat, label, confidence);

                int predictedLabel = label.get(0);
                double ConfidenceLevel = confidence.get(0);

                Log.d(TAG, "Prediction completed, predictedLabel: " + predictedLabel + ", ConfidenceLevel: " + ConfidenceLevel);
                BytePointer bp = mLBPHFaceRecognizer.getLabelInfo(predictedLabel);
                if (predictedLabel == -1 || ConfidenceLevel >= Threshold) {
                    PredictedName = "???";
                } else {
                    PredictedName = bp.getString().trim();
                }

                //Show result on screen
                for (Rect face : facesArray) {
                    int posX = (int) Math.max(face.tl().x - 10, 0);
                    int posY = (int) Math.max(face.tl().y - 10, 0);
                    Imgproc.putText(mRgba1, "Closest picture" + PredictedName, new Point(posX, posY),
                            Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 255, 0, 255));
                }
                //TODO: Add Update CSV file

            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage(), e);
            }
        }
        // End of prediction
        return mRgba1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognizer);
        File PicsPath = getExternalFilesDir(null);
        ConfirmButton = findViewById(R.id.ConfirmButton);
        HeadImage =findViewById(R.id.HeadImage);
        Recognized =findViewById(R.id.Recognized);
        RecogName = findViewById(R.id.RecogName);
        RecogFaceView = findViewById(R.id.RecogFaceView);

        //Ask for permission
        isExternalStoragewritable();
        isExternalStoragereadable();

        // Set onClick action
        ConfirmButton.setOnClickListener(this);

        //OpenCV Manager calling
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseCallback);

        }else{
            baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        RecogFaceView.setCvCameraViewListener(this);

    //---------------------------------------------------------------------------------------------//
    // Read from Training Classifier, and confirm if found.
    //---------------------------------------------------------------------------------------------//
        if(checktrain) {

            try {
                mLBPHFaceRecognizer.read((PicsPath+ "/"+LBPH_CLASSIFIER));
                Toast.makeText(this, "Classifier was found. Let's recognize !", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage(), e);
                Toast.makeText(this, "Cannot get classifier", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(this, "training was not complete, go back to training", Toast.LENGTH_SHORT).show();
        }

    }

    //---------------------------------------------------------------------------------------------//
    // Calling Baseloadercallback method from OPENCV to assist face detection process
    //---------------------------------------------------------------------------------------------//
    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            switch(status){
                case LoaderCallbackInterface.SUCCESS: {
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                        File CascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        cascFile1 = new File(CascadeDir, "haarcascade_frontalface_alt2.xml");

                        FileOutputStream fos = new FileOutputStream(cascFile1);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        fos.close();

                        faceDetector1 = new CascadeClassifier(cascFile1.getAbsolutePath());
                        if (faceDetector1.empty()) {
                            faceDetector1 = null;
                        } else {
                            CascadeDir.delete();
                        }
                        RecogFaceView.enableView();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default:
                    super.onManagerConnected(status);
                    break;
            }

        }
    };

    @Override

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ConfirmButton:
                try {
                    //-------------After Click CONFIRM on screen----------------------------------//
                    //Show name of predicted student
                    RecogName.setText(PredictedName);
                    //Show image of confirmed student from archive at corner
                    if (PredictedName!="???"){
                        File RootPath = getExternalFilesDir(null);
                        File Dirpath = new File(RootPath.getAbsolutePath() +"/"+ GALLERY_FOLDER + "/"+PredictedName);
                        File[] ImageArray = Dirpath.listFiles();
                        if(ImageArray.length>0)
                        HeadImage.setImageBitmap(BitmapFactory.decodeFile(ImageArray[0].getAbsolutePath()));
                        //Update attendance list
                        UpdateAttendance(PredictedName);
                    }

                    Toast.makeText(this, "Attendance was updated", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();

                }

                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isExternalStoragereadable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
            Log.i("State", "Yes, it is readable");
            return true;

        } else {
            return false;
        }
    }

    private boolean isExternalStoragewritable(){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is writable");
            return true;

        }else{
            return false;
        }

    }

    //--------------------------------------------------------------------------------------------//
    //Method to update Attendance list with students that are present
    //--------------------------------------------------------------------------------------------//

    private void UpdateAttendance(String studentname){

        for(StudentInfo info:Methods.AttendanceList){
            if(info.getName().equals(studentname)){
               info.setStatus("present");
            }
        }
    }


} //end of class
