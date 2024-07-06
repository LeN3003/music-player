
package musicfx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.nio.file.Path;

public class Song {
    private final StringProperty title;
    private final StringProperty artist;
    private final StringProperty duration;
    private final Media media;
    private final MediaPlayer mediaPlayer;
    private boolean isPaused;
    private boolean isLooping;

    public Song(String title, String artist, String duration, Path filePath) {
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.duration = new SimpleStringProperty(duration);
        this.media = new Media(filePath.toUri().toString());
        this.mediaPlayer = new MediaPlayer(media);
        this.isPaused = false;
        this.isLooping = false;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public void play() {
        if (isPaused) {
            mediaPlayer.play();
        } else {
            mediaPlayer.seek(mediaPlayer.getStartTime());
            mediaPlayer.play();
        }
        isPaused = false;
    }

    public void pause() {
        mediaPlayer.pause();
        isPaused = true;
    }

    public void restart() {
        mediaPlayer.seek(mediaPlayer.getStartTime());
        mediaPlayer.play();
        isPaused = false;
    }

    public void toggleLoop() {
        isLooping = !isLooping;
        mediaPlayer.setCycleCount(isLooping ? MediaPlayer.INDEFINITE : 1);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public static Song createSongFromPath(Path path) {
        String title = path.getFileName().toString();
        String artist = "Unknown Artist";
        String duration = "Unknown Duration";

        try {
            AudioFile audioFile = AudioFileIO.read(path.toFile());
            Tag tag = audioFile.getTag();
            if (tag != null) {
                title = tag.getFirst(FieldKey.TITLE);
                artist = tag.getFirst(FieldKey.ARTIST);
            }
            long lengthInSeconds = audioFile.getAudioHeader().getTrackLength();
            duration = String.format("%02d:%02d", lengthInSeconds / 60, lengthInSeconds % 60);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Song(title, artist, duration, path);
    }
}
