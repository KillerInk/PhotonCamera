package com.eszdman.photoncamera.processing.opengl.scripts;

import android.graphics.Point;

import com.eszdman.photoncamera.app.PhotonCamera;
import com.eszdman.photoncamera.processing.opengl.GLCoreBlockProcessing;
import com.eszdman.photoncamera.processing.opengl.GLFormat;
import com.eszdman.photoncamera.processing.opengl.GLOneScript;
import com.eszdman.photoncamera.processing.opengl.GLProg;
import com.eszdman.photoncamera.processing.opengl.GLTexture;
import com.eszdman.photoncamera.processing.opengl.GLUtils;
import com.eszdman.photoncamera.processing.render.Parameters;

import static com.eszdman.photoncamera.processing.ImageProcessing.unlimitedCounter;

public class AverageRaw extends GLOneScript {
    public AverageRaw(Point size, int rid, String name) {
        super(size, new GLCoreBlockProcessing(size,new GLFormat(GLFormat.DataType.UNSIGNED_16)), rid, name);
    }
    @Override
    public void StartScript() {
        AverageParams scriptParams = (AverageParams)additionalParams;
        GLProg glProg = glOne.glProgram;
        GLTexture input1 = new GLTexture(size,new GLFormat(GLFormat.DataType.UNSIGNED_16),scriptParams.inp1);
        glProg.setTexture("InputBuffer",input1);
        glProg.setTexture("InputBuffer2",new GLTexture(size,new GLFormat(GLFormat.DataType.UNSIGNED_16),scriptParams.inp2));
        glProg.setVar("blacklevel", PhotonCamera.getParameters().blackLevel);
        glProg.setVar("unlimitedcount",Math.min(unlimitedCounter,25));
    }
}