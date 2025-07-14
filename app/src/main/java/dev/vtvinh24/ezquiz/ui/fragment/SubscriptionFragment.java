package dev.vtvinh24.ezquiz.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.AuthViewModel;
import dev.vtvinh24.ezquiz.ui.LoginActivity;

public class SubscriptionFragment extends Fragment {

    private TextView tvUserName, tvPremiumStatus;
    private Button btnMonthlyPlan, btnYearlyPlan, btnRedeemCode, btnLogout;
    private TextInputEditText etGiftCode;
    private ProgressBar progressBarRedeem;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupClickListeners();
        observeUserData();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvPremiumStatus = view.findViewById(R.id.tvPremiumStatus);
        btnMonthlyPlan = view.findViewById(R.id.btnMonthlyPlan);
        btnYearlyPlan = view.findViewById(R.id.btnYearlyPlan);
        btnRedeemCode = view.findViewById(R.id.btnRedeemCode);
        btnLogout = view.findViewById(R.id.btnLogout);
        etGiftCode = view.findViewById(R.id.etGiftCode);
        progressBarRedeem = view.findViewById(R.id.progressBarRedeem);
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    private void setupClickListeners() {
        btnMonthlyPlan.setOnClickListener(v -> purchaseMonthlyPlan());
        btnYearlyPlan.setOnClickListener(v -> purchaseYearlyPlan());
        btnRedeemCode.setOnClickListener(v -> redeemGiftCode());
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void observeUserData() {
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText("Welcome, " + user.getName());

                if (user.isPremium()) {
                    tvPremiumStatus.setText("Premium Active • Unlimited access");
                    btnMonthlyPlan.setText("Active");
                    btnYearlyPlan.setText("Active");
                    btnMonthlyPlan.setEnabled(false);
                    btnYearlyPlan.setEnabled(false);
                } else {
                    tvPremiumStatus.setText("Free Plan • 10 prompts, 3 images remaining");
                    btnMonthlyPlan.setText("Choose");
                    btnYearlyPlan.setText("Choose");
                    btnMonthlyPlan.setEnabled(true);
                    btnYearlyPlan.setEnabled(true);
                }
            }
        });

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBarRedeem.setVisibility(View.VISIBLE);
                btnRedeemCode.setEnabled(false);
            } else {
                progressBarRedeem.setVisibility(View.GONE);
                btnRedeemCode.setEnabled(true);
            }
        });
    }

    private void purchaseMonthlyPlan() {
        Toast.makeText(getContext(), "Opening payment for Monthly Plan (99,000 VND)", Toast.LENGTH_LONG).show();
        // Here you would integrate with your payment provider (e.g., VNPay, ZaloPay, etc.)
        openPaymentGateway("monthly", "99000");
    }

    private void purchaseYearlyPlan() {
        Toast.makeText(getContext(), "Opening payment for Yearly Plan (999,000 VND)", Toast.LENGTH_LONG).show();
        // Here you would integrate with your payment provider
        openPaymentGateway("yearly", "999000");
    }

    private void openPaymentGateway(String planType, String amount) {
        // This is a placeholder for payment integration
        // You would typically integrate with:
        // - VNPay
        // - ZaloPay
        // - Momo
        // - Google Play Billing
        // etc.

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://your-payment-gateway.com/checkout?plan=" + planType + "&amount=" + amount));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Payment service temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void redeemGiftCode() {
        String code = etGiftCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a gift code", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.redeemGiftCode(code).observe(this, result -> {
            if (result.isSuccess()) {
                etGiftCode.setText("");
                Toast.makeText(getContext(),
                    "Gift code redeemed successfully! " + result.getPremium().getDaysAdded() + " days added.",
                    Toast.LENGTH_LONG).show();

                // Refresh user data to update UI
                observeUserData();
            } else {
                Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performLogout() {
        authViewModel.logout();
        Toast.makeText(getContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
