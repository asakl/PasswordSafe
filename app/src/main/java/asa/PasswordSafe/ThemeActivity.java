package asa.PasswordSafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ThemeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DBWarpper db = new DBWarpper(this);

        if(Constants.color == 0) {
            setContentView(R.layout.theme_window_dark);
        }
        else {
            setContentView(R.layout.theme_window_light);
        }

        RadioGroup radioGroup = findViewById(R.id.themes);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.darkButton:
                        Constants.color = 0;
                        db.changeColor(0);
                        recreate();
                        break;
                    case R.id.lightButton:
                        Constants.color = 1;
                        db.changeColor(1);
                        recreate();
                        break;
                    case R.id.customButton:
                        //Constants.color = 1;
                        Toast.makeText(getBaseContext(), "not ready yet", Toast.LENGTH_LONG).show();
                        break;
                        default:
                            Toast.makeText(getBaseContext(), "we have some error...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.backMenu) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
