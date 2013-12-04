package org.kevoree.library.javase.javafx.media;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.kevoree.annotation.*;
import org.kevoree.api.Context;
import org.kevoree.api.ModelService;
import org.kevoree.library.javase.javafx.layout.SingleWindowLayout;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/08/13
 * Time: 14:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "javafx")
@ComponentType
public class JavaFXVideoDisplay {

    private Stage localWindow;
    private Tab tab;
    private Scene scene;

    private Media media;
    private MediaPlayer mediaPlayer;
    private MediaControl mediaControl;

    @Param(optional = true, defaultValue = "true")
    private boolean singleFrame;

    @KevoreeInject
    ModelService modelService;

    @KevoreeInject
    protected Context cmpContext;

    // TODO add a list of media to display
    // the media port is only use to add media to this list
    // when the user select one the media, this one is played
    private String mediaUrl;

    private final Object wait = new Object();

    @Start
    public void start() throws InterruptedException {
        SingleWindowLayout.initJavaFX();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // This method is invoked on JavaFX thread
                if (singleFrame) {
                    tab = new Tab();
                    tab.setText(cmpContext.getInstanceName());
                    tab.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                            if (tab.isSelected()) {
                                playOrInit();
                            } else {
                                pause();
                            }
                        }
                    });
                    SingleWindowLayout.getInstance().addTab(tab);
                    SingleWindowLayout.getInstance().getStage().focusedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                            if (SingleWindowLayout.getInstance().getStage().isFocused() && tab.isSelected()) {
                                playOrInit();
                            } else if (!SingleWindowLayout.getInstance().getStage().isFocused()) {
                                pause();
                            }
                        }
                    });
                } else {
                    localWindow = new Stage();
                    localWindow.setTitle(cmpContext.getInstanceName() + "@@@" + cmpContext.getNodeName());

                    localWindow.show();
//                    TODO localFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                }
                if (mediaUrl != null) {
                    playOrInit();
                }
                synchronized (wait) {
                    wait.notify();
                }
            }
        });
        synchronized (wait) {
            wait.wait();
        }
    }

    private void playOrInit() {
        if (mediaUrl != null) {
            if (mediaPlayer != null &&
                    (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)
                            || mediaPlayer.getStatus().equals(MediaPlayer.Status.STOPPED)
                            || mediaPlayer.getStatus().equals(MediaPlayer.Status.READY))) {
                mediaPlayer.play();
            } else {
                defineMedia(mediaUrl);
            }
        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            mediaPlayer.pause();
        }
    }

    @Stop
    public void stop() throws InterruptedException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // TODO unload javafx stuff
                if (singleFrame) {
                    SingleWindowLayout.getInstance().removeTab(tab);
                } else {
                    localWindow.hide();
                }
                synchronized (wait) {
                    wait.notify();
                }
            }
        });
        synchronized (wait) {
            wait.wait();
        }
    }

    @Update
    public void update() {

    }

    @Input
    public void media(final Object o) {
        Log.warn("URL received: {}", o.toString());
        if (o instanceof String) {
            mediaUrl = (String) o;
            if ((tab != null && tab.isSelected()) || localWindow != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        defineMedia(mediaUrl);
                    }
                });

            }
        }
    }

    private void defineMedia(String url) {
        // create media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        media = new Media(url);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaControl = new MediaControl(mediaPlayer);
        scene = new Scene(mediaControl);

        if (singleFrame) {
            tab.setContent(mediaControl);
        } else {
            localWindow.setScene(scene);
        }

        mediaControl.getScene().getStylesheets().add(JavaFXVideoDisplay.class.getResource("/mediaplayer.css").toExternalForm());

    }
}
