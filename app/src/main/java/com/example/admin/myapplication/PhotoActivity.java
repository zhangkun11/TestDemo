package com.example.admin.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by admin on 2017-02-22.
 */

public class PhotoActivity extends AppCompatActivity {
    @InjectView(R.id.take_photo)
    Button takePhoto;
    @InjectView(R.id.photo_get)
    ImageView photoGet;
    @InjectView(R.id.next_test)
    Button nextTest;
    private Uri imageUri;
    private static final int TAKE_PHOTO = 1;
    private PopupWindow popupWindow;
    private File outPutImage;
    private int touchX = 0, touchY = 0;

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            touchX = (int) event.getX();
            touchY = (int) event.getY();
        }
        return false;
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getSession().set("photo",true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);
        photoGet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchX = (int) event.getX();
                    touchY = (int) event.getY();
                    Log.i("info", "onTouch: " + touchX + "<=========>" + touchY);
                    initPopWindow(v);
                }
                return true;
            }
        });
        nextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PhotoActivity.this,ElectrictorchActivity.class);
                startActivity(intent);
                MyApplication.getSession().set("photo",true);
                finish();
            }
        });
        /*photoGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("info", "onClick: "+touchX+"<------->"+touchY);
                initPopWindow();
            }
        });*/
        /*photoGet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    touchY= (int) event.getY();
                    touchX= (int) event.getX();
                    Log.i("info", "onTouch: "+touchX+"<------->"+touchY);

                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    initPopWindow(v);
                }
                return false;
            }
        });*/
    }

    private void initPopWindow(View view) {
        // 获取自定义布局文件activity_popupwindow_left.xml的视图
        View popupWindow_view = LayoutInflater.from(PhotoActivity.this).inflate(R.layout.popwindow, null);
        popupWindow = new PopupWindow(popupWindow_view, 120, 100, true);
        popupWindow.setContentView(popupWindow_view);
        TextView delete = (TextView) popupWindow_view.findViewById(R.id.image_delete);

        // 这里是位置显示方式,在屏幕的左侧
        //popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL , 0, 0);

        /*if(touchY==0&&touchX==0){
            popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL , 0, 0);
        }else{*/
        popupWindow.showAsDropDown(takePhoto, touchX - 60, touchY - 100);


        // 点击其他地方消失
        popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outPutImage != null) {
                    outPutImage.delete();

                    photoGet.setImageResource(R.mipmap.ic_launcher);
                    Toast.makeText(PhotoActivity.this, "图片文件已删除", Toast.LENGTH_SHORT).show();

                }
                popupWindow.dismiss();
            }
        });

    }

    //Uri转file
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @OnClick(R.id.take_photo)
    public void onClick() {
        //创建File对象,存储拍摄的照片
        outPutImage = new File(getExternalCacheDir(), "output_image.jpg");

        try {
            if (outPutImage.exists()) {
                outPutImage.delete();
            }
            outPutImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(PhotoActivity.this, "com.example.admin.myapplication.photo", outPutImage);

        } else {
            imageUri = Uri.fromFile(outPutImage);
        }

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photoGet.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
