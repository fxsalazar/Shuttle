package com.aa.salazar;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;


/**
 * Created by fxsalazar
 * 27/01/2018.
 */

public final class MediaBrowserLifecycleManager implements LifecycleObserver, MediaBrowserManager {
    private static final String TAG = MediaBrowserLifecycleManager.class.getSimpleName().substring(0, 22);
    private MediaBrowserCompat mediaBrowser;
    private Context context;
    private final Callback mediaBrowserLifecycleManagerCallback;
    private final MediaControllerCompat.Callback mediaControllerCallback;
    private final MediaBrowserCompat.ConnectionCallback connectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        mediaBrowserLifecycleManagerCallback.onConnected(connectToSession(mediaBrowser.getSessionToken()));
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not connect media controller", e);
                        mediaBrowserLifecycleManagerCallback.onError(new Exception("could not connect media controller", e));
                    }
                }
            };
    private MediaControllerCompat mediaController;
    private Class<? extends MediaBrowserServiceCompat> mediaServiceClass;

    public static MediaBrowserManager bind(LifecycleOwner lifecycleOwner,
                                           Context context,
                                           Callback mediaBrowserLifecycleManagerCallback,
                                           MediaControllerCompat.Callback mediaControllerCallback,
                                           Class<? extends MediaBrowserServiceCompat> mediaServiceClass) {
        return new MediaBrowserLifecycleManager(
                lifecycleOwner,
                context,
                mediaBrowserLifecycleManagerCallback,
                mediaControllerCallback,
                mediaServiceClass);
    }

    private MediaBrowserLifecycleManager(LifecycleOwner lifecycleOwner,
                                         Context context,
                                         Callback mediaBrowserLifecycleManagerCallback,
                                         MediaControllerCompat.Callback mediaControllerCallback,
                                         Class<? extends MediaBrowserServiceCompat> mediaServiceClass) {
        this.context = context;
        this.mediaBrowserLifecycleManagerCallback = mediaBrowserLifecycleManagerCallback;
        this.mediaControllerCallback = mediaControllerCallback;
        this.mediaServiceClass = mediaServiceClass;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {
        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mediaBrowser = new MediaBrowserCompat(
                this.context,
                new ComponentName(this.context, mediaServiceClass), connectionCallback, null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onStart() {
        mediaBrowser.connect();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onStop() {
        mediaBrowser.disconnect();
        mediaController.unregisterCallback(mediaControllerCallback);
    }

    private MediaControllerCompat connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        mediaController = new MediaControllerCompat(this.context, token);
        mediaController.registerCallback(mediaControllerCallback);
        return mediaController;
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mediaBrowser;
    }

    public interface Callback {
        void onConnected(MediaControllerCompat mediaControllerCompat);

        void onError(Exception exception);
    }
}
