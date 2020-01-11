package asa.PasswordSafe;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class LoginActivity extends AppCompatActivity {

    private DBWarpper db;
    private EditText Password;
    private long backPressedTime;
    private Toast exitToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_window);
        Password = findViewById(R.id.passInputOnLogin);
        db = new DBWarpper(this);
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            exitToast.cancel();
            this.finishAffinity();
        }
        else{
            exitToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            exitToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    public void LoginButtonOnClick(View v){
        if (checkPassword()) {
            findViewById(R.id.errorMsgOnLogin).setVisibility(View.INVISIBLE);
            Constants.key = Password.getText().toString();
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
        else {
            findViewById(R.id.errorMsgOnLogin).setVisibility(View.VISIBLE);
        }
    }

    private boolean checkPassword() {
        String pass = Password.getText().toString(); // get password

        Cursor res = db.getData(Constants.thisApp); // get all database

        if (res.getCount() == 0){ // empty database?
            // add password
            db.insertData(Constants.thisApp, Constants.none, AES.encrypt(pass, pass));
            return true;
        }
        else{ // not empty database
            res.moveToNext(); // get password in database
            return pass.equals(AES.decrypt(res.getString(3), pass)); // same password?
        }
    }
}
