package com.example.indegenousmedicine2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class SubscriptionActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private static final String PREFS_NAME = "agro_prefs";
    private static final String KEY_SUBSCRIBED = "is_subscribed";

    // State containers
    private View layoutPlans;
    private View layoutPayment;
    private View layoutSuccess;

    // Plans state
    private Button btnMonthly;
    private Button btnAnnual;
    private TextView tvPrice;
    private TextView tvPriceSub;
    private Button btnContinue;
    private boolean isAnnual = true;

    // Payment state
    private Button tabUpi;
    private Button tabCard;
    private Button tabNetBanking;
    private View panelUpi;
    private View panelCard;
    private View panelNetBanking;
    private Button btnPay;
    private ProgressBar progressPay;

    // Success state
    private ImageView ivSuccessCheck;
    private TextView tvTxnId;
    private TextView tvValidUntil;
    private Button btnStartPro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyLanguage(this);
        setContentView(R.layout.activity_subscription);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.subscription_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        setupPlansState();
        setupPaymentState();
        setupSuccessState();
    }

    private void bindViews() {
        layoutPlans = findViewById(R.id.layoutPlans);
        layoutPayment = findViewById(R.id.layoutPayment);
        layoutSuccess = findViewById(R.id.layoutSuccess);

        btnMonthly = findViewById(R.id.btnMonthly);
        btnAnnual = findViewById(R.id.btnAnnual);
        tvPrice = findViewById(R.id.tvPrice);
        tvPriceSub = findViewById(R.id.tvPriceSub);
        btnContinue = findViewById(R.id.btnContinue);

        tabUpi = findViewById(R.id.tabUpi);
        tabCard = findViewById(R.id.tabCard);
        tabNetBanking = findViewById(R.id.tabNetBanking);
        panelUpi = findViewById(R.id.panelUpi);
        panelCard = findViewById(R.id.panelCard);
        panelNetBanking = findViewById(R.id.panelNetBanking);
        btnPay = findViewById(R.id.btnPay);
        progressPay = findViewById(R.id.progressPay);

        ivSuccessCheck = findViewById(R.id.ivSuccessCheck);
        tvTxnId = findViewById(R.id.tvTxnId);
        tvValidUntil = findViewById(R.id.tvValidUntil);
        btnStartPro = findViewById(R.id.btnStartPro);
    }

    // ── PLANS STATE ──────────────────────────────────────────────────────────

    private void setupPlansState() {
        updatePlanToggle(true); // annual pre-selected

        btnAnnual.setOnClickListener(v -> updatePlanToggle(true));
        btnMonthly.setOnClickListener(v -> updatePlanToggle(false));

        btnContinue.setOnClickListener(v -> showState(layoutPayment));
    }

    private void updatePlanToggle(boolean annual) {
        isAnnual = annual;
        int transparent = android.graphics.Color.TRANSPARENT;
        if (annual) {
            btnAnnual.setBackgroundTintList(getColorStateList(R.color.green_primary));
            btnAnnual.setTextColor(getColor(R.color.white));
            btnMonthly.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(transparent));
            btnMonthly.setTextColor(getColor(R.color.text_secondary));
            tvPrice.setText("₹1,499/year");
            tvPriceSub.setText(getString(R.string.plan_per_month_breakdown));
            if (btnPay != null) btnPay.setText(getString(R.string.pay_annual));
        } else {
            btnMonthly.setBackgroundTintList(getColorStateList(R.color.green_primary));
            btnMonthly.setTextColor(getColor(R.color.white));
            btnAnnual.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(transparent));
            btnAnnual.setTextColor(getColor(R.color.text_secondary));
            tvPrice.setText("₹199/month");
            tvPriceSub.setText("Billed monthly · cancel anytime");
            if (btnPay != null) btnPay.setText(getString(R.string.pay_monthly));
        }
    }

    // ── PAYMENT STATE ─────────────────────────────────────────────────────────

    private void setupPaymentState() {
        tabUpi.setOnClickListener(v -> selectPaymentTab(0));
        tabCard.setOnClickListener(v -> selectPaymentTab(1));
        tabNetBanking.setOnClickListener(v -> selectPaymentTab(2));

        // Quick UPI shortcuts
        Button btnGpay = findViewById(R.id.btnGpay);
        Button btnPhonepe = findViewById(R.id.btnPhonepe);
        Button btnPaytm = findViewById(R.id.btnPaytm);
        if (btnGpay != null) btnGpay.setOnClickListener(v -> prefillUpi("name@okicici"));
        if (btnPhonepe != null) btnPhonepe.setOnClickListener(v -> prefillUpi("name@ybl"));
        if (btnPaytm != null) btnPaytm.setOnClickListener(v -> prefillUpi("name@paytm"));

        // Net Banking spinner
        Spinner spinner = findViewById(R.id.spinnerBank);
        if (spinner != null) {
            String[] banks = {"Select Bank", "SBI", "HDFC", "ICICI", "Axis", "Kotak", "Others"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, banks);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        btnPay.setOnClickListener(v -> simulatePayment());
    }

    private void selectPaymentTab(int tab) {
        int transparent = android.graphics.Color.TRANSPARENT;
        tabUpi.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(transparent));
        tabUpi.setTextColor(getColor(R.color.text_secondary));
        tabCard.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(transparent));
        tabCard.setTextColor(getColor(R.color.text_secondary));
        tabNetBanking.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(transparent));
        tabNetBanking.setTextColor(getColor(R.color.text_secondary));

        panelUpi.setVisibility(View.GONE);
        panelCard.setVisibility(View.GONE);
        panelNetBanking.setVisibility(View.GONE);

        Button activeTab = tab == 0 ? tabUpi : (tab == 1 ? tabCard : tabNetBanking);
        View activePanel = tab == 0 ? panelUpi : (tab == 1 ? panelCard : panelNetBanking);
        activeTab.setBackgroundTintList(getColorStateList(R.color.green_primary));
        activeTab.setTextColor(getColor(R.color.white));
        activePanel.setVisibility(View.VISIBLE);
    }

    private void prefillUpi(String mockId) {
        com.google.android.material.textfield.TextInputEditText et = findViewById(R.id.etUpiId);
        if (et != null) et.setText(mockId);
    }

    private void simulatePayment() {
        btnPay.setEnabled(false);
        progressPay.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressPay.setVisibility(View.GONE);
            showSuccessState();
        }, 1500);
    }

    // ── SUCCESS STATE ─────────────────────────────────────────────────────────

    private void setupSuccessState() {
        btnStartPro.setOnClickListener(v -> finish());
    }

    private void showSuccessState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SUBSCRIBED, true).apply();

        String txnId = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.US);
        tvTxnId.setText(getString(R.string.payment_txn_id, "TXN-" + txnId));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        String validDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.getTime());
        tvValidUntil.setText(getString(R.string.payment_valid_until, validDate));

        showState(layoutSuccess);
        animateSuccessCheck();
    }

    private void animateSuccessCheck() {
        ivSuccessCheck.setScaleX(0f);
        ivSuccessCheck.setScaleY(0f);
        ivSuccessCheck.setAlpha(0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(ivSuccessCheck, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(ivSuccessCheck, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(ivSuccessCheck, "alpha", 0f, 1f)
        );
        set.setDuration(400);
        set.start();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void showState(View target) {
        layoutPlans.setVisibility(View.GONE);
        layoutPayment.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.GONE);
        target.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
