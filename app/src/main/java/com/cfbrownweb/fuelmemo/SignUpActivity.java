/*Author: Chris Brown
* Date: 26/02/2016
* Description: Sign up class. Shows sign up screen and performs validation
* on the input. Lastly it performs the sign up process*/
package com.cfbrownweb.fuelmemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "cfbrownweb";

    private final String signUpUrl = "http://cfbrownweb.ngrok.io/fuel/signup.php";
    private final String userExistsUrl = "http://cfbrownweb.ngrok.io/fuel/userExists.php";

    private EditText userInput;
    private EditText passInput;
    private EditText passRepeatInput;

    private String user;
    private String pass;
    private String passRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userInput = (EditText) findViewById(R.id.sign_up_username_input);
        passInput = (EditText) findViewById(R.id.sign_up_password_input);
        passRepeatInput = (EditText) findViewById(R.id.sign_up_password_repeat_input);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void signUpReq() {
        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, signUpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")){
                            //success
                            //set user in config
                            Configuration.getConfig().setUser(new User(user));

                            //Store in shared prefs
                            SharedPreferences settings = Configuration.getConfig().getSharedPrefs(SignUpActivity.this);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("user", user);
                            editor.apply();

                            //Navigate to vehicles page
                            Intent intent = new Intent(SignUpActivity.this, ConnectionActivity.class);
                            intent.putExtra("dest", VehiclesActivity.class);
                            startActivity(intent);

                            //End sign in activity
                            finish();
                        }
                        else {
                            //Something went wrong
                            Utils.serverErrorToast(SignUpActivity.this);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null){
                    //Network error
                    Utils.netErrorToast(SignUpActivity.this);
                }
                else {
                    //A different error
                    Utils.serverErrorToast(SignUpActivity.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user",user);
                params.put("pass",pass);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void userExistsReq() {
        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, userExistsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("0")){
                            //Exists - Show error
                            TextView userExists = (TextView) findViewById(R.id.sign_up_user_exists);
                            userExists.setVisibility(View.VISIBLE);
                        }
                        else if(response.equals("1")){
                            //success - user doesn't exist
                            signUpReq();
                        }
                        else {
                            //Something went wrong
                            Utils.serverErrorToast(SignUpActivity.this);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null){
                    //Network error
                    Utils.netErrorToast(SignUpActivity.this);
                }
                else {
                    //A different error
                    Utils.serverErrorToast(SignUpActivity.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user",user);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void signUpAttempt(View view){
        //Get all the inputs
        user = userInput.getText().toString();
        pass = passInput.getText().toString();
        passRepeat = passRepeatInput.getText().toString();

        if(validateInput()){
            //check if username exists
            userExistsReq();
        }
    }

    private boolean validateInput(){
        if(user.isEmpty()){
            Toast.makeText(this, getString(R.string.sign_up_empty_user), Toast.LENGTH_LONG).show();
            return false;
        }
        else if(user.length() < 5){
            Toast.makeText(this, getString(R.string.sign_up_min_user), Toast.LENGTH_LONG).show();
            return false;
        }
        else if(pass.isEmpty()){
            Toast.makeText(this, getString(R.string.sign_up_empty_pass), Toast.LENGTH_LONG).show();
            return false;
        }
        else if(pass.length() < 5){
            Toast.makeText(this, getString(R.string.sign_up_min_pass), Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!pass.equals(passRepeat)){
            //Show password mismatch error
            TextView passMismatch = (TextView) findViewById(R.id.sign_up_password_mismatch);
            passMismatch.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            return true;
        }
    }

}
