package FileReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {

    public static void main(String[] args) {
        Service.run(args);
    }

    private static class Service {

        private static final String TEXT_URL = "http://www.gutenberg.org/files/84/84-0.txt";

        private static void run(String[] args) {
            printResponse(getWordFrequencyMap(), args);
        }

        private static LinkedHashMap getWordFrequencyMap() {
            LinkedHashMap<String, Long> map;
            try {
                URL url = new URL(TEXT_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/text");
                if (con.getResponseCode() != 200) {
                    throw new RuntimeException("Connection failed. HTTP Error code: "
                            + con.getResponseCode());
                }
                InputStreamReader in = new InputStreamReader(con.getInputStream());
                BufferedReader br = new BufferedReader(in);
                map = getMapFromStream(br.lines());
                con.disconnect();
                return map;

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }

        private static LinkedHashMap getMapFromStream(Stream<String> stream) {
            return stream
                    .flatMap(Pattern.compile("[^a-zA-Z|-]")::splitAsStream)
                    .filter(el -> el != null && !el.trim().isEmpty() && !el.equals(""))
                    .map(x -> x.toLowerCase())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }

        private static void printResponse(LinkedHashMap map, String[] args) {
            if (args.length > 0) {
                if (args.length > 1)
                    System.out.println("There is more than one argument, taking the first one.");

                String word = args[0];
                if (map.containsKey(word))
                    System.out.println("The number of occurrences of the word \"" + word + "\" are: " + map.get(word.toLowerCase()).toString() + ".");
                else
                    System.out.println("There are no occurrences of this word in the book.");
            } else {
                ArrayList<String> mapKeySetArray = new ArrayList<>(Arrays.asList((String[]) map.keySet().toArray(new String[map.keySet().toArray().length])));
                System.out.println("The most common words are: " + mapKeySetArray.get(0) + ", " + mapKeySetArray.get(1) + ", " + mapKeySetArray.get(2) + ".");
            }
        }
    }
}

//Python:
//make sure to install requests and re
//usage: python FileReader.py or python FileReader.py {optional-word}

 /*
import requests
import re
from collections import Counter
import sys

url = 'http://www.gutenberg.org/files/84/84-0.txt'
response = requests.get(url)
c = Counter(list(filter(None, re.split("[^a-zA-Z|-]", response.text.lower()))))
if len(sys.argv) > 1:
    print("The number of times \"" + str(sys.argv[1]) + "\" is mentioned is: " + str(c[str(sys.argv[1])]))
else:
    print("The top 3 words are: " + str(c.most_common(3)))
 */
