package wrteam.ekart.dboy.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import wrteam.ekart.dboy.R;
import wrteam.ekart.dboy.helper.ApiConfig;
import wrteam.ekart.dboy.helper.AppController;
import wrteam.ekart.dboy.helper.Constant;
import wrteam.ekart.dboy.helper.Session;
import wrteam.ekart.dboy.helper.Utils;
import wrteam.ekart.dboy.helper.VolleyCallback;

public class LoginActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText edtLoginPassword, edtLoginMobile;
    Button btnLogin;
    LinearLayout lytlogin;
    Session session;
    Activity activity;
    TextView tvPrivacy;

    ////Firebase
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        activity = LoginActivity.this;

        session = new Session(activity);

        btnLogin = findViewById(R.id.btnlogin);

        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        edtLoginMobile = findViewById(R.id.edtLoginMobile);
        tvPrivacy = findViewById(R.id.tvPrivacy);

        //layouts
        lytlogin = findViewById(R.id.lytlogin);

        edtLoginMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0);

        edtLoginPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);
        Utils.setHideShowPassword(edtLoginPassword);

        edtLoginPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_show, 0);

        Utils.setHideShowPassword(edtLoginPassword);

        //setAppLocal("replace_your_language_code_here");

//        Constant.country_code = "your_country_code";
//        edtFCode.setText(Constant.country_code);zz

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                AppController.getInstance().setDeviceToken(token);
            }
        });

        PrivacyPolicy();
    }

//    public void setAppLocal(String languageCode){
//        Resources resources = getResources ();
//        DisplayMetrics dm = resources.getDisplayMetrics ();
//        Configuration configuration = resources.getConfiguration ();
//        configuration.setLocale (new Locale(languageCode.toLowerCase ()));
//        resources.updateConfiguration (configuration,dm);
//    }


    public void OnBtnClick(View view) {
        if (AppController.isConnected(activity)) {

            String mobile = edtLoginMobile.getText().toString();
            String password = edtLoginPassword.getText().toString();

            if (ApiConfig.CheckValidation(mobile, false, false)) {
                edtLoginMobile.setError(getString(R.string.enter_mobile_number));
            } else if (ApiConfig.CheckValidation(mobile, false, true)) {
                edtLoginMobile.setError(getString(R.string.enter_valid_mobile_number));

            } else if (ApiConfig.CheckValidation(password, false, false)) {
                edtLoginPassword.setError(getString(R.string.password_required));
            } else if (AppController.isConnected(activity)) {

                ApiConfig.disableButton(activity, btnLogin);

                Map<String, String> params = new HashMap<String, String>();
                params.put(Constant.EMAIL, mobile);
                params.put(Constant.PASSWORD, password);
                params.put(Constant.LOGIN, Constant.GetVal);
                params.put(Constant.FCM_ID, "" + AppController.getInstance().getDeviceToken());
//                        System.out.println ("=============>>FCM_ID" + AppController.getInstance ().getDeviceToken ());
                ApiConfig.RequestToVolley(new VolleyCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(boolean result, String response) {
                        if (result) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean(Constant.SUCCESS)) {
                                    StartMainActivity(jsonObject.getJSONObject(Constant.DATA));
                                } else {
                                    setSnackBar(activity, jsonObject.getString(Constant.MESSAGE), getString(R.string.ok), Color.RED);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, activity, Constant.LOGIN_DELIVERY_BOY_URL, params, true);

            }
        } else {
            setSnackBar(activity, getString(R.string.no_internet_message), getString(R.string.retry), Color.RED);
        }
    }

    public void StartMainActivity(JSONObject jsonObject) {
        try {
            new Session(activity).createUserLoginSession(
                    AppController.getInstance().getDeviceToken(),
                    jsonObject.getString(Constant.ID),
                    jsonObject.getString(Constant.NAME),
                    jsonObject.getString(Constant.EMAIL),
                    jsonObject.getString(Constant.PHONE),
                    jsonObject.getString(Constant.BALANCE),
                    jsonObject.getString(Constant.STATUS)
            );

            session.setData(Constant.BALANCE, Constant.formatter.format(Double.parseDouble(jsonObject.getString(Constant.BALANCE))));
            session.setData(Constant.ID, jsonObject.getString(Constant.ID));

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSnackBar(final Activity activity, String message, String action, int color) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
                startActivity(new Intent(activity, LoginActivity.class).putExtra(Constant.FROM, "login"));
            }
        });
        snackbar.setActionTextColor(color);
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public void PrivacyPolicy() {
        tvPrivacy.setClickable(true);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        String message = getString(R.string.msg_privacy_terms);
        String s2 = getString(R.string.terms_conditions);
        String s1 = getString(R.string.privacy_policy);

        final Spannable wordtoSpan = new SpannableString(message);

        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent privacy = new Intent(LoginActivity.this, WebViewActivity.class);
                privacy.putExtra("link", Constant.DELIVERY_BOY_POLICY);
                startActivity(privacy);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                ds.isUnderlineText();
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent terms = new Intent(LoginActivity.this, WebViewActivity.class);
                terms.putExtra("link", Constant.DELIVERY_BOY_TERMS);
                startActivity(terms);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                ds.isUnderlineText();
            }
        }, message.indexOf(s2), message.indexOf(s2) + s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacy.setText(wordtoSpan);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}