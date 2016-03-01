package testEnglishWords;

import jaco.mp3.player.MP3Player;

import java.util.ArrayList;

/**
 * Created by charm on 14.04.2015.
 */
public class SoundList {
    private ArrayList<MP3Player> listOfSounds;

    public SoundList() {
        listOfSounds = new ArrayList<MP3Player>();
    }

    public void add(MP3Player mp3) {
        listOfSounds.add(mp3);
    }

    public ArrayList<MP3Player> getAll() {
        return listOfSounds;
    }

    public void playAll() throws InterruptedException {
        for (int i = 0; i < listOfSounds.size(); i++) {
            listOfSounds.get(i).play();
            if (i != listOfSounds.size() - 1) {
                Thread.sleep(1200);
            }
        }
    }
}
