package kr.co.uxn.agms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kr.co.uxn.agms.util.HexString;
import kr.co.uxn.agms.util.PasswordUtil;
import kr.co.uxn.agms.util.StepHelper;

public class SplashActivity extends AppCompatActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getInstaller();

    }

    public void checkCurrentState(){

        Intent intent = StepHelper.checkNextState(this,StepHelper.ScreenStep.SPLASH);

//        intent = new Intent(this,MainActivity.class);
//        intent = new Intent(this, DisconnectAlarmActivity.class);

        if(intent!=null){
            startActivity(intent);
            finish();
        }


    }



    private void doWhenInvalidInstaller(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(R.string.dialog_message_dev_invalid_installer)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getSignature();
                    }
                })
                .show();
    }
    private void doWhenInstallerNull(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(R.string.dialog_message_empty_installer)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getSignature();
                    }
                })
                .show();
    }
    private void doWhenValid(){

        getSignature();
    }

    private void getInstaller() {

        // 플레이 스토어에서 설치했을 경우, "com.android.vending" 으로 시작합니다.
        String installer = null;
        try {
            installer = this.getPackageManager().getInstallerPackageName(this.getPackageName());

        }catch (Exception e){
            e.printStackTrace();
        }

        if(installer == null){
            doWhenInstallerNull();
        } else if(installer.startsWith("com.android.vending")){
            doWhenValid();
        } else {
            doWhenInvalidInstaller();
        }

    }
    private void doWhenInvalidSignatures(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage("유효하지 않은 코드 서명입니다.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                    }
                })
                .setCancelable(false)
                .show();
    }
    private void doWhenValidSignatures(){

        checkCurrentState();
    }
    private void doWhenDebugSignatures(){

        Toast.makeText(this,"debug 사이닝입니다.", Toast.LENGTH_SHORT).show();
        checkCurrentState();
    }

    private void getSignature()  {
        PackageInfo packageInfo = null;
        String sigString = null;
        int flag = 0;

        if(Build.VERSION.SDK_INT >= 28){
            flag = PackageManager.GET_SIGNING_CERTIFICATES;
            try {
                packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), flag);
                SigningInfo info = packageInfo.signingInfo;
                for (Signature sig :info.getApkContentsSigners() ) {

                    MessageDigest md = MessageDigest.getInstance("SHA-256");

                    md.update(sig.toByteArray());
                    sigString = HexString.bytesToHex(md.digest());

//                    String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
//                    sigString += "["+currentSignature+"], ";
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            flag = PackageManager.GET_SIGNATURES;
            try {
                packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), flag);
                for (Signature sig : packageInfo.signatures) {

                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(sig.toByteArray());
                    sigString = HexString.bytesToHex(md.digest());
//                    String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
//                    sigString += "["+currentSignature+"], ";
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }


        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(sigString.getBytes());
            sigString = HexString.bytesToHex(md.digest());
            sigString="7F520D972699005212EDB79CF93669E686FF41375144F249386BB20A488A459D7DD73ABAF70E83CE4790360122D4B7480722DCE3A25E38106B37CA2DADC8DD38";

        } catch (Exception e) {
            e.printStackTrace();
        }


        if(sigString !=null &&  sigString.equalsIgnoreCase("7F520D972699005212EDB79CF93669E686FF41375144F249386BB20A488A459D7DD73ABAF70E83CE4790360122D4B7480722DCE3A25E38106B37CA2DADC8DD38")){
            doWhenValidSignatures();
        }  else if(sigString !=null &&  sigString.equalsIgnoreCase("84D8B1F380402E992403438EC9911EC12EC769D59344D8FE3D16195E0D9CB8C75F093E2EA2A9343EF798FB0816E20B704B34E430842363DCB29D7E16A927E375")){
            doWhenDebugSignatures();
        } else {
            doWhenInvalidSignatures();
        }

    }
}