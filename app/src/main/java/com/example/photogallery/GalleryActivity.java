package com.example.photogallery;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_FILTER = 2;
    String currentPhotoPath;
    String currentPhotoName;
    ImageView imageView;
    EditText captionText;
    TextView timestampText;
    TextView geotagText;
    File path;
    ArrayList<String> photoNames;
    HashMap<String,ArrayList<String>> captions = new HashMap<>();
    int photoNumber;
    Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ActivityCompat.requestPermissions(GalleryActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);
        imageView = findViewById(R.id.galleryView);
        captionText = findViewById(R.id.captionText);
        timestampText = findViewById(R.id.timeText);
        geotagText = findViewById(R.id.geoText);
        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photoNames = new ArrayList<>(Arrays.asList(path.list()));
        photoNumber = photoNames.size() - 1;
        if(photoNames != null){
            setImage();
            setTimestamp();
            setGeotag(false, "no");
        }
    }

    public void takePicture(View view) {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.photogallery.fileprovider",
                        photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            photoNames = new ArrayList<>(Arrays.asList(path.list()));
            photoNumber = photoNames.size() - 1;
            setImage();
            setTimestamp();
            setGeotag(false, "no");
        } else if(requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            String keyword = data.getStringExtra("keywordText");
            String startDateStr = data.getStringExtra("startDateText");
            String endDateStr = data.getStringExtra("endDateText");
            String topLatStr = data.getStringExtra("topLatText");
            String topLongStr = data.getStringExtra("topLongText");
            String btmLatStr = data.getStringExtra("btmLatText");
            String btmLongStr = data.getStringExtra("btmLongText");

            if(!(keyword.equals(""))) {
                try {
                    String filteredPhoto = captions.get(keyword).get(0);
                    photoNumber = photoNames.indexOf(filteredPhoto);
                    setImage();
                    setTimestamp();
                    setGeotag(false, "no");
                }catch(Exception e){
                    Log.d("Keyword Search", e.toString());
                    captionText.setText("Keyword not found. Please ensure correct word");
                }
            }else if(!startDateStr.equals("") && !endDateStr.equals("")){
                DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                try {
                    Date startDate = sdf.parse(startDateStr);
                    Date endDate = sdf.parse(endDateStr);
                    for(int i = 0; i < photoNames.size(); i++){
                        String tempPhotoName = photoNames.get(i);
                        String[] tempPhotoNameSplit = tempPhotoName.split("_");
                        String tempDateStr = tempPhotoNameSplit[0];
                        try {
                            Date tempDate = sdf.parse(tempDateStr);
                            if((tempDate.after(startDate) || tempDate.equals(startDate)) && (tempDate.before(endDate) || tempDate.equals(endDate))){
                                photoNumber = photoNames.indexOf(tempPhotoName);
                                setImage();
                                setTimestamp();
                                setGeotag(false, "no");
                            }
                        } catch (ParseException e) {
                            Log.d("Date Parse", e.toString());
                        }
                    }
                } catch (ParseException e) {
                    Log.d("Date Search", e.toString());
                    captionText.setText("Date not found. Please ensure correct format");
                }
            }else if(!topLatStr.equals("") && !topLongStr.equals("") && !btmLatStr.equals("") && !btmLongStr.equals("")){
                float topLat = Float.parseFloat(topLatStr);
                float topLong = Float.parseFloat(topLongStr);
                float btmLat = Float.parseFloat(btmLatStr);
                float btmLong = Float.parseFloat(btmLongStr);

                for(int i = 0; i < photoNames.size(); i++){
                    String tempPhotoName = photoNames.get(i);
                    float[] coords = getGeotag(true, tempPhotoName);
                    if(coords != null) {
                        try {
                            float lat = coords[0];
                            float lon = coords[1];
                            if ((Math.abs(lat) <= Math.abs(topLat) && Math.abs(lat) >= Math.abs(btmLat)) &&
                                    (Math.abs(lon) <= Math.abs(topLong) && Math.abs(lon) >= Math.abs(btmLong))) {
                                photoNumber = photoNames.indexOf(tempPhotoName);
                                setImage();
                                setTimestamp();
                                setGeotag(false, "no");
                            }
                        } catch (Exception e) {
                            Log.d("Location Search", e.toString());
                            captionText.setText("Location not found. Please ensure correct coordinates");
                        }
                    }
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  //prefix
            ".jpg",   //suffix
            storageDir     //directory
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void nextPicture(View view){
        photoNumber += 1;
        if(photoNumber >= photoNames.size()){
            photoNumber = 0;
        }
        try{
            setImage();
            setTimestamp();
            setGeotag(false, "no");
        }catch(Exception e){
            Log.d("NEXT PICTURE", "No pictures found");
        }
    }

    public void lastPicture(View view){
        photoNumber -= 1;
        if(photoNumber <= 0){
            photoNumber = photoNames.size() - 1;
        }
        try{
            setImage();
            setTimestamp();
            setGeotag(false, "no");
        }catch(Exception e){
            Log.d("LAST PICTURE", "No pictures found");
        }
    }

    public void saveCaption(View view){
        String caption = captionText.getText().toString();
        deleteOldCaptionForPhoto();
        if(captionExists(caption)){
            captions.get(caption).add(currentPhotoName);
        }else{
            ArrayList<String> photo = new ArrayList<>();
            photo.add(currentPhotoName);
            captions.put(caption, photo);
        }
        Log.d("CAPTION TEST", caption);
    }

    public void deleteOldCaptionForPhoto(){
        Set<String> keys = captions.keySet();
        ArrayList<String> tempPhotos;
        for(String tempKey : keys){
            tempPhotos = captions.get(tempKey);
            for(int i = 0; i < tempPhotos.size(); i++){
                if(tempPhotos.get(i).equals(currentPhotoName)){
                    captions.get(tempKey).remove(i);
                }
            }
        }
    }

    private boolean captionExists(String caption){
        boolean captionExists = false;
        Set<String> keys = captions.keySet();
        for(String tempKey : keys){
            if(tempKey.equals(caption)){
                captionExists = true;
            }
        }
        return captionExists;
    }

    private String captionToSet(String photoName){
        String captionToSet = "";
        ArrayList<String> tempPhotos;
        Set<String> keys = captions.keySet();
        for(String tempKey : keys){
            tempPhotos = captions.get(tempKey);
            for(int i = 0; i < tempPhotos.size(); i++){
                if(tempPhotos.get(i).equals(photoName)){
                    captionToSet = tempKey;
                    return(captionToSet);
                }
            }
        }
        return captionToSet;
    }

    public void filterPhotos(View view){
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivityForResult(searchIntent, REQUEST_FILTER);
    }

    private void setImage() {
        currentPhotoName = photoNames.get(photoNumber);
        myBitmap = (BitmapFactory.decodeFile(path.getAbsolutePath() + "/" + currentPhotoName));
        Matrix matrix = new Matrix ();
        matrix.postRotate(90);
        myBitmap = Bitmap.createBitmap(myBitmap, 0 ,0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(myBitmap);
        captionText.setText(captionToSet(currentPhotoName));
    }

    private void setTimestamp(){
        String[] photoNameSplit = currentPhotoName.split("_");
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = (photoNameSplit[0] + "_" + photoNameSplit[1]);
        try {
            Date timestamp = sdf.parse(time);
            timestampText.setText(timestamp.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private float[] getGeotag(boolean useCustomPhotoName, String customPhotoName){
        ExifInterface exif;
        float[] coords = null;
        geotagText.setText("No geotag found");
        try {
            if(useCustomPhotoName){
                exif = new ExifInterface(path.getAbsolutePath() + "/" + customPhotoName);
            }else{
                exif = new ExifInterface(path.getAbsolutePath() + "/" + currentPhotoName);
            }
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if(lat != null && lon != null) {
                float latDD = convertDMSToDD(lat, latRef);
                float lonDD = convertDMSToDD(lon, lonRef);
                coords = new float[2];
                coords[0] = latDD;
                coords[1] = lonDD;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coords;
    }

    private void setGeotag(boolean useCustomPhotoName, String customPhotoName){
        float coords[] = getGeotag(useCustomPhotoName, customPhotoName);
        if(coords != null){
            geotagText.setText(coords[0] + "\n" + coords[1]);
        } else {
            geotagText.setText("No geotag found");
        }
    }

    private float convertDMSToDD(String coordinateInDMS, String ref){
        String[] coordinateInDMSSplit = coordinateInDMS.split("/[0-9],");
        coordinateInDMSSplit[2] = coordinateInDMSSplit[2].replaceAll("/[0-9]", "");
        float decimalDD = ((Float.parseFloat(coordinateInDMSSplit[1]) * 60) + Float.parseFloat(coordinateInDMSSplit[2])) / 3600;
        float coordinateDD = Float.parseFloat(coordinateInDMSSplit[0]) + decimalDD;
        if(ref.equals("S") || ref.equals("W")){
            coordinateDD = coordinateDD * -1;
        }
        return coordinateDD;
    }
}