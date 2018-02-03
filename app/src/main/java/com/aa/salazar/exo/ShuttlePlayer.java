package com.aa.salazar.exo;

import android.os.Looper;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public class ShuttlePlayer implements ShuttleExoPlayer {

    private SimpleExoPlayer simpleExoPlayer;

    public static ShuttleExoPlayer createInstance(SimpleExoPlayer simpleExoPlayer) {
        return new ShuttlePlayer(simpleExoPlayer);
    }

    private ShuttlePlayer(SimpleExoPlayer simpleExoPlayer) {
        this.simpleExoPlayer = simpleExoPlayer;
    }

    @Override
    public void unDuckVolume() {
        // TODO: 03/02/2018 implement
    }

    @Override
    public void duckVolume() {
        // TODO: 03/02/2018 implement
    }

    public Looper getPlaybackLooper() {
        return simpleExoPlayer.getPlaybackLooper();
    }

    public void addListener(Player.EventListener listener) {
        simpleExoPlayer.addListener(listener);
    }

    public void removeListener(Player.EventListener listener) {
        simpleExoPlayer.removeListener(listener);
    }

    public int getPlaybackState() {
        return simpleExoPlayer.getPlaybackState();
    }

    public void prepare(MediaSource mediaSource) {
        simpleExoPlayer.prepare(mediaSource);
    }

    public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
        simpleExoPlayer.prepare(mediaSource, resetPosition, resetState);
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        simpleExoPlayer.setPlayWhenReady(playWhenReady);
    }

    public boolean getPlayWhenReady() {
        return simpleExoPlayer.getPlayWhenReady();
    }

    public int getRepeatMode() {
        return simpleExoPlayer.getRepeatMode();
    }

    public void setRepeatMode(int repeatMode) {
        simpleExoPlayer.setRepeatMode(repeatMode);
    }

    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
        simpleExoPlayer.setShuffleModeEnabled(shuffleModeEnabled);
    }

    public boolean getShuffleModeEnabled() {
        return simpleExoPlayer.getShuffleModeEnabled();
    }

    public boolean isLoading() {
        return simpleExoPlayer.isLoading();
    }

    public void seekToDefaultPosition() {
        simpleExoPlayer.seekToDefaultPosition();
    }

    public void seekToDefaultPosition(int windowIndex) {
        simpleExoPlayer.seekToDefaultPosition(windowIndex);
    }

    public void seekTo(long positionMs) {
        simpleExoPlayer.seekTo(positionMs);
    }

    public void seekTo(int windowIndex, long positionMs) {
        simpleExoPlayer.seekTo(windowIndex, positionMs);
    }

    public void setPlaybackParameters(@Nullable PlaybackParameters playbackParameters) {
        simpleExoPlayer.setPlaybackParameters(playbackParameters);
    }

    public PlaybackParameters getPlaybackParameters() {
        return simpleExoPlayer.getPlaybackParameters();
    }

    public void stop() {
        simpleExoPlayer.stop();
    }

    public void release() {
        simpleExoPlayer.release();
    }

    public void sendMessages(ExoPlayer.ExoPlayerMessage... messages) {
        simpleExoPlayer.sendMessages(messages);
    }

    public void blockingSendMessages(ExoPlayer.ExoPlayerMessage... messages) {
        simpleExoPlayer.blockingSendMessages(messages);
    }

    public int getRendererCount() {
        return simpleExoPlayer.getRendererCount();
    }

    public int getRendererType(int index) {
        return simpleExoPlayer.getRendererType(index);
    }

    public TrackGroupArray getCurrentTrackGroups() {
        return simpleExoPlayer.getCurrentTrackGroups();
    }

    public TrackSelectionArray getCurrentTrackSelections() {
        return simpleExoPlayer.getCurrentTrackSelections();
    }

    public Timeline getCurrentTimeline() {
        return simpleExoPlayer.getCurrentTimeline();
    }

    public Object getCurrentManifest() {
        return simpleExoPlayer.getCurrentManifest();
    }

    public int getCurrentPeriodIndex() {
        return simpleExoPlayer.getCurrentPeriodIndex();
    }

    public int getCurrentWindowIndex() {
        return simpleExoPlayer.getCurrentWindowIndex();
    }

    public int getNextWindowIndex() {
        return simpleExoPlayer.getNextWindowIndex();
    }

    public int getPreviousWindowIndex() {
        return simpleExoPlayer.getPreviousWindowIndex();
    }

    public long getDuration() {
        return simpleExoPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return simpleExoPlayer.getCurrentPosition();
    }

    public long getBufferedPosition() {
        return simpleExoPlayer.getBufferedPosition();
    }

    public int getBufferedPercentage() {
        return simpleExoPlayer.getBufferedPercentage();
    }

    public boolean isCurrentWindowDynamic() {
        return simpleExoPlayer.isCurrentWindowDynamic();
    }

    public boolean isCurrentWindowSeekable() {
        return simpleExoPlayer.isCurrentWindowSeekable();
    }

    public boolean isPlayingAd() {
        return simpleExoPlayer.isPlayingAd();
    }

    public int getCurrentAdGroupIndex() {
        return simpleExoPlayer.getCurrentAdGroupIndex();
    }

    public int getCurrentAdIndexInAdGroup() {
        return simpleExoPlayer.getCurrentAdIndexInAdGroup();
    }

    public long getContentPosition() {
        return simpleExoPlayer.getContentPosition();
    }
}
