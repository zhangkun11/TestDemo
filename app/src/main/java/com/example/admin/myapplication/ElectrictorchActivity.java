package com.example.admin.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.jb.electrictorch.ElectrictorchController;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.example.admin.myapplication.meter.MeterActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ElectrictorchActivity extends Activity {

    @InjectView(R.id.next_test)
    Button nextTest;
    private ToggleButton btn;
    private boolean isOn;
    private ElectrictorchController electrictorchController;
    private Camera mCamera;
    private SurfaceView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_light);
        ButterKnife.inject(this);
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
                Parameters torchParameters = mCamera.getParameters();
                List<String> flashModes = torchParameters
                        .getSupportedFlashModes();
                if (flashModes == null)
                    return;
                String flashMode = torchParameters.getFlashMode();
                if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                    if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                        torchParameters
                                .setFlashMode(Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(torchParameters);

                        mCamera.setPreviewDisplay(sv.getHolder());
                        mCamera.startPreview();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        nextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ElectrictorchActivity.this, MeterActivity.class);
                startActivity(intent);
                MyApplication.getSession().set("elec",true);
                finish();
            }
        });


        btn = (ToggleButton) findViewById(R.id.led_light_btn);
        btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                try {
                    if (isChecked) {
                        Parameters torchParameters = mCamera
                                .getParameters();
                        List<String> flashModes = torchParameters
                                .getSupportedFlashModes();
                        if (flashModes == null)
                            return;
                        String flashMode = torchParameters.getFlashMode();
                        if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                            if (flashModes
                                    .contains(Parameters.FLASH_MODE_TORCH)) {
                                torchParameters
                                        .setFlashMode(Parameters.FLASH_MODE_TORCH);
                                mCamera.setParameters(torchParameters);
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                                    @Override
                                    public void onAutoFocus(boolean success,
                                                            Camera camera) {
                                    }
                                });
                                isOn = true;

                            }
                        }

                    } else {
                        Parameters closeParameters = mCamera
                                .getParameters();
                        closeParameters
                                .setFlashMode(Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(closeParameters);
                        isOn = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getSession().set("elec",true);
        isOn = false;
        btn.setChecked(isOn);
        mCamera.release();
    }


}
