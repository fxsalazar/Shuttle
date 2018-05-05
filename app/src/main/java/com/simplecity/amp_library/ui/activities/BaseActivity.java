package com.simplecity.amp_library.ui.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;
import com.afollestad.aesthetic.AestheticActivity;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.greysonparrelli.permiso.Permiso;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.billing.BillingManager;
import com.simplecity.amp_library.constants.Config;
import com.simplecity.amp_library.playback.MediaManager;
import com.simplecity.amp_library.playback.MediaManagerLifecycle;
import com.simplecity.amp_library.playback.constants.InternalIntents;
import com.simplecity.amp_library.ui.dialog.UpgradeDialog;
import com.simplecity.amp_library.utils.MusicServiceConnectionUtils;
import com.simplecity.amp_library.utils.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public abstract class BaseActivity extends AestheticActivity implements MediaManagerLifecycle.Callback {

    @Nullable
    private BillingManager billingManager;

    private MediaManagerLifecycle mediaManagerLifecycle;

    @CallSuper
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mediaManagerLifecycle = new MusicServiceConnectionUtils(this, this);

        billingManager = new BillingManager(this, new BillingManager.BillingUpdatesListener() {
            @Override
            public void onPurchasesUpdated(List<Purchase> purchases) {
                for (Purchase purchase : purchases) {
                    if (purchase.getSku().equals(Config.SKU_PREMIUM)) {
                        ShuttleApplication.getInstance().setIsUpgraded(true);
                    }
                }
            }

            @Override
            public void onPremiumPurchaseCompleted() {
                ShuttleApplication.getInstance().setIsUpgraded(true);
                UpgradeDialog.getUpgradeSuccessDialog(BaseActivity.this).show();
            }

            @Override
            public void onPremiumPurchaseRestored() {
                ShuttleApplication.getInstance().setIsUpgraded(true);
                Toast.makeText(BaseActivity.this, R.string.iab_purchase_restored, Toast.LENGTH_SHORT).show();
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        keepScreenOn(SettingsManager.getInstance().keepScreenOn());
        super.onResume();

        Permiso.getInstance().setActivity(this);

        if (billingManager != null && billingManager.getBillingClientResponseCode() == BillingClient.BillingResponse.OK) {
            billingManager.queryPurchases();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) {
            billingManager.destroy();
        }

        super.onDestroy();
    }

    @Nullable
    public BillingManager getBillingManager() {
        return billingManager;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Fix for issue on LG devices
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Fix for issue on LG devices
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void keepScreenOn(boolean on) {
        final Window window = getWindow();
        if (on) {
            window.addFlags(FLAG_KEEP_SCREEN_ON);
        } else {
            window.clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    protected abstract String screenName();

    @NonNull
    public MediaManager getMediaManager() {
        return mediaManagerLifecycle.getMediaManager();
    }

    @Override
    public void onMediaManagerConnected() {
        sendBroadcast(new Intent(InternalIntents.SERVICE_CONNECTED));
    }

    @Override
    public void onMediaManagerConnectionSuspended() {
        Toast.makeText(this, "Service Connection Suspended",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMediaManagerConnectionError(@NotNull Exception exception) {
        Toast.makeText(this, exception.getMessage(),Toast.LENGTH_LONG).show();
    }
}