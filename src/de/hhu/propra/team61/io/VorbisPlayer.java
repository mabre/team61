package de.hhu.propra.team61.io;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for playing ogg vorbis files.
 *
 * “Ogg Vorbis is a fully open, non-proprietary, patent-and-royalty-free, general-purpose compressed audio format for
 * mid to high quality (8kHz-48.0kHz, 16+ bit, polyphonic) audio and music at fixed and variable bitrates from 16 to
 * 128 kbps/channel. This places Vorbis in the same competitive class as audio representations such as MPEG-4 (AAC), and
 * similar to, but higher performance than MPEG-1/2 audio layer 3, MPEG-4 audio (TwinVQ), WMA and PAC.”
 * (http://xiph.org/vorbis/)
 * <p>
 * This class is based on the code at http://www.javazoom.net/vorbisspi/documents.html. The VorbisSPI package is
 * licensed under LGPL. To use this class, make sure that the {@code lib} directory has been added to the libraries of
 * the project. If the library is missing, a {@code javax.sound.sampled.UnsupportedAudioFileException} will be thrown.
 * <p>
 * Use {@code VorbisPlayer.play("resources/audio/BGM/sample.ogg", true);} to play a background music, and
 * {@code VorbisPlayer.play("resources/audio/SFX/sample.ogg", false);} for sound effects. To stop playback, use
 * {@code VorbisPlayer.stop();}; if you do not call this method, the player threads are shut down when all files have
 * been played, which might be never if one file is repeated.
 */
public class VorbisPlayer {

    static ArrayList<Thread> playerThreads = new ArrayList<>(); // we might want to extend this data structure to allow stopping specific files
    static boolean stopped = false;
    static int filesBeingPlayed = 0;

    private static void testPlay(String filename, boolean repeat) {
        try {
            while(stopped && filesBeingPlayed > 0) { // in case playback is currently stopped, wait till all files stopped playing before setting stopped to false again
                Thread.sleep(100);
            }
            stopped = false;

            File file = new File(filename);
            filesBeingPlayed++;
            do {
                // Get AudioInputStream from given file.
                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                AudioInputStream din = null;
                if (in != null) {
                    AudioFormat baseFormat = in.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false);
                    // Get AudioInputStream that will be decoded by underlying VorbisSPI
                    din = AudioSystem.getAudioInputStream(decodedFormat, in);
                    // Play now !
                    rawplay(decodedFormat, din);
                    in.close();
                }
            } while (repeat && !stopped);
        } catch (FileNotFoundException e) {
            System.err.println("VorbisPlayer: " + filename + " could not be found: " + e.getLocalizedMessage());
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Vorbis Player: " + filename + " is no valid ogg vorbis audio file (is lib/ in your class path?): " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            filesBeingPlayed--;
        }
    }

    private static void rawplay(AudioFormat targetFormat,
                                AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1 && !stopped) {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    /**
     * Plays the given ogg vorbis file and returns to the caller.
     * Use {@code VorbisPlayer.play("resources/audio/BGM/sample.ogg", true);} to play a background music,
     * {@code VorbisPlayer.play("resources/audio/SFX/sample.ogg", false);} for sound effects. If a new file is played,
     * {@link #stopped} is set to {@code false}.
     * @param file the path of the file
     * @param repeat whether to repeat the file
     */
    public static void play(String file, boolean repeat) {
        Thread t = new Thread(() -> testPlay(file, repeat));
        playerThreads.add(t);
        t.start();
    }

    /**
     * Stops the playback of all files.
     */
    public static void stop() {
        stopped = true;
    }
}
