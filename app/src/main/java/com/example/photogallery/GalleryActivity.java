package com.example.photogallery;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_FILTER = 2;
    String currentPhotoPath;
    ImageView imageView;
    EditText captionText;
    File path;
    ArrayList<String> photoNames;
    ArrayList<String> filteredPhotos = new ArrayList<>();
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
        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photoNames = new ArrayList<>(Arrays.asList(path.list()));
        photoNumber = photoNames.size() - 1;
        if(photoNames != null){
            setImage();
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
        } else if(requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            String keyword = data.getStringExtra("keywordText");
            if(!(keyword.equals(""))) {
                try {
                    String filteredPhoto = captions.get(keyword).get(0);
                    photoNumber = photoNames.indexOf(filteredPhoto);
                    setImage();
                    captionText.setText(keyword);
                }catch(Exception e){
                    captionText.setText("No images matching");
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
        }catch(Exception e){
            Log.d("LAST PICTURE", "No pictures found");
        }
    }

    public void saveCaption(View view){
        String caption = captionText.getText().toString();
        if(captionExists(caption)){
            captions.get(caption).add(photoNames.get(photoNumber));
        }else{
            ArrayList<String> photo = new ArrayList<>();
            photo.add(photoNames.get(photoNumber));
            captions.put(caption, photo);
        }
        Log.d("CAPTION TEST", caption);
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
        ArrayList<String> tempPhotos = new ArrayList<>();
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

    public void setImage() {
        myBitmap = (BitmapFactory.decodeFile(path.getAbsolutePath() + "/" + photoNames.get(photoNumber)));
        Matrix matrix = new Matrix ();
        matrix.postRotate(90);
        myBitmap = Bitmap.createBitmap(myBitmap, 0 ,0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(myBitmap);
        captionText.setText(captionToSet(photoNames.get(photoNumber)));
    }
}