package com.hgsoft.testtraffic;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.btn_start)
    Button mBtnStart;
    @InjectView(R.id.btn_end)
    Button mBtnEnd;
    @InjectView(R.id.tv_start)
    TextView mTvStart;
    @InjectView(R.id.tv_end)
    TextView mTvEnd;
    @InjectView(R.id.tv_display)
    TextView mTvDisplay;
    @InjectView(R.id.activity_main)
    LinearLayout mActivityMain;
    @InjectView(R.id.sp_package_name)
    Spinner mSpPackageName;

    private long trafficTxStart;
    private long trafficRxStart;
    private long trafficTxEnd;
    private long trafficRxEnd;

    CmdHelper mCmdHelper;
    private List<PackageInfo> mInstalledPackages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initView();

        mBtnStart.setEnabled(true);
        mBtnEnd.setEnabled(false);

        mCmdHelper = new CmdHelper();
    }

    private void initView() {
        String[] mItems = getPackageNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPackageName.setAdapter(adapter);
        mSpPackageName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBtnStart.setEnabled(true);
                mBtnEnd.setEnabled(false);
                mTvStart.setText("开始流量");
                mTvEnd.setText("结束流量");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick(R.id.btn_start)
    public void btnStartOnclick() {
        int uid = getPackageUid(getMyPackageName());
        if (uid == -1) {
            Toast.makeText(this, "包名不存在...", Toast.LENGTH_SHORT).show();
            return;
        }
        trafficTxStart = getTxTraffic(uid);
        trafficRxStart = getRxTraffic(uid);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        mTvStart.setText(sdf.format(new Date()) + "\n开始下行流量:" + Formatter.formatFileSize(this, trafficTxStart) + "\n开始上行流量:" + Formatter.formatFileSize(this, trafficRxStart));
        mBtnStart.setEnabled(false);
        mBtnEnd.setEnabled(true);
    }

    @OnClick(R.id.btn_end)
    public void btnEndOnclick() {
        int uid = getPackageUid(getMyPackageName());
        if (uid == -1) {
            Toast.makeText(this, "包名不存在...", Toast.LENGTH_SHORT).show();
            return;
        }
        trafficTxEnd = getTxTraffic(uid);
        trafficRxEnd = getRxTraffic(uid);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        mTvEnd.setText(sdf.format(new Date()) + "\n结束下行流量:" + Formatter.formatFileSize(this, trafficTxEnd) + "\n结束上行流量:" + Formatter.formatFileSize(this, trafficRxEnd));

        mTvDisplay.append(sdf.format(new Date()) + " 消耗" + Formatter.formatFileSize(this, trafficRxEnd + trafficTxEnd - trafficRxStart - trafficTxStart) + " 流量\n" +
                ("(下行:" + Formatter.formatFileSize(this, trafficTxEnd - trafficTxStart)) + ",上行:" + Formatter.formatFileSize(this, trafficRxEnd - trafficRxStart) + ")\n");

        mBtnStart.setEnabled(true);
        mBtnEnd.setEnabled(false);
    }

    /**
     * 获取应用的包名
     *
     * @return
     */
    private String getMyPackageName() {
        return mInstalledPackages.get(mSpPackageName.getSelectedItemPosition()).packageName;
    }

    /**
     * 获得应用某个时刻的下行网络流量
     *
     * @param uid
     * @return
     */
    private long getTxTraffic(int uid) {
        String result = mCmdHelper.run("cat /proc/uid_stat/" + uid + "/tcp_rcv");
        if (result.equals("")) {
            return 0;
        }
        long tx = Long.parseLong(result);
        System.out.println("tx=" + tx);
        return tx;
    }

    /**
     * 获得应用某个时刻的上行网络流量
     *
     * @param uid
     * @return
     */
    private long getRxTraffic(int uid) {
        String result = mCmdHelper.run("cat /proc/uid_stat/" + uid + "/tcp_snd");
        if (result.equals("")) {
            return 0;
        }
        long rx = Long.parseLong(result);
        System.out.println("rx=" + rx);
        return rx;
    }

    /**
     * 获取包的UID
     *
     * @param packageName
     * @return
     */
    private int getPackageUid(String packageName) {
        try {
            PackageManager pm = getPackageManager();

            ApplicationInfo ai = pm.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES);
            System.out.println("ai.uid=" + ai.uid);
            return ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 获取所有包名
     * @return
     */
    private String[] getPackageNames() {
        PackageManager pm = getPackageManager();

        mInstalledPackages = pm.getInstalledPackages(PackageManager.GET_PROVIDERS);
        Collections.sort(mInstalledPackages, new PackageNameComparator(pm));
        String[] result = new String[mInstalledPackages.size()];
        for (int i = 0; i < mInstalledPackages.size(); i++) {
            result[i] = mInstalledPackages.get(i).applicationInfo.loadLabel(pm).toString();
        }
        return result;
    }
}
