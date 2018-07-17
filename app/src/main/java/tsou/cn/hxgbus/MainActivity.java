package tsou.cn.hxgbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tsou.cn.hxgbus.bean.Contant;
import tsou.cn.lib_hxgbus.HxgBus;
import tsou.cn.lib_hxgbus.Subscriber;
import tsou.cn.lib_hxgbus.ThreadMode;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 跳转到TestActivity
     */
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HxgBus.getDefault().register(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        HxgBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView() {
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
    }

    /**
     * 主线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.MAIN, priority = 100)
    public void ShowName(String name) {
        Log.e("huangxiaoguo", name + "===100");
    }

    /**
     * 异步线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.ASYNC, priority = 500)
    public void ShowName2(String name) {
        Log.e("huangxiaoguo", name + "===500");
    }

    /**
     * 子线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.BACKGROUND, priority = 600)
    public void ShowName3(String name) {
        Log.e("huangxiaoguo", name + "===600");
    }

    /**
     * 相同线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.POSTING, priority = 700)
    public void ShowName4(String name) {
        Log.e("huangxiaoguo", name + "===700");
    }

    /**
     * 主线程，带有tag标记
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.MAIN, tag = Contant.tag, priority = 100)
    public void ShowNameTag(String name) {
        Log.e("huangxiaoguo", name + "---带有tag===100");
    }

    /**
     * 主线程
     *
     * @param name
     */
    @Subscriber(threadMode = ThreadMode.MAIN)
    public void Show(String name) {
        Log.e("huangxiaoguo", name + "---只是带有线程");
    }

    /**
     * 什么都没带
     * @param name
     */
    @Subscriber()
    public void ShowNameNo(String name) {
        Log.e("huangxiaoguo", name + "---什么都没带");
    }
    /**
     * 带有tag标记
     *
     * @param name
     */
    @Subscriber(tag = Contant.tag)
    public void ShowNameTag1(String name) {
        Log.e("huangxiaoguo", name + "---只带有tag");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn:
                startActivity(new Intent(this, TestActivity.class));
                break;
        }
    }
}
