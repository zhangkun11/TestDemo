package com.example.admin.myapplication.meter;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by admin on 2017-02-27.
 */

public class MeterTestActivity extends AppCompatActivity {
   /* @InjectView(R.id.meter_info)
    TextView meterInfo;
    @InjectView(R.id.meter_try)
    Button meterTry;
    //红外服务管理
    private ConsumerIrManager consumerIrManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter);
        ButterKnife.inject(this);
        //获取系统红外管理服务
        consumerIrManager= (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);


    }

    @OnClick(R.id.meter_try)
    public void onClick() {
        if(!consumerIrManager.hasIrEmitter()){
            meterInfo.setText("无红外服务...");
            return;
        }else {
            // 一种交替的载波序列模式，通过毫秒测量
            int[] pattern = { 1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625,
                    442, 625, 442, 625, 468, 625, 442, 625, 494, 572, 1614,
                    625, 1588, 625, 1614, 625, 494, 572, 442, 651, 442, 625,
                    442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442,
                    625, 494, 598, 442, 625, 442, 625, 520, 572, 442, 625, 442,
                    625, 442, 651, 1588, 625, 1614, 625, 1588, 625, 1614, 625,
                    1588, 625, 48958 };

            // 在38.4KHz条件下进行模式转换
            consumerIrManager.transmit(38400, pattern);

            getConsumerInfo();
        }
    }
    private void getConsumerInfo(){
        StringBuilder b = new StringBuilder();

        if (!consumerIrManager.hasIrEmitter()) {
            meterInfo.setText("未找到红外发身器！");
            return;
        }

        // 获得可用的载波频率范围
        ConsumerIrManager.CarrierFrequencyRange[] freqs = consumerIrManager
                .getCarrierFrequencies();
        b.append("IR Carrier Frequencies:\n");// 红外载波频率
        // 边里获取频率段
        for (ConsumerIrManager.CarrierFrequencyRange range : freqs) {
            b.append(String.format("    %d - %d\n",
                    range.getMinFrequency(), range.getMaxFrequency()));
        }
        meterInfo.setText(b.toString());// 显示结果
    }*/
}
