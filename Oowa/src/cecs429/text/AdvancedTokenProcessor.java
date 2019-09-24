package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedTokenProcessor implements TokenProcessor {
    
	@Override
	public List<String> processToken(String token) {
            String _token = token;
            List<String> result = new ArrayList();
            
            // TODO:    remove non-alphanumeric characters from start & end of string
            //          (!He,llo. => He,llo) (192.186.1.1 => 192.186.1.1)
            _token = _token.replaceAll("\\W()\\W", "");
            
            // TODO:    remove all apostrophies and quotation marks
            _token = _token.replaceAll("\'|\"", "");
            
            // TODO:    if hyphenated: split AND remove hyphens (turn to single word)
            result = Arrays.asList(_token.split("-"));
            result.add(_token.replaceAll("-", ""));
            
            // TODO:    to lowercase
            for (String str : result) {
                str = str.toLowerCase();
            }
            
            // TODO:    stem using Porter2 stemmer
            return null;
	}

     
    
}
