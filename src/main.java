import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class main {

    private static String cipher = "cot fvj pow bvzztz cot ztjctjnt zowrhg zpyjx cot zpwdg";

    public static void main(String[] args) {
        //Load all words from text file in to words array
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("google-10000-english-no-swears.txt"))) {
            while (br.ready()) {
                words.add(br.readLine().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //send a message to rose with the word cookie and your team name to get the next clue
        List<String> cipherWords = new ArrayList<>(Arrays.asList(cipher.split(" ")));


        HashMap<Character, List<String>> charOccurrences = new HashMap<>();
        //Add all words where a character occurs to a list then map that list to the character
        for (String cipherWord : cipherWords) {
            for (Character ch : cipherWord.toCharArray()) {
                for (String cipherWord2 : cipherWords) {
                    if (cipherWord2.contains(ch.toString())) {
                        if (charOccurrences.get(ch) == null) {
                            charOccurrences.put(ch, (new ArrayList<>(Collections.singletonList(cipherWord2))));
                        } else {
                            if (!charOccurrences.get(ch).contains(cipherWord2)) {
                                charOccurrences.get(ch).add(cipherWord2);
                            }
                        }
                    }
                }
            }
        }
        HashMap<Character, List<Character>> possibleChars = new HashMap<>();
        //Map all characters to a list of all characters which it could possibly be
        for (Map.Entry<Character, List<String>> entry : charOccurrences.entrySet()) {
            Character key = entry.getKey();
            List<String> value = entry.getValue();

            List<List<Character>> lists = new ArrayList<>();

            for (String string : value) {
                List<Character> characterList = new ArrayList<>();
                for (String patternMatch : getMatches(string, words)) {
                    if (!characterList.contains(patternMatch.charAt(string.indexOf(key)))) {
                        characterList.add(patternMatch.charAt(string.indexOf(key)));
                    }
                }
                lists.add(characterList);
            }
            possibleChars.put(key, findDupes(lists));
        }

        HashMap<Character, Character> keyMap = new HashMap<>();

        removeCharacter(possibleChars, keyMap, true);


        //Loop a bunch of times to eliminate things that characters cant be
        for (int x = 0; x < 10; x++) {
            HashMap<Character, List<Character>> tempMap = new HashMap<>(possibleChars);
            possibleChars.clear();
            for (Map.Entry<Character, List<String>> entry : charOccurrences.entrySet()) {
                Character key = entry.getKey();
                List<String> value = entry.getValue();

                List<List<Character>> lists = new ArrayList<>();

                for (String string : value) {
                    List<Character> characterList = new ArrayList<>();
                    for (String patternMatch : getMatchesWithLetters(string, keyMap, tempMap, words)) {
                        if (!characterList.contains(patternMatch.charAt(string.indexOf(key)))) {
                            characterList.add(patternMatch.charAt(string.indexOf(key)));
                        }
                    }
                    lists.add(characterList);
                }
                possibleChars.put(key, findDupes(lists));
            }

            keyMap.clear();

            removeCharacter(possibleChars, keyMap, true);

        }

        System.out.println(cipher);
        //print with a lil bit of frequency analysis to pick most common word if word has multiple possible results
        for (String str : cipherWords) {
            String temp = "";
            List<String> matchedWords = getMatchesWithLetters(str, keyMap, possibleChars, words);
            if (matchedWords.size() > 1) {
                int highest = 0;
                for (int x = 1; x < matchedWords.size(); x++) {
                    if (words.indexOf(matchedWords.get(highest)) > words.indexOf(matchedWords.get(x))) {
                        highest = x;
                    }
                }
                temp = matchedWords.get(highest);
            }

            if (matchedWords.size() == 1) { //if there only 1 possibility print it.
                System.out.print(matchedWords.get(0));
                System.out.print(" ");
            } else { //if there's more than 1 print the most common one.
                System.out.print(temp);
                System.out.print(" ");
            }
        }
    }

    private static void removeCharacter(HashMap<Character, List<Character>> possibleChars, HashMap<Character, Character> keyMap, boolean removeChar) {
        while (removeChar) {
            removeChar = false;
            for (Map.Entry<Character, Character> entry : keyMap.entrySet()) {
                Character key = entry.getKey();
                Character value = entry.getValue();

                if (value != null) {
                    for (Map.Entry<Character, List<Character>> entry1 : possibleChars.entrySet()) {
                        Character key1 = entry1.getKey();
                        //List<Character> value1 = entry1.getValue();

                        if (key != key1) {
                            possibleChars.get(key1).remove(value);
                        }
                    }
                }
            }
            for (Map.Entry<Character, List<Character>> entry1 : possibleChars.entrySet()) {
                Character key = entry1.getKey();
                List<Character> value = entry1.getValue();

                if (value.size() == 1) {
                    if (keyMap.get(key) == null) {
                        keyMap.put(key, value.get(0));
                        removeChar = true;
                    }
                } else {
                    keyMap.put(key, null);
                }
            }
        }
    }

    private static List<String> getMatchesWithLetters(String input, HashMap<Character, Character> map, HashMap<Character, List<Character>> possibleChars, List<String> words) {
        List<String> output = new ArrayList<>();
        for (String str : getMatches(input, words)) {
            boolean flag = true;

            for (Map.Entry<Character, Character> entry : map.entrySet()) {
                Character key = entry.getKey();
                Character value = entry.getValue();

                if (value != null) {
                    if (input.indexOf(key) >= 0) {
                        if (str.charAt(input.indexOf(key)) != value) {
                            flag = false;
                        }
                    }
                } else {
                    for (int x = 0; x < str.length(); x++) {
                        if (!possibleChars.get(input.charAt(x)).contains(str.charAt(x))) {
                            flag = false;
                        }
                    }
                }
            }
            if (flag) {
                output.add(str);
            }
        }
        if (output.size() == 0) {
            return getMatches(input, words);
        }
        Set<String> set = new HashSet<>(output);
        output.clear();
        output.addAll(set);
        return output;
    }

    private static List<Character> findDupes(List<List<Character>> lists) {
        if (lists.size() == 0) {
            return new ArrayList<>();
        } else if (lists.size() == 1) {
            return lists.get(0);
        } else {
            for (int x = 1; x < lists.size(); x++) {
                lists.get(x).retainAll(lists.get(0));
                lists.get(0).retainAll(lists.get(x));
            }
            return lists.get(0);
        }
    }

    private static String getPattern(String input) {
        int nextNum = 0;
        StringBuilder output = new StringBuilder();
        HashMap<Character, Integer> existingChars = new HashMap<>();
        for (char ch : input.toCharArray()) {
            if (existingChars.get(ch) == null) {
                existingChars.put(ch, nextNum);
                nextNum++;
            }
            output.append(existingChars.get(ch));
        }
        return output.toString();
    }

    private static List<String> getMatches(String input, List<String> words) {
        List<String> output = new ArrayList<>();

        for (String word : words) {
            if (input.length() == word.length()) {
                if (getPattern(input.toLowerCase()).equals(getPattern(word.toLowerCase()))) {
                    if (isLetters(word.toLowerCase())) {
                        output.add(word.toLowerCase());
                    }
                }
            }
        }
        return output;
    }

    private static boolean isLetters(String input) {
        return input.matches("[a-z]+");
    }
}
