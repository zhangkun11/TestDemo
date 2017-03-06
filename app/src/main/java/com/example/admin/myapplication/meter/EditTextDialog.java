package com.example.admin.myapplication.meter;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class EditTextDialog extends Dialog {
	private EditText et;
	private Button btn1, btn2;
	private TextView tv;
	private ClickListener mL;

	public interface ClickListener {
		public boolean onBtn1Click(View v, String etStr);

		public boolean onBtn2Click(View v, String etStr);
	}

	public EditTextDialog(Context context, String noticeTv, String btn1Str,
						  String btn2Str, ClickListener l) {
		super(context);
		// TODO Auto-generated constructor stub
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		setCancelable(false);
		this.mL = l;
		LinearLayout contentPad = new LinearLayout(context);
		contentPad.setPadding(5, 5, 5, 5);
		contentPad.setOrientation(LinearLayout.VERTICAL);
		et = new EditText(context);
		tv = new TextView(context);
		tv.setText(noticeTv);
		LayoutParams lp = new LayoutParams(wm.getDefaultDisplay().getWidth(),
				LayoutParams.WRAP_CONTENT);

		contentPad.addView(tv);
		contentPad.addView(et, lp);
		LinearLayout btnPad = new LinearLayout(context);
		btnPad.setOrientation(LinearLayout.HORIZONTAL);
		btn1 = new Button(context);
		btn1.setText(btn1Str);
		btn1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mL != null) {
					mL.onBtn1Click(v, et.getText().toString());
				}
			}
		});
		btn2 = new Button(context);
		btn2.setText(btn2Str);
		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mL != null) {
					mL.onBtn2Click(v, et.getText().toString());
				}
			}
		});
		lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		btnPad.addView(btn1, lp);
		btnPad.addView(btn2, lp);
		lp.setMargins(0, 10, 0, 5);
		contentPad.addView(btnPad, lp);
		setContentView(contentPad);

	}
}
