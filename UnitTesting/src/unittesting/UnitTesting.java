package unittesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnitTesting {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String _token = "192.168.0.1";
            List<String> result = new ArrayList();
            
            // TODO:    remove non-alphanumeric characters from start & end of string
            //          (!He,llo. => He,llo) (192.186.1.1 => 192.186.1.1)
            _token = _token.replaceAll("(\\W+)(.)(\\W+)", "");
            
            // TODO:    remove all apostrophies and quotation marks
            _token = _token.replaceAll("\'|\"", "");
            
            // TODO:    if hyphenated: split AND remove hyphens (turn to single word)
            result.addAll(Arrays.asList(_token.split("-")));
            result.add(_token.replaceAll("-", ""));
            
            // TODO:    to lowercase
            for (String str : result) {
                str = str.toLowerCase();
            }
            
            System.out.println(result.toString());
    }
    
    
}
