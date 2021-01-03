package com.eszdman.photoncamera.processing.opengl.postpipeline;

import android.graphics.*;
import android.util.Log;

import com.eszdman.photoncamera.processing.opengl.*;
import com.eszdman.photoncamera.processing.parameters.IsoExpoSelector;
import com.eszdman.photoncamera.R;
import com.eszdman.photoncamera.processing.render.Parameters;
import com.eszdman.photoncamera.app.PhotonCamera;
import java.nio.ByteBuffer;

public class PostPipeline extends GLBasePipeline {
    public ByteBuffer stackFrame;
    public ByteBuffer lowFrame;
    public ByteBuffer highFrame;
    float regenerationSense = 1.f;
    float AecCorr = 1.f;
    public int getRotation() {
        int rotation = PhotonCamera.getParameters().cameraRotation;
        String TAG = "ParseExif";
        Log.d(TAG, "Gravity rotation:" + PhotonCamera.getGravity().getRotation());
        Log.d(TAG, "Sensor rotation:" + PhotonCamera.getCaptureController().mSensorOrientation);
        return rotation;
    }
    @SuppressWarnings("SuspiciousNameCombination")
    private Point getRotatedCoords(Point in){
        switch (getRotation()){
            case 0:
            case 180:
                return in;
            case 90:
            case 270:
                return new Point(in.y,in.x);
        }
        return in;
    }
    public Bitmap Run(ByteBuffer inBuffer, Parameters parameters){
        mParameters = parameters;
        mSettings = PhotonCamera.getSettings();
        Point rotated = getRotatedCoords(parameters.rawSize);
        /*if (PhotonCamera.getSettings().selectedMode == CameraMode.NIGHT) {
            rotated.x/=2;
            rotated.y/=2;
        }*/
        Bitmap output = Bitmap.createBitmap(rotated.x,rotated.y, Bitmap.Config.ARGB_8888);

        GLCoreBlockProcessing glproc = new GLCoreBlockProcessing(rotated,output, new GLFormat(GLFormat.DataType.UNSIGNED_8,4));
        glint = new GLInterface(glproc);
        stackFrame = inBuffer;
        glint.parameters = parameters;
        add(new Bayer2Float(0,"Bayer2Float"));
        add(new ExposureFusionBayer("FusionBayer"));
        if(!IsoExpoSelector.HDR) {
            if (PhotonCamera.getSettings().cfaPattern != 4) {
                //if (PhotonCamera.getSettings().selectedMode != CameraMode.NIGHT) {
                    add(new Demosaic());
                //} else {
                //    add(new BinnedDemosaic());
                //}
                //add(new Debug3(R.raw.debugraw,"Debug3"));
            } else {
                add(new MonoDemosaic(R.raw.monochrome, "Monochrome"));
            }
        } else {
            add(new LFHDR(0, "LFHDR"));
        }
        /*
         * * * All filters after demosaicing * * *
         */
        //add(new AEC("AEC"));
        //add(new ExposureFusionFast2("ExposureFusion"));
        if(PhotonCamera.getSettings().hdrxNR) {
            add(new SmartNR("SmartNR"));
        }
        //add(new GlobalToneMapping(0,"GlobalTonemap"));
        add(new Initial(R.raw.initial,"Initial"));
        add(new AWB(0,"AWB"));
        add(new Equalization(0,"Equalization"));

        //if(PhotonCamera.getParameters().focalLength <= 3.0)
        //add(new LensCorrection());
        add(new Sharpen(R.raw.sharpeningbilateral,"Sharpening"));
        add(new RotateWatermark(getRotation()));
        //add(new ShadowTexturing(R.raw.shadowtexturing,"Shadow Texturing"));
        return runAll();
    }
}
