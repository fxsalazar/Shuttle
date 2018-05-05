package com.simplecity.amp_library.utils;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.*;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.greysonparrelli.permiso.Permiso;
import com.simplecity.amp_library.playback.LocalBinder;
import com.simplecity.amp_library.playback.MediaManager;
import com.simplecity.amp_library.playback.MediaManagerLifecycle;
import com.simplecity.amp_library.playback.MusicService;
import com.simplecity.amp_library.ui.activities.BaseActivity;

import java.util.WeakHashMap;

public class MusicServiceConnectionUtils implements MediaManagerLifecycle, ServiceConnection {

    public static LocalBinder serviceBinder = null;
    private final BaseActivity activity;
    private Callback callback;

    @Nullable
    private MusicServiceConnectionUtils.ServiceToken token = null;

    private static final WeakHashMap<Context, ServiceBinder> connectionMap = new WeakHashMap<>();

    private MediaManager mediaManager = new MusicUtils();

    /**
     * @param activity The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     */
    public MusicServiceConnectionUtils(@NonNull final BaseActivity activity, @NonNull final MediaManagerLifecycle.Callback callback) {
        this.activity = activity;
        this.callback = callback;
        activity.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void bindToService() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    resumeService();
                } else {
                    Toast.makeText(activity, "Permission check failed", Toast.LENGTH_LONG).show();
                    activity.finish();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                callback.onRationaleProvided();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resumeService(){
        if (token == null) {
            Activity realActivity = activity.getParent();
            if (realActivity == null) {
                realActivity = activity;
            }
            final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
            contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
            final ServiceBinder binder = new ServiceBinder(this);
            if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicService.class), binder, 0)) {
                connectionMap.put(contextWrapper, binder);
                token = new ServiceToken(contextWrapper);
//                mediaManager = new MusicUtils();
            } else {
                callback.onMediaManagerConnectionError(new Exception("bindService failed"));
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stopService(){
        unbindFromService();
    }

    private void unbindFromService() {
        if (token == null) {
            return;
        }
        final ContextWrapper contextWrapper = token.wrappedContext;
        final ServiceBinder binder = connectionMap.remove(contextWrapper);
        if (binder == null) {
            return;
        }
        contextWrapper.unbindService(binder);
        if (connectionMap.isEmpty()) {
            serviceBinder = null;
        }
        token = null;
    }

    @NonNull
    @Override
    public MediaManager getMediaManager() {
        return this.mediaManager;
    }

    @Override
    @CallSuper
    public void onServiceConnected(ComponentName name, IBinder service) {
        callback.onMediaManagerConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        unbindFromService();
        callback.onMediaManagerConnectionSuspended();
    }

    static final class ServiceBinder implements ServiceConnection {

        private final ServiceConnection callback;

        public ServiceBinder(final ServiceConnection callback) {
            this.callback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {

            serviceBinder = (LocalBinder) service;

            if (callback != null) {
                callback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (callback != null) {
                callback.onServiceDisconnected(className);
            }
            serviceBinder = null;
        }
    }

    static final class ServiceToken {

        public ContextWrapper wrappedContext;

        /**
         * Constructor of <code>ServiceToken</code>
         *
         * @param context The {@link ContextWrapper} to use
         */
        public ServiceToken(final ContextWrapper context) {
            wrappedContext = context;
        }
    }
}