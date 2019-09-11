package ognora.drishti;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceHolder holder;


    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.camera = camera ;
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        Camera.Parameters params = camera.getParameters();

        //Auto focus
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //setting max resolution
        List<Camera.Size> size = params.getSupportedPictureSizes();
        Camera.Size msize = null;

        for(Camera.Size size1 : size)
        {
            msize = size1;
        }

        //change the orientation of camera

        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            params.setRotation(90);
        }
        else
        {
            params.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }


        params.setPictureSize(msize.width, msize.height);
        camera.setParameters(params);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();

    }
}
