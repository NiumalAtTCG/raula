package lk.javainstitute.raula.model;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;

public class PaymentHelper {
    public static void initiatePayment(Activity activity, double amount, String orderId, String description, String email, String firstName, String lastName, String mobile) {
        InitRequest req = new InitRequest();
        req.setMerchantId("1225559"); // Replace with your Merchant ID
        req.setCurrency("LKR");
        req.setAmount(amount);
        req.setOrderId(orderId);
        req.setItemsDescription(description);
        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(activity, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Use LIVE_URL for production

        Log.d("PaymentHelper", "Starting payment activity with Order ID: " + orderId);

        activity.startActivityForResult(intent, 110);
    }
}
