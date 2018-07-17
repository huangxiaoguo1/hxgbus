package tsou.cn.hxgbus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import tsou.cn.hxgbus.bean.Contant;
import tsou.cn.lib_hxgbus.HxgBus;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 返回数据
     */
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn:
                HxgBus.getDefault().post( "111");
//                HxgBus.getDefault().post( Contant.tag, "111");
                finish();
                break;
        }
    }
}
