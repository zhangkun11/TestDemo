package com.example.admin.myapplication;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.jb.electrictorch.ElectrictorchController;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import java.util.List;

public class ElectrictorchActivity extends Activity {

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
		try {
			if (mCamera == null) {
				mCamera = Camera.open();
				Camera.Parameters torchParameters = mCamera.getParameters();
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


		btn = (ToggleButton) findViewById(R.id.led_light_btn);
		btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				try {
					if (isChecked) {
						Camera.Parameters torchParameters = mCamera
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
						Camera.Parameters closeParameters = mCamera
								.getParameters();
						closeParameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
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
		isOn = false;
		btn.setChecked(isOn);
		mCamera.release();
	}


}
