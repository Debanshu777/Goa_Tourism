package com.example.goatourism.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.audiofx.EnvironmentalReverb;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.goatourism.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class CameraFragment extends Fragment {

    ImageButton capture;
    TextureView textureView;


    //check state orientation of output image
    private static  final SparseIntArray ORIENTATION=new SparseIntArray();
    static {
        ORIENTATION.append(Surface.ROTATION_0,90);
        ORIENTATION.append(Surface.ROTATION_90,0);
        ORIENTATION.append(Surface.ROTATION_180,270);
        ORIENTATION.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    Camera.PictureCallback jpegCallback;
    private ImageReader imageReader;


    //save to file
    private File file;
    private  static  final  int REQUEST_CAMERA_PERMISSION=200;
    private  boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


    CameraDevice.StateCallback stateCallBack=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice=camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera , container, false);
        textureView=view.findViewById(R.id.textureview);
        capture=view.findViewById(R.id.capture);
        assert  textureView!=null;
        textureView.setSurfaceTextureListener(textureListener);
        capture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        Toast.makeText(getContext(), "start", Toast.LENGTH_SHORT).show();
        if(cameraDevice==null) {
            return;
        }
        CameraManager manager=(CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
        try{
            Toast.makeText(getContext(), "try block", Toast.LENGTH_SHORT).show();
            CameraCharacteristics cameraCharacteristics=manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes=null;
            if(cameraCharacteristics !=null)
                jpegSizes=cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with customsize
            int width=640;
            int height=480;
            if(jpegSizes!=null&&jpegSizes.length>0)
            {
                width=jpegSizes[0].getWidth();
                height=jpegSizes[0].getHeight();
            }
            final ImageReader reader=ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface=new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            //Check orientation base on device
            int rotation=getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATION.get(rotation));
            Toast.makeText(getContext(), "before file", Toast.LENGTH_SHORT).show();

            //file=new File(Environment.getExternalStorageDirectory()+"/"+UUID.randomUUID().toString()+".jpg");
            ImageReader.OnImageAvailableListener readerListener=new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imagereader) {
                    Image image=null;
                    try{
                        Toast.makeText(getContext(), "inside file", Toast.LENGTH_SHORT).show();
                        image=reader.acquireLatestImage();
                        ByteBuffer buffer=image.getPlanes()[0].getBuffer();
                        byte[] bytes=new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        {
                            if(image!=null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    Toast.makeText(getContext(), "save", Toast.LENGTH_SHORT).show();
                    OutputStream outputStream=null;
                    try{
                        outputStream=new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream!=null)
                            outputStream.close();
                    }
                }
            };

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture=textureView.getSurfaceTexture();
            assert  texture!=null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface=new Surface(texture);
            captureRequestBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if(cameraDevice==null){
                        return;
                    }
                    cameraCaptureSession=session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getContext(), "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void updatePreview() {
        if(cameraDevice== null)
        {
            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void opencamera() {
        CameraManager manager=(CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId=manager.getCameraIdList()[0];
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map!=null;
            imageDimension=map.getOutputSizes(SurfaceTexture.class)[0];
            //check reltime permission if run higher api23
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity) getContext(),new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallBack,null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            opencamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode==REQUEST_CAMERA_PERMISSION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), "You cannot use camera without Permission", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        textureView.setVisibility(View.VISIBLE);
        startBackgroundThread();
        if(textureView.isAvailable()){
            opencamera();
        }
        else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        textureView.setVisibility(View.GONE);
        stopBackgroundThread();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread=null;
            mBackgroundThread=null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread=new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler=new Handler(mBackgroundThread.getLooper());
    }
}
