package com.jhsc.testwebservice;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Android平台调用WebService（手机号码归属地查询）
 *
 * @author chao
 * @date 2015-10-25
 */
public class MainActivity extends AppCompatActivity {

    private EditText phoneSecEditText;
    private TextView resultView;
    private Button queryButton;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneSecEditText = (EditText) findViewById(R.id.phone_sec);
        resultView = (TextView) findViewById(R.id.result_text);
        queryButton = (Button) findViewById(R.id.query_btn);

        queryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 手机号码（段）
                String phoneSec = phoneSecEditText.getText().toString().trim();
                // 简单判断用户输入的手机号码（段）是否合法
                if ("".equals(phoneSec) || phoneSec.length() < 7) {
                    // 给出错误提示
                    phoneSecEditText.setError("您输入的手机号码（段）有误！");
                    phoneSecEditText.requestFocus();
                    // 将显示查询结果的TextView清空
                    resultView.setText("");
                    return;
                }

                //启动后台异步线程进行连接webService操作，并且根据返回结果在主线程中改变UI
                QueryAddressTask queryAddressTask = new QueryAddressTask();
                //启动后台任务
                queryAddressTask.execute(phoneSec);

            }
        });
    }

    /**
     * 手机号段归属地查询
     *
     * @param phoneSec 手机号段
     */
    public String getRemoteInfo(String phoneSec) throws Exception{
        String WSDL_URI = "http://webservice.webxml.com.cn/WebServices/MobileCodeWS.asmx?WSDL";//wsdl 的uri
        String namespace = "http://WebXml.com.cn/";//namespace
        String methodName = "getMobileCodeInfo";//要调用的方法名称

        SoapObject request = new SoapObject(namespace, methodName);
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        request.addProperty("mobileCode", phoneSec);
        request.addProperty("userId", "");

        //创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER12);
        envelope.bodyOut = request;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = true;//由于是.net开发的webservice，所以这里要设置为true

        HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI);
        httpTransportSE.call(null, envelope);//调用

        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        // 获取返回的结果
        result = object.getProperty(0).toString();
        Log.d("debug",result);
        return result;

    }

    class QueryAddressTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            // 查询手机号码（段）信息*/
            try {
                result = getRemoteInfo(params[0]);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //将结果返回给onPostExecute方法
            return result;
        }

        @Override
        //此方法可以在主线程改变UI
        protected void onPostExecute(String result) {
            // 将WebService返回的结果显示在TextView中
            resultView.setText(result);
        }
    }
}

