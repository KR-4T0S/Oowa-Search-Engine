package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class AdvancedTokenProcessor implements TokenProcessor {

    @Override
    public List<String> processToken(String token) {
        List<String> result = new ArrayList();

        // TODO:    remove non-alphanumeric characters from start & end of string
        //          (!He,llo. => He,llo) (192.186.1.1 => 192.186.1.1)
        // TODO:    remove all apostrophies and quotation marks
        token = removeQuote(removeNonAlphanum(token));
            
        // TODO:    if hyphenated: split AND remove hyphens (turn to single word)
        result = removeHyphen(token);

        // TODO:    to lowercase
        result = toLower(result);

        // TODO:    stem using Porter2 stemmer
        result = snowballStemmer(result);
        

        return result;
    }

    private List<String> snowballStemmer(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            SnowballStemmer snowballStemmer = new englishStemmer();
            snowballStemmer.setCurrent(list.get(i));
            snowballStemmer.stem();
            list.set(i, snowballStemmer.getCurrent());
        }

        return list;
    }

    private List<String> toLower(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).toLowerCase());
        }

        return list;
    }

    private List<String> removeHyphen(String str) {
        List<String> result = new ArrayList<>(Arrays.asList(str.split("-")));
        if (result.size() != 1) {
            result.add(str.replaceAll("-", ""));
            for (int i = 0; i < result.size(); i++) {
                result.set(i, removeNonAlphanum(result.get(i)));
            }
        } 

        List<String> temp = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i).isEmpty()) {
                temp.add(result.get(i));
            }
        }
        result = temp;
        
        return result;
    }

    private String removeQuote(String str) {
        return str.replaceAll("\'|\"|”|’", "");
    }

    private String removeNonAlphanum(String str) {
        // Iterate from left
        int stopLeft = 0;
        for (int i = 0; i < str.length(); i++) {
            // stop once first alphanum character is found
            if (Character.isLetter(str.charAt(i))
                    || Character.isDigit(str.charAt(i))) {
                break;
            } else {
                stopLeft++;
            }
        }
        
        if (stopLeft < str.length()) {
            str = str.substring(stopLeft);
        }
        

        // Iterate from right
        int stopRight = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            // stop once first alphanum character is found
            if (Character.isLetter(str.charAt(i))
                    || Character.isDigit(str.charAt(i))) {
                break;
            } else {
                stopRight++;
            }
        }
        
        if (stopRight < str.length()) {
            str = str.substring(0, (str.length() - 1) - (stopRight - 1));
        }
        

        return str;
    }

}
