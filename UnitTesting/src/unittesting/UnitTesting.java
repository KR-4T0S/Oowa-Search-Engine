package unittesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class UnitTesting {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       System.out.println((int)'\n');
    }
    
    public static String stemmer(String str) {
        SnowballStemmer snowballStemmer = new englishStemmer();
        snowballStemmer.setCurrent(str);
        snowballStemmer.stem();
        String result = snowballStemmer.getCurrent();
        
        return result;
    }    

    public static String removeNonAlphanum(String str) {
            String result = str;
                       
            // Iterate from left
            int stopLeft = 0;
            for (int i = 0; i < result.length(); i++) {
                // stop once first alphanum character is found
                if (Character.isLetter(result.charAt(i)) || 
                       Character.isDigit(result.charAt(i))) {
                    break;
                } else {
                    stopLeft++; 
                }
            }
            result = result.substring(stopLeft);
            
            // Iterate from right
            int stopRight = 0;
            for (int i = result.length() - 1; i >= 0; i--) {
                // stop once first alphanum character is found
                if (Character.isLetter(result.charAt(i)) || 
                       Character.isDigit(result.charAt(i))) {
                    break;
                } else {
                    stopRight++;
                }
            }
            result = result.substring(0, (result.length() - 1) - (stopRight - 1));
            
            return result;
    }
    
    public static List<String> removeHyphen(String str) {

        List<String> result = new ArrayList<>(Arrays.asList(str.split("-")));
        if (result.size() != 1) {
            result.add(str.replaceAll("-", ""));
        }
            
        return result;
    }
    
    public static List<String> toLower(List<String> list) {
        
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).toLowerCase());
        }
        
        return list;
    }
}
