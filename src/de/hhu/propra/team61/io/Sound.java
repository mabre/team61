package de.hhu.propra.team61.io;


import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dinii on 26.06.14.
 */
public class Sound {

    private Thread loopThread;
    public SourceDataLine outputLine = null;
    private int volume;
    private boolean mute;

    public Sound(Thread loop){
        this.loopThread = loop;
    }

    /**
     * Plays the given sound effect (SFX)
     * @param sfxName is the name of the played SoundEffect.
     */
    public void playSFX(InputStream sfxName){

    }

    /**
     * Plays the given background music (BGM)
     * @param bgmName is the name of the played SoundEffect.
     */
    public void playBGM(InputStream bgmName){

    }

    /**
     * Close loop.
     */
    public void close(){
        Thread closeThread = loopThread;
        if (closeThread != null) {
            if (outputLine != null){
                outputLine.close();
            }
            closeThread.interrupt();
            loopThread = null;
        }
    }



    /**
     * Set the volume.
     * @param volume the volume 0...100
     */
    public void setVolume(int volume) {
        this.volume = volume;
        if (outputLine != null) {
            int vol;
            if (mute) vol = 0;
            else vol = volume;
            AudioThread.setVolume(outputLine, vol);


}
