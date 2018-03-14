package com.example.aditya.bitesnap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    CameraView cameraView;
    ImageView click,galary;
    ProgressBar load;
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }
    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        load=findViewById(R.id.load);

        cameraView=findViewById(R.id.cam);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override public void onEvent(CameraKitEvent cameraKitEvent) {}
            @Override public void onError(CameraKitError cameraKitError) {}
            @Override
            public void onImage(CameraKitImage cameraKitImage)
            {
                Bitmap bitmap=cameraKitImage.getBitmap();load.setVisibility(View.VISIBLE);
                StorageReference sr= FirebaseStorage.getInstance().getReference().child("image.jpg");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap=bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                sr.putBytes(baos.toByteArray())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @SuppressLint("ApplySharedPref")
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests")
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();load.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {load.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"Failed", Toast.LENGTH_LONG).show();
                            }
                        });

            }
            @Override public void onVideo(CameraKitVideo cameraKitVideo) {}
        });

        click=findViewById(R.id.click);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.captureImage();
            }
        });

        galary=findViewById(R.id.gallery);
        galary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select an Image"),0);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        if (resultcode == RESULT_OK && requestCode == 0)
        {
            Uri uri=intent.getData();load.setVisibility(View.VISIBLE);
            StorageReference sr= FirebaseStorage.getInstance().getReference().child("image.jpg");
            sr.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {load.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {load.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Failed", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
