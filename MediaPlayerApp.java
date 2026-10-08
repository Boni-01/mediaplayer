import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.util.List;

public class MediaPlayerApp extends Application {

    //  Core fields
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private ListView<File> playlistView;
    private ObservableList<File> playlist;
    private int currentIndex = -1;
    private double volume = 0.5;
    private boolean isMuted = false;

    // UI fields
    private Label nowPlayingLabel;
    private Label statusLabel;
    private Label volumeLabel;
    private Label volumeIconLabel;
    private Slider volumeSlider;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button nextButton;
    private Button prevButton;
    private Button muteButton;

    @Override
    public void start(Stage primaryStage) {

        // ROOT LAYOUT
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
//logo 
        
        javafx.scene.Node logo = createLogo();

        Label welcomeLabel = new Label("Welcome to");
        welcomeLabel.getStyleClass().add("welcome-label");

        Label brandLabel = new Label("Bonnie Media Player");
        brandLabel.getStyleClass().add("brand-label");

        VBox titleBox = new VBox(0, welcomeLabel, brandLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // Spacer pushes the logo to the far right corner
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(18, titleBox, spacer, logo);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(14, 25, 14, 25));
        topBox.setMinHeight(100);
        topBox.setPrefHeight(100);
        topBox.getStyleClass().add("top-bar");
        root.setTop(topBox);

        // Entrance animations
        FadeTransition brandFade = new FadeTransition(Duration.seconds(1.4), brandLabel);
        brandFade.setFromValue(0.0);
        brandFade.setToValue(1.0);
        brandFade.play();

        ScaleTransition logoPop = new ScaleTransition(Duration.millis(700), logo);
        logoPop.setFromX(0.4);
        logoPop.setFromY(0.4);
        logoPop.setToX(1.0);
        logoPop.setToY(1.0);
        logoPop.play();

        
        // CENTER: MediaView  +  VOLUME SLIDER
        
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        mediaView.fitWidthProperty().bind(root.widthProperty().subtract(280));
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(420));

        volumeIconLabel = new Label("🔊");
        volumeIconLabel.getStyleClass().add("volume-icon");

        volumeSlider = new Slider(0.0, 1.0, volume);
        volumeSlider.setPrefWidth(750);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setBlockIncrement(0.05);

        volumeLabel = new Label("Volume: 50%");
        volumeLabel.getStyleClass().add("volume-label");

        HBox volumeBox = new HBox(12, volumeIconLabel, volumeSlider, volumeLabel);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setPadding(new Insets(8, 0, 0, 0));

        VBox mediaContainer = new VBox(10, mediaView, volumeBox);
        mediaContainer.setAlignment(Pos.CENTER);
        mediaContainer.setPadding(new Insets(10));
        mediaContainer.getStyleClass().add("media-container");
        VBox.setVgrow(mediaContainer, Priority.ALWAYS);
        root.setCenter(mediaContainer);

        FadeTransition mediaFade = new FadeTransition(Duration.seconds(1.2), mediaContainer);
        mediaFade.setFromValue(0.0);
        mediaFade.setToValue(1.0);
        mediaFade.play();

        // RIGHT: Playlist
        playlist = FXCollections.observableArrayList();
        playlistView = new ListView<>(playlist);
        playlistView.setPrefWidth(230);
        playlistView.setMinWidth(200);
        playlistView.getStyleClass().add("playlist-view");

        playlistView.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("♫  " + item.getName());
                }
            }
        });

        Button addButton = new Button("＋ Add Files");
        Button removeButton = new Button("✖ Remove");
        addButton.getStyleClass().add("action-button");
        removeButton.getStyleClass().add("action-button-danger");
        addButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setMaxWidth(Double.MAX_VALUE);
        applyHoverScale(addButton);
        applyHoverScale(removeButton);

        Label playlistTitle = new Label("Playlist");
        playlistTitle.getStyleClass().add("section-title");

        VBox playlistBox = new VBox(10, playlistTitle, playlistView, addButton, removeButton);
        playlistBox.setPadding(new Insets(15));
        playlistBox.setMinWidth(240);
        playlistBox.getStyleClass().add("playlist-box");
        VBox.setVgrow(playlistView, Priority.ALWAYS);
        root.setRight(playlistBox);

        // BOTTOM: Controls
        nowPlayingLabel = new Label("No media loaded");
        nowPlayingLabel.getStyleClass().add("now-playing");

        statusLabel = new Label("Status: Idle");
        statusLabel.getStyleClass().add("status-label");

        prevButton = new Button("⏮ Previous");
        playButton = new Button("▶ Play");
        pauseButton = new Button("⏸ Pause");
        stopButton = new Button("⏹ Stop");
        nextButton = new Button("⏭ Next");
        muteButton = new Button("🔊 Mute");

        Button[] controls = {prevButton, playButton, pauseButton, stopButton, nextButton, muteButton};
        for (Button b : controls) {
            b.getStyleClass().add("control-button");
            applyHoverScale(b);
        }

        HBox controlBox = new HBox(10, prevButton, playButton, pauseButton, stopButton, nextButton, muteButton);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(5));

        Label hint = new Label("Keys:  SPACE play/pause  •  S stop  •  N next  •  P previous  •  ↑↓ volume  •  M mute");
        hint.getStyleClass().add("hint-label");

        VBox bottomBox = new VBox(8, nowPlayingLabel, statusLabel, controlBox, hint);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setMinHeight(150);
        bottomBox.setPrefHeight(150);
        bottomBox.getStyleClass().add("bottom-bar");
        root.setBottom(bottomBox);

        TranslateTransition bottomSlide = new TranslateTransition(Duration.seconds(1), bottomBox);
        bottomSlide.setFromY(80);
        bottomSlide.setToY(0);
        bottomSlide.play();

        // Playlist Management Logic
        addButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Media Files");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Media Files", "*.mp3", "*.mp4", "*.wav", "*.m4a", "*.aac", "*.flv"),
                    new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.m4a", "*.aac"),
                    new FileChooser.ExtensionFilter("Video", "*.mp4", "*.flv")
            );
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null && !files.isEmpty()) {
                playlist.addAll(files);
                if (currentIndex == -1) {
                    currentIndex = 0;
                    playlistView.getSelectionModel().select(0);
                    playCurrent();
                }
                pulse(playlistView);
            }
        });

        removeButton.setOnAction(e -> {
            int index = playlistView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                playlist.remove(index);
                if (playlist.isEmpty()) {
                    stopCurrent();
                    currentIndex = -1;
                    nowPlayingLabel.setText("No media loaded");
                    statusLabel.setText("Status: Idle");
                } else if (index == currentIndex) {
                    stopCurrent();
                    currentIndex = Math.min(index, playlist.size() - 1);
                    playlistView.getSelectionModel().select(currentIndex);
                    playCurrent();
                } else if (index < currentIndex) {
                    currentIndex--;
                }
            }
        });

        playlistView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int newIndex = playlist.indexOf(newVal);
                if (newIndex != currentIndex) {
                    currentIndex = newIndex;
                    playCurrent();
                }
            }
        });

        // Button Controls
        playButton.setOnAction(e -> playCurrent());
        pauseButton.setOnAction(e -> pauseCurrent());
        stopButton.setOnAction(e -> stopCurrent());
        nextButton.setOnAction(e -> playNext());
        prevButton.setOnAction(e -> playPrev());
        muteButton.setOnAction(e -> toggleMute());

        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            volume = newV.doubleValue();
            if (mediaPlayer != null && !isMuted) {
                mediaPlayer.setVolume(volume);
            }
            updateVolumeLabel();
        });

        // Keyboard Controls
        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add("data:text/css," + getInlineCSS().replace("\n", "").replace(" ", "%20"));

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE:
                    if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        pauseCurrent();
                    } else if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                        mediaPlayer.play();
                        statusLabel.setText("Status: Playing");
                    } else {
                        playCurrent();
                    }
                    event.consume();
                    break;
                case S:
                    stopCurrent();
                    event.consume();
                    break;
                case N:
                    playNext();
                    event.consume();
                    break;
                case P:
                    playPrev();
                    event.consume();
                    break;
                case UP:
                    changeVolume(0.05);
                    volumeSlider.setValue(volume);
                    event.consume();
                    break;
                case DOWN:
                    changeVolume(-0.05);
                    volumeSlider.setValue(volume);
                    event.consume();
                    break;
                case M:
                    toggleMute();
                    event.consume();
                    break;
                default:
                    break;
            }
        });

        primaryStage.setTitle("Bonnie Media Player");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(720);
        primaryStage.show();
    }

    // LOGO CREATION
    private javafx.scene.Node createLogo() {
        try {
            File logoFile = new File("logo.png");
            if (logoFile.exists()) {
                Image img = new Image(logoFile.toURI().toString());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(70);
                iv.setFitHeight(70);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.getStyleClass().add("logo-image");
                return iv;
            }
        } catch (Exception ignored) { }

        Circle circle = new Circle(34);
        circle.setFill(Color.web("#ff4081"));
        circle.setStroke(Color.web("#ffd6ff"));
        circle.setStrokeWidth(3);

        Text letter = new Text("B");
        letter.setFill(Color.WHITE);
        letter.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        StackPane badge = new StackPane(circle, letter);
        badge.getStyleClass().add("logo-badge");
        return badge;
    }

    // HELPER METHODS
    private void playCurrent() {
        if (currentIndex < 0 || currentIndex >= playlist.size()) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        File file = playlist.get(currentIndex);
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(isMuted ? 0 : volume);
            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.setOnEndOfMedia(this::playNext);

            mediaPlayer.statusProperty().addListener((obs, oldS, newS) ->
                    statusLabel.setText("Status: " + newS));

            mediaPlayer.play();

            nowPlayingLabel.setText("♫ Now Playing: " + file.getName());
            pulse(nowPlayingLabel);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.6), mediaView);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

        } catch (Exception ex) {
            statusLabel.setText("Status: Error loading file");
            System.err.println("Error loading media: " + ex.getMessage());
        }
    }

    private void pauseCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            statusLabel.setText("Status: Paused");
        }
    }

    private void stopCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        statusLabel.setText("Status: Stopped");
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            nowPlayingLabel.setText("⏹ Stopped: " + playlist.get(currentIndex).getName());
        }
    }

    private void playNext() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        playlistView.getSelectionModel().select(currentIndex);
    }

    private void playPrev() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playlistView.getSelectionModel().select(currentIndex);
    }

    private void changeVolume(double delta) {
        volume = Math.max(0.0, Math.min(1.0, volume + delta));
        if (mediaPlayer != null && !isMuted) {
            mediaPlayer.setVolume(volume);
        }
        updateVolumeLabel();
    }

    private void updateVolumeLabel() {
        volumeLabel.setText("Volume: " + (int) Math.round(volume * 100) + "%");
        if (isMuted || volume == 0) {
            volumeIconLabel.setText("🔇");
        } else if (volume < 0.5) {
            volumeIconLabel.setText("🔉");
        } else {
            volumeIconLabel.setText("🔊");
        }
    }

    private void toggleMute() {
        if (mediaPlayer == null) {
            isMuted = !isMuted;
            muteButton.setText(isMuted ? "🔇 Unmute" : "🔊 Mute");
            volumeIconLabel.setText(isMuted ? "🔇" : "🔊");
            return;
        }
        if (isMuted) {
            mediaPlayer.setVolume(volume);
            isMuted = false;
            muteButton.setText("🔊 Mute");
        } else {
            mediaPlayer.setVolume(0);
            isMuted = true;
            muteButton.setText("🔇 Unmute");
        }
        updateVolumeLabel();
    }

    private void applyHoverScale(Button button) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), button);
        grow.setToX(1.08);
        grow.setToY(1.08);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), button);
        shrink.setToX(1.0);
        shrink.setToY(1.0);

        button.setOnMouseEntered(e -> grow.playFromStart());
        button.setOnMouseExited(e -> shrink.playFromStart());
    }

    private void pulse(javafx.scene.Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    // INLINE CSS
    private String getInlineCSS() {
        return
            ".root-pane { -fx-background-color: linear-gradient(to bottom right, #1a0b2e, #2d1b4e, #4a1e5c); }" +
            ".top-bar { -fx-background-color: linear-gradient(to right, #4a148c, #6a1b9a, #c2185b); -fx-background-radius: 0 0 18 18; -fx-border-color: #ff4081; -fx-border-width: 0 0 2 0; }" +
            ".welcome-label { -fx-font-size: 13px; -fx-text-fill: #f8bbd0; -fx-font-style: italic; }" +
            ".brand-label { -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-effect: dropshadow(gaussian, #ff4081, 18, 0.7, 0, 0); }" +
            ".subtitle-label { -fx-font-size: 11px; -fx-text-fill: #e1bee7; }" +
            ".logo-image { -fx-effect: dropshadow(gaussian, #ff4081, 20, 0.8, 0, 0); }" +
            ".logo-badge { -fx-effect: dropshadow(gaussian, #ff4081, 20, 0.8, 0, 0); }" +
            ".media-container { -fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 12; -fx-border-color: #c2185b; -fx-border-radius: 12; -fx-border-width: 2; }" +
            ".playlist-box { -fx-background-color: linear-gradient(to bottom, #2d1b4e, #1a0b2e); -fx-border-color: #c2185b; -fx-border-width: 0 0 0 2; }" +
            ".section-title { -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff80ab; -fx-effect: dropshadow(gaussian, #c2185b, 8, 0.4, 0, 0); }" +
            ".playlist-view { -fx-background-color: rgba(255,255,255,0.05); -fx-control-inner-background: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #6a1b9a; }" +
            ".playlist-view .list-cell { -fx-background-color: transparent; -fx-text-fill: #f8bbd0; -fx-padding: 8; -fx-font-size: 13px; }" +
            ".playlist-view .list-cell:hover { -fx-background-color: rgba(194,24,91,0.35); -fx-text-fill: white; }" +
            ".playlist-view .list-cell:selected { -fx-background-color: linear-gradient(to right, #c2185b, #6a1b9a); -fx-text-fill: white; -fx-font-weight: bold; }" +
            ".action-button { -fx-background-color: linear-gradient(to right, #9c27b0, #e91e63); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 14; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #e91e63, 10, 0.4, 0, 0); }" +
            ".action-button:hover { -fx-background-color: linear-gradient(to right, #ab47bc, #f06292); }" +
            ".action-button-danger { -fx-background-color: linear-gradient(to right, #6a1b9a, #ad1457); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 14; -fx-cursor: hand; }" +
            ".action-button-danger:hover { -fx-background-color: linear-gradient(to right, #7b1fa2, #c2185b); }" +
            ".control-button { -fx-background-color: linear-gradient(to bottom, #4a148c, #880e4f); -fx-text-fill: #ffd6ff; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 18; -fx-cursor: hand; -fx-border-color: #f06292; -fx-border-radius: 25; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, #e91e63, 10, 0.3, 0, 0); }" +
            ".control-button:hover { -fx-background-color: linear-gradient(to bottom, #6a1b9a, #c2185b); -fx-text-fill: white; -fx-effect: dropshadow(gaussian, #ff69b4, 18, 0.7, 0, 0); }" +
            ".control-button:pressed { -fx-background-color: #c2185b; }" +
            ".bottom-bar { -fx-background-color: linear-gradient(to top, #1a0b2e, #2d1b4e); -fx-border-color: #c2185b; -fx-border-width: 2 0 0 0; }" +
            ".now-playing { -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ff80ab; -fx-effect: dropshadow(gaussian, #c2185b, 8, 0.3, 0, 0); }" +
            ".status-label { -fx-font-size: 12px; -fx-text-fill: #ce93d8; }" +
            ".volume-icon { -fx-font-size: 22px; -fx-text-fill: #ff80ab; -fx-effect: dropshadow(gaussian, #ff4081, 10, 0.5, 0, 0); }" +
            ".volume-label { -fx-text-fill: #f8bbd0; -fx-font-weight: bold; }" +
            ".volume-slider .track { -fx-background-color: #4a148c; -fx-background-radius: 5; }" +
            ".volume-slider .thumb { -fx-background-color: #ff4081; -fx-effect: dropshadow(gaussian, #ff4081, 10, 0.6, 0, 0); }" +
            ".volume-slider .thumb:hover { -fx-background-color: #ff80ab; }" +
            ".hint-label { -fx-font-size: 11px; -fx-text-fill: #ba68c8; -fx-font-style: italic; }";
    }

    public static void main(String[] args) {
        launch(args);
    }
}