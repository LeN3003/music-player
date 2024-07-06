
package musicfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    private TableView<Song> songTable;

    @FXML
    private TableColumn<Song, String> titleColumn;

    @FXML
    private TableColumn<Song, String> artistColumn;

    @FXML
    private TableColumn<Song, String> durationColumn;

    private final List<Song> songData = new ArrayList<>();
    private Song currentlyPlayingSong = null;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        loadSongsFromFileSystem();
        setupTableButtons();
        songTable.getItems().addAll(songData);

        setRowFactoryWithAlternatingColors();
        setCellFactoryWithTextColors();
    }

    private void loadSongsFromFileSystem() {
        String userHome = System.getProperty("user.home");
        Path musicDir = Paths.get(userHome, "Music");

        try {
            Files.walk(musicDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".mp3") || path.toString().endsWith(".wav"))
                    .forEach(path -> songData.add(Song.createSongFromPath(path)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTableButtons() {
        TableColumn<Song, Void> buttonsColumn = new TableColumn<>("Controls");
        buttonsColumn.setPrefWidth(200);

        buttonsColumn.setCellFactory(new Callback<TableColumn<Song, Void>, TableCell<Song, Void>>() {
            @Override
            public TableCell<Song, Void> call(final TableColumn<Song, Void> param) {
                return new TableCell<Song, Void>() {
                    private final HBox buttonsBox = new HBox();
                    private final Button playButton = createButtonWithIcon("play.png");
                    private final Button pauseButton = createButtonWithIcon("pause.png");
                    private final Button restartButton = createButtonWithIcon("restart.png");
                    private final Button loopButton = createButtonWithIcon("loop.png");

                    {
                        buttonsBox.getChildren().addAll(playButton, pauseButton, restartButton, loopButton);

                        playButton.setOnAction(event -> {
                            Song song = getTableRow().getItem();
                            if (song != null) {
                                if (currentlyPlayingSong != null && currentlyPlayingSong != song) {
                                    currentlyPlayingSong.getMediaPlayer().stop();
                                }
                                song.play();
                                currentlyPlayingSong = song;
                            }
                        });

                        pauseButton.setOnAction(event -> {
                            Song song = getTableRow().getItem();
                            if (song != null) {
                                song.pause();
                            }
                        });

                        restartButton.setOnAction(event -> {
                            Song song = getTableRow().getItem();
                            if (song != null) {
                                song.restart();
                            }
                        });

                        loopButton.setOnAction(event -> {
                            Song song = getTableRow().getItem();
                            if (song != null) {
                                song.toggleLoop();
                                loopButton.setStyle(song.isLooping() ? "-fx-background-color: #00ff00;" : "-fx-background-color: #ffffff;");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        });

        songTable.getColumns().add(buttonsColumn);
    }

    private Button createButtonWithIcon(String iconFileName) {
        Button button = new Button();
        try {
            Image icon = new Image(getClass().getResourceAsStream("/musicfx/icons/" + iconFileName));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(25); // Set the desired width
            imageView.setFitHeight(25); // Set the desired height
            button.setGraphic(imageView);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Icon file not found: " + iconFileName);
        }
        return button;
    }

    private void setRowFactoryWithAlternatingColors() {
        songTable.setRowFactory(tableView -> new TableRow<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (!empty) {
                    int index = getIndex();
                    if (index % 2 == 0) {
                        setStyle("-fx-background-color: #000000;");
                    } else {
                        setStyle("-fx-background-color: #2a2a2a;");
                    }
                } else {
                    setStyle("");  // Reset the style if the row is empty
                }
            }
        });
    }

    private void setCellFactoryWithTextColors() {
        Callback<TableColumn<Song, String>, TableCell<Song, String>> cellFactory = column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white;");
                }
            }
        };

        titleColumn.setCellFactory(cellFactory);
        artistColumn.setCellFactory(cellFactory);
        durationColumn.setCellFactory(cellFactory);
    }
}
