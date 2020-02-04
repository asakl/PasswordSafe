package asa.PasswordSafe;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    // define variables
    DBWarpper db;                                   // database
    TreeMap<String, Pair<String, String>> info;     // passwords list
    String key;                                     // current password
    private long backPressedTime;
    private Toast exitToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Constants.color == 0) {
            setContentView(R.layout.main_window_dark);
        }
        else {
            setContentView(R.layout.main_window_light);
        }

        // get the password box and database
        db = new DBWarpper(this);

        showInfo();
        // init the search box
        EditText filter = findViewById(R.id.searchOnMain);
        key = Constants.key;
        Constants.key = "";

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // get all of the database that the site name like what in the search box
                showInfo(db.selectPart(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            exitToast.cancel();
            MyExitButtonOnClick(null);
        }
        else{
            exitToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            exitToast.show();
        }
        backPressedTime = System.currentTimeMillis();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mainMenuSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void MyExitButtonOnClick(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }



    private void showInfo(){
        // refresh the info list in UI
        showInfo(db.getAllDataMap());
    }

    private void showInfo(TreeMap<String, Pair<String, String>> m){
        info = m; // get the info list

        // init the list
        ListView listView = findViewById(R.id.infoListOnMain);
        List<HashMap<String, String>> list = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item_on_list, new String[]{Constants.firstLine}, new int[]{R.id.text1});

        for (Map.Entry<String, Pair<String, String>> stringPairEntry : info.entrySet()) {
            HashMap<String, String> resultsMap = new HashMap<>();

            resultsMap.put(Constants.firstLine, (stringPairEntry).getKey());

            list.add(resultsMap);
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListOnClick(parent, view, position, id);
            }

        });

    }

    public void AddButtonOnClick(View v){
        View dialog;
        if(Constants.color == 0) {
            dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup_dark, null);
        }
        else {
            dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup_light, null);
        }
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();

        mainDialog.setView(dialog);
        mainDialog.show();


        dialog.findViewById(R.id.okButtonOnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String site = ((EditText)mainDialog.findViewById(R.id.siteNameOnAdd)).getText().toString();
                String name = ((EditText)mainDialog.findViewById(R.id.usernameOnAdd)).getText().toString();
                String pass = ((EditText)mainDialog.findViewById(R.id.passOnAdd)).getText().toString();

                if(site.equals(Constants.thisApp)){
                    Toast.makeText(MainActivity.this, Constants.addNameError, new Integer(0)).show();
                }
                else{
                    long in = db.insertData(site, name, AES.encrypt(pass, key));
                    if(in == 0){
                        showInfo();
                    }
                    else if (in == 1){
                        Toast.makeText(MainActivity.this, "The site is already exist", Toast.LENGTH_LONG).show();
                    }
                }
                mainDialog.dismiss();
            }
        });
    }

    public void ListOnClick(AdapterView<?> adapter, View v, int position, long id){
        View dialog;
        if(Constants.color == 0) {
            dialog = LayoutInflater.from(this).inflate(R.layout.popup_list_on_click_dark, null);
        }
        else {
            dialog = LayoutInflater.from(this).inflate(R.layout.popup_list_on_click_light, null);
        }
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();
        mainDialog.setView(dialog);
        mainDialog.show();


        Map.Entry pair = null;
        Iterator it = info.entrySet().iterator();
        for(int i = 0 ; i <= position ; i++) {
            pair = (Map.Entry)it.next();
        }

        final Map.Entry<String, Pair<String, String>> res = pair;

        ((TextView)dialog.findViewById(R.id.nameOnEdit)).setText((res.getValue()).first);
        ((TextView)dialog.findViewById(R.id.passOnEdit)).setText(AES.decrypt((res.getValue()).second, key));

        if(res.getKey().equals(Constants.thisApp)){
            dialog.findViewById(R.id.deleteButtonOnEdit).setVisibility(View.INVISIBLE);
        }
        else {
            dialog.findViewById(R.id.deleteButtonOnEdit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.deleteData(res.getKey());
                    showInfo();
                    mainDialog.dismiss();
                }
            });
        }

        dialog.findViewById(R.id.editButtonOnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit(view, res.getKey(), (res.getValue()).first, (res.getValue()).second);
                mainDialog.dismiss();
            }
        });

    }

    public void edit(View v,String site, String name, String pass){
        View dialog;
        if(Constants.color == 0) {
            dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup_dark, null);
        }
        else {
            dialog = LayoutInflater.from(this).inflate(R.layout.add_item_popup_light, null);
        }
        final AlertDialog mainDialog = new AlertDialog.Builder(this).create();

        if(site.equals(Constants.thisApp))
        {
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setText(site);
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setEnabled(false);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setText(name);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setEnabled(false);
            ((EditText) dialog.findViewById(R.id.passOnAdd)).setText(AES.decrypt(pass, key));
        }
        else {
            ((EditText) dialog.findViewById(R.id.siteNameOnAdd)).setText(site);
            ((EditText) dialog.findViewById(R.id.usernameOnAdd)).setText(name);
            ((EditText) dialog.findViewById(R.id.passOnAdd)).setText(AES.decrypt(pass, key));
        }
        mainDialog.setView(dialog);
        mainDialog.show();

        final String str = site;

        dialog.findViewById(R.id.okButtonOnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((EditText)mainDialog.findViewById(R.id.usernameOnAdd)).getText().toString();
                String pass = ((EditText)mainDialog.findViewById(R.id.passOnAdd)).getText().toString();
                String site = ((EditText)mainDialog.findViewById(R.id.siteNameOnAdd)).getText().toString();

                if(str.equals(Constants.thisApp)) {
                    db.changePassword(key, pass);
                    key = pass;
                    showInfo();
                }
                else if(site.equals(Constants.thisApp)){
                    Toast.makeText(MainActivity.this, Constants.editNameError, new Integer(0)).show();
                }
                else{
                    db.deleteData(str);
                    long in = db.insertData(site, name, AES.encrypt(pass, key));
                    if(in == 1){
                        showInfo();
                    }
                }
                mainDialog.dismiss();
            }
        });
    }
}
