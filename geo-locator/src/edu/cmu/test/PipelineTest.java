package edu.cmu.test;

import java.io.BufferedReader;
import java.util.List;

import twitter4j.Status;
import twitter4j.json.DataObjectFactory;
import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.coder.CoderFactory;
import edu.cmu.geolocator.io.GetReader;
import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.parser.ParserFactory;

public class PipelineTest {

  public static void main(String args[]) throws Exception {

    // If you have changed where your GazIndex lies, you have to specify where GazIndex lies
    // by using the following:
    GlobalParam.setGazIndex("/users/indri/GazIndex");

    // The path for the file that you want to parse, one line per tweet JSON file.
    String path = "/Users/Indri/Desktop/dataForDisambiguation/downloads-preferedCountries.txt";
    BufferedReader br = GetReader.getUTF8FileReader(path);
    String line = null;
    while ((line = br.readLine()) != null) {

      // create Tweet Status from the JSON file. Status is the structure containing all the
      // information in tweet.
      Status status = DataObjectFactory.createStatus(line);
      // create the tweet object from the status.
      Tweet tweet = new Tweet(status);
//      Tweet tweet = new Tweet("I was born in Harbin. But I am in Pittsburgh right now.");

      // If you are not reading JSON, and just parse the string, you should use the following to
      // wrap a sentence in a tweet:
      // Tweet tweet = new Tweet("sentence");

      System.out.println("////////////////////////////////////////////////////////////////\n"
              + tweet.getText());

      // generate the parsed toponyms from the tweet.
      List<LocEntityAnnotation> topos = ParserFactory.getEnAggrParser().parse(tweet);

      // print the extracted toponyms
      for (LocEntityAnnotation topo : topos)
        System.out.println("Topo is:" + topo.getTokenString() + " " + topo.getNEType());

      
      List<CandidateAndFeature> resolved = null;
      if (topos == null)
        System.out.println("Can not parse. No topos Found.");
      else {
        System.out.println("Start to resolve places ..");
        long previous = System.currentTimeMillis();
        
        // resolve the place
        resolved = CoderFactory.getMLGeoCoder().resolve(tweet, "debug");
        System.out.println("time Spent is :" + (System.currentTimeMillis() - previous));
        if (resolved == null)
          System.out.println("No resolve result. OOG.");
        else {
          System.out.println("Resolution results:");
          
          //Note that we could output multiple results for one place. 
          //This is for the user to decide which is the best they want.
          // We may improve this later to output only one result.
          for (CandidateAndFeature c : resolved) {
            System.out.println(c.getAsciiName() + " " + c.getLe().getNEType() + " "
                    + c.getCountryCode() + " " + c.getAdm1Code() + " " + c.getLatitude() + " "
                    + c.getLongitude());
          }
        }
      }
    }
  }
}
