package testEnglishWords;

import jaco.mp3.player.MP3Player;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Main {

    //API key SQX8P1xEogXzO2NgLKtdeVZhoqRX0YAgBzbng487xt9Co9hZfHVt8BaCwl61vo3L

    static List<String> listEnglishWords = new ArrayList<String>();
    static List<String> listRussianWords = new ArrayList<String>();
    static List<SoundList> listEnglishSounds = new ArrayList<SoundList>();
    static String APIKEY = "SQX8P1xEogXzO2NgLKtdeVZhoqRX0YAgBzbng487xt9Co9hZfHVt8BaCwl61vo3L";
    static String baseUrl = "https://dictionary.cambridge.org";


    private static void getDictionary() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("words");
        Scanner input = new Scanner(fis, "UTF-8");
        String str;
        while (input.hasNextLine()) {
            int lastEnglishLetter = 0;
            str = input.nextLine();
            int index = -1;
            for (int i = 0; i < str.length(); i++) {
                if ((str.charAt(i) >= 'a' && str.charAt(i) <= 'z')
                        || (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')) {
                    lastEnglishLetter = i;
                }
                if ((str.charAt(i) >= 'а' && str.charAt(i) <= 'я') || (str.charAt(i) >= 'А' && str.charAt(i) <= 'Я')) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                System.out.println("can't find Russian words in " + index + " line");
            } else {
                listEnglishWords.add(str.substring(0, lastEnglishLetter + 1));
                listRussianWords.add(str.substring(index, str.length()));
            }
        }
        input.close();
    }

    static MP3Player downloadEnglishSound(String word) throws SkPublishAPIException, JSONException, UnsupportedAudioFileException, IOException, LineUnavailableException {
        ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager();
        DefaultHttpClient httpClient = new DefaultHttpClient(threadSafeClientConnManager);
        SkPublishAPI api = new SkPublishAPI(baseUrl + "/api/v1", APIKEY, httpClient);

        api.setRequestHandler(new SkPublishAPI.RequestHandler() {
            public void prepareGetRequest(HttpGet request) {
                request.setHeader("Accept", "application/json");
            }
        });

        String dictCode = "british";
        try {
            JSONObject bestMatch = new JSONObject(api.searchFirst(dictCode, word, "html"));
            String string = bestMatch.toString();
            int finish = string.indexOf(".mp3") + 4;
            int start = finish - 1;
            while (true) {
                if (start < 0) {
                    System.out.println("error, can't find https");
                    return null;
                }
                String tmp = string.substring(start, start + 5);
                if (tmp.equals("https")) {
                    break;
                }
                start--;
            }
            String link = string.substring(start, finish);
            return playSound(link);
        } catch (SkPublishAPIException e) {
            System.out.println("didn't find sound for word: " + word);
        }
        return null;
    }

    String GetMp3FromString (String page) { //TODO
        return null;
    }

    static MP3Player downloadRussianSound(String word) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        String link = "http://translate.google.com/translate_tts?ie=utf-8&tl=ru&q=" + word;
        return playSound(link);
    }

    public static synchronized MP3Player playSound(final String link) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        return new MP3Player(new URL(link));
    }

    private static void runTest() throws UnsupportedAudioFileException, SkPublishAPIException, LineUnavailableException, JSONException, IOException, InterruptedException {
        int cntOfWords = listEnglishWords.size();
        Scanner input = new Scanner(System.in, "UTF-8");
        int prevIndex = -1;
        Random random = new Random(System.nanoTime());
        ArrayList<Integer> cntOfAnswerWord = new ArrayList<Integer>(Collections.nCopies(cntOfWords, 0));
        while (true) {
            int index = Math.abs((random.nextInt()) % cntOfWords);
            if (index == prevIndex) {
                continue;
            }
            if (cntOfAnswerWord.get(index) >= 2) {
                continue;
            }
            prevIndex = index;
            //System.out.println("write translate - " + listRussianWords.get(index));
            System.out.println(listRussianWords.get(index));
            String answer;
            try {
                answer = input.nextLine();
            } catch (NoSuchElementException e) {
                return;
            }
            //answer = "abc";
            if (answer.compareTo(listEnglishWords.get(index)) == 0) {
                cntOfAnswerWord.set(index, cntOfAnswerWord.get(index) + 1);
            } else {
                cntOfAnswerWord.set(index, cntOfAnswerWord.get(index) - 3);
                System.out.println("Right answer was " + listEnglishWords.get(index));
            }
            try {
                listEnglishSounds.get(index).playAll();
            } catch (NullPointerException e) {
                System.out.println("can't find sound");
            }
            //downloadRussianSound(listRussianWords.get(index));
            System.out.println(cntOfAnswerWord.get(index) + " from " + 2);
            //Thread.sleep(1000);
        }
    }

    public static void getAllSounds() throws UnsupportedAudioFileException, SkPublishAPIException, LineUnavailableException, JSONException, IOException {
        int cnt = 0;
        for (String englishWord : listEnglishWords) {
            String words[] = englishWord.split(" ");
            SoundList soundList = new SoundList();
            for (String word : words) {
                soundList.add(downloadEnglishSound(word));
            }
            listEnglishSounds.add(soundList);
            System.out.println(cnt++ + "/" + listEnglishWords.size());
        }
    }

    public static void main(String[] args) throws IOException, SkPublishAPIException, JSONException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        getDictionary();
        getAllSounds();
        runTest();
        /*
        String s = "ЛОВКОВ";
        int x = (s.hashCode() & 0x7fffffff) % 3 + 1;
        System.out.println(x);
        */
    }
}
