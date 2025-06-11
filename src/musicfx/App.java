
package musicfx;

// import javafx.application.Application;
// import javafx.fxml.FXMLLoader;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.stage.Stage;

// public class Main extends Application {
// @Override
// public void start(Stage primaryStage) throws Exception {
// Parent root = FXMLLoader.load(getClass().getResource("musicfx.fxml"));
// primaryStage.setTitle("Music Player");
// primaryStage.setScene(new Scene(root));
// primaryStage.show();
// }

// public static void main(String[] args) {
// launch(args);
// }
// }

import javafx.application.Application;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private MediaPlayer mediaPlayer;

    private Slider progressBar;
    private Label timeLabel;
    private Slider volumeSlider;

    private List<File> playlist = new ArrayList<>();
    private int currentIndex = -1;

    private Label titleLabel;
    private Label artistLabel;

    private BorderPane root;
    private ImageView albumArtView = new ImageView();

    private boolean isRepeat = false;

    @Override
    public void start(Stage primaryStage) {

        // Buttons
        Button btnOpen = new Button("Open");
        Button btnPlay = new Button("Play");
        Button btnPause = new Button("Pause");
        Button btnStop = new Button("Stop");
        Button btnPrev = new Button("Previous");
        Button btnNext = new Button("Next");
        Button btnRepeat = new Button("Repeat");

        // Labels
        artistLabel = new Label("Artist: Unknown");
        titleLabel = new Label("Title: Unknown");
        timeLabel = new Label("00:00 / 00:00");

        // Sliders
        progressBar = new Slider(0, 100, 0);
        progressBar.setPrefWidth(400);

        volumeSlider = new Slider(0, 1, 0.5);

        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.1);

        // Disable buttons initially
        btnPlay.setDisable(true);
        btnPause.setDisable(true);
        btnStop.setDisable(true);
        btnPrev.setDisable(true);
        btnNext.setDisable(true);

        // Layout containers
        HBox topBox = new HBox(20, artistLabel, titleLabel);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER);

        // HBox controlButtons = new HBox(10, btnOpen, btnPrev, btnPlay, btnPause,
        // btnStop, btnNext);
        HBox controlButtons = new HBox(10, btnOpen, btnPrev, btnPlay, btnPause, btnStop, btnNext, btnRepeat);

        controlButtons.setPadding(new Insets(10));
        controlButtons.setAlignment(Pos.CENTER);

        HBox slidersBox = new HBox(10, new Label("Volume:"), volumeSlider);
        slidersBox.setPadding(new Insets(10));
        slidersBox.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(10, progressBar, timeLabel, slidersBox);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setAlignment(Pos.CENTER);

        albumArtView.setFitHeight(150);
        albumArtView.setPreserveRatio(true);
        albumArtView.setSmooth(true);
        albumArtView.setVisible(false); // Initially hidden

        root = new BorderPane();
        root.setTop(topBox);
        // root.setCenter(controlButtons);
        root.setBottom(bottomBox);
        // root.setLeft(albumArtView); // Place it on the left

        VBox centerBox = new VBox(20, albumArtView, controlButtons);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 500);

        scene.setOnDragOver(event -> {
            if (event.getGestureSource() != scene && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        scene.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav")
                            || file.getName().endsWith(".aiff")) {
                        playlist.add(file);
                        currentIndex = playlist.size() - 1;
                        playMedia(file);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        System.out.println("CSS URL: " + getClass().getResource("/musicfx/style.css"));

        scene.getStylesheets().add(getClass().getResource("/musicfx/style.css").toExternalForm());

        primaryStage.setTitle("Java MP");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Event handlers

        btnOpen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aiff"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                playlist.add(file);
                currentIndex = playlist.size() - 1;
                playMedia(file);

                // Enable buttons based on playlist state
                btnPlay.setDisable(false);
                btnPause.setDisable(false);
                btnStop.setDisable(false);
                btnPrev.setDisable(currentIndex <= 0);
                btnNext.setDisable(currentIndex >= playlist.size() - 1);
            }
        });

        btnPlay.setOnAction(e -> {
            if (mediaPlayer != null)
                mediaPlayer.play();
        });

        btnPause.setOnAction(e -> {
            if (mediaPlayer != null)
                mediaPlayer.pause();
        });

        btnStop.setOnAction(e -> {
            if (mediaPlayer != null)
                mediaPlayer.stop();
        });

        btnNext.setOnAction(e -> {
            if (currentIndex < playlist.size() - 1) {
                currentIndex++;
                playMedia(playlist.get(currentIndex));
                btnPrev.setDisable(false);
                btnNext.setDisable(currentIndex == playlist.size() - 1);
            }
        });

        btnPrev.setOnAction(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                playMedia(playlist.get(currentIndex));
                btnNext.setDisable(false);
                btnPrev.setDisable(currentIndex == 0);
            }
        });

        btnRepeat.setOnAction(e -> {
            isRepeat = !isRepeat;
            btnRepeat.setText(isRepeat ? "Repeat: On" : "Repeat");
        });

    }

    private void playMedia(File file) {
        // Stop existing player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        albumArtView.setImage(null);
        albumArtView.setVisible(false);

        Media media = new Media(file.toURI().toString());

        // Listen to metadata changes and update labels
        media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {

            if (change.wasAdded()) {
                String key = change.getKey();
                Object value = change.getValueAdded();

                if ("artist".equalsIgnoreCase(key)) {
                    artistLabel.setText("Artist: " + value.toString());
                } else if ("title".equalsIgnoreCase(key)) {
                    titleLabel.setText("Title: " + value.toString());
                } else if ("image".equalsIgnoreCase(key) && value instanceof javafx.scene.image.Image) {
                    albumArtView.setImage((Image) value);
                    albumArtView.setVisible(true);
                }
            }
        });

        mediaPlayer = new MediaPlayer(media);

        // Bind volume slider to media player volume
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());

        // Update progress bar and time label during playback
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressBar.isValueChanging()) {
                double current = mediaPlayer.getCurrentTime().toMillis();
                double total = mediaPlayer.getTotalDuration().toMillis();
                if (total > 0) {
                    progressBar.setValue(current / total * 100);
                    timeLabel.setText(formatTime(mediaPlayer.getCurrentTime()) + " / " +
                            formatTime(mediaPlayer.getTotalDuration()));
                }
            }
        });

        // mediaPlayer.setOnEndOfMedia(() -> {
        // if (currentIndex < playlist.size() - 1) {
        // currentIndex++;
        // playMedia(playlist.get(currentIndex));
        // }
        // });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (isRepeat) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            } else if (currentIndex < playlist.size() - 1) {
                currentIndex++;
                playMedia(playlist.get(currentIndex));
            }
        });

        // Allow seeking with the progress bar
        progressBar.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && mediaPlayer != null) {
                double total = mediaPlayer.getTotalDuration().toMillis();
                mediaPlayer.seek(Duration.millis(progressBar.getValue() * total / 100));
            }
        });

        progressBar.setOnMouseReleased(event -> {
            if (mediaPlayer != null) {
                double total = mediaPlayer.getTotalDuration().toMillis();
                mediaPlayer.seek(Duration.millis(progressBar.getValue() * total / 100));
            }
        });

        mediaPlayer.play();
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
