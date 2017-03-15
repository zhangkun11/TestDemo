package com.example.admin.myapplication.simpleIc;

import android.app.Activity;
import android.content.Intent;
import android.jb.simpleic.SimpleIcController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.scan.ScanActivity;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class SimpleIcActivity extends Activity implements OnClickListener {

    private static final byte[] ReadIcCardId = new byte[]{(byte) 0x00,
            (byte) 0xB0, (byte) 0x99, (byte) 0x01, (byte) 0x08,};// 读取 8字节芯片序列号
    private static final byte[] ReadIcCardState_1 = new byte[]{(byte) 0x00,
            (byte) 0xB0, (byte) 0x92, (byte) 0x00, (byte) 0x01,};// 读取 1字节芯片状态
    private static final byte[] ReadIcCardState_2 = new byte[]{(byte) 0x00,
            (byte) 0xB0, (byte) 0x9A, (byte) 0x00, (byte) 0x04,};// 读取4字节剩余红外认证次数
    private static final byte[] createRandomCom2 = new byte[]{(byte) 0x00,
            (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x04,};// 产生随机数指令
    @InjectView(R.id.next_test)
    Button nextTest;

    private TextView tv_show;
    boolean bind = false;
    boolean isReset = false;
    private SimpleIcController pSamCon;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000:
                    String text = (String) msg.obj;
                    tv_show.setText(text);
                    break;

                case 2000:

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simpleic);
        ButterKnife.inject(this);
        tv_show = (TextView) findViewById(R.id.textView1);
        Button btn0 = (Button) findViewById(R.id.button0);
        /*Button btn1 = (Button) findViewById(R.id.button1);
		Button btn2 = (Button) findViewById(R.id.button2);
		Button btn3 = (Button) findViewById(R.id.button3);*/
        Button btn4 = (Button) findViewById(R.id.button4);

        btn0.setOnClickListener(this);
		/*btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);*/
        btn4.setOnClickListener(this);

        nextTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SimpleIcActivity.this, ScanActivity.class);
                startActivity(intent);
                MyApplication.getSession().set("esam",true);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        pSamCon = SimpleIcController.getInstance();

        try {
            pSamCon.Simpleic_Open(this);
            //pSamCon.Simpleic_Reset();

        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (pSamCon != null)
            try {
                pSamCon.Simpleic_Close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        MyApplication.getSession().set("esam",true);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button0:
                isReset = true;
                tv_show.setText("");
                if (pSamCon != null) {
                    try {
                        if (pSamCon.Simpleic_Reset()) {
                            if (tv_show != null)
                                tv_show.setText(getString(R.string.reset_success));
                        } else {
                            if (tv_show != null)
                                tv_show.setText(getString(R.string.reset_failed));
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        tv_show.setText("error:" + e.getMessage());
                    }
                }
                break;

		/*case R.id.button1:
			isReset = false;
			tv_show.setText("");
			if (pSamCon != null) {
				try {
					byte[] result = pSamCon.Simpleic_Write(ReadIcCardId);
					if (tv_show != null) {
						tv_show.setText(bytesToHexString(result));
						System.out
								.println("result:" + bytesToHexString(result));
					} else {
						tv_show.setText(getString(R.string.psam_operator_failed));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					tv_show.setText("error:" + e1.getMessage());
				}
			}
			break;

		case R.id.button2:
			isReset = false;
			tv_show.setText("");
			if (pSamCon != null) {
				try {
					byte[] result = pSamCon
							.Simpleic_Write(ReadIcCardState_1);
					if (tv_show != null) {
						tv_show.setText(bytesToHexString(result));
						System.out
								.println("result:" + bytesToHexString(result));
					} else {
						tv_show.setText(getString(R.string.psam_operator_failed));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					tv_show.setText("error:" + e1.getMessage());
				}
			}
			break;

		case R.id.button3:
			isReset = false;
			tv_show.setText("");
			if (pSamCon != null) {
				try {
					byte[] result = pSamCon
							.Simpleic_Write(ReadIcCardState_2);
					if (tv_show != null) {
						tv_show.setText(bytesToHexString(result));
						System.out
								.println("result:" + bytesToHexString(result));
					} else {
						tv_show.setText(getString(R.string.psam_operator_failed));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					tv_show.setText("error:" + e1.getMessage());
				}
			}
			break;*/

            case R.id.button4:
                isReset = false;
                tv_show.setText("");
                if (pSamCon != null) {
                    try {
                        byte[] result = pSamCon.Simpleic_Write(createRandomCom2);
                        if (tv_show != null) {
                            tv_show.setText(bytesToHexString(result));
                            System.out
                                    .println("result:" + bytesToHexString(result));
                        } else {
                            tv_show.setText(getString(R.string.psam_operator_failed));
                        }
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        tv_show.setText("error:" + e1.getMessage());
                    }
                }
                break;
            default:
                break;
        }
    }

    private String parseRandom(byte[] result) {
        // TODO Auto-generated method stub
        if (result != null && result.length > 4) {
            try {
                return bytesToHexString(result).substring(6, 14);
            } catch (IndexOutOfBoundsException e) {
                // TODO: handle exception
                e.printStackTrace();
                return getString(R.string.psam_operator_failed);
            }

        } else
            return getString(R.string.psam_operator_failed);
    }

    private byte[] parseHexStr(String cosCommandStr) {
        // TODO Auto-generated method stub

        if (cosCommandStr != null && !cosCommandStr.trim().equals("")) {
            if (cosCommandStr.contains(",")) {
                String[] commandArr = cosCommandStr.split(",");
                byte[] command = new byte[commandArr.length];
                for (int i = 0; i < commandArr.length; i++) {
                    command[i] = hexStrToByte(commandArr[i]);
                }
                return command;
            }
        }
        return null;
    }

    private byte hexStrToByte(String string) {
        // TODO Auto-generated method stub
        int value = 0;
        if (string != null) {
            switch (string.length()) {
                case 1:
                    value = getValueByHexChar(string.charAt(0));
                    break;
                case 2:
                    value = getValueByHexChar(string.charAt(1));
                    value += getValueByHexChar(string.charAt(0)) * 16;
                    break;
                default:
                    break;
            }
        }
        return (byte) (value & 0xFF);
    }

    private int getValueByHexChar(char charAt) {
        // TODO Auto-generated method stub
        int i = 0;
        if (charAt >= '0' && charAt <= '9') {
            i = charAt - '0';
        } else if (charAt >= 'A' && charAt <= 'F') {
            i = charAt - 'A' + 10;
        } else if (charAt >= 'a' && charAt <= 'f') {
            i = charAt - 'a' + 10;
        }
        return i;
    }

    // 字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
