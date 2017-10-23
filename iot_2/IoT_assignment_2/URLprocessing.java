import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.*;

public class URLprocessing {

  public interface URLhandler {
    void takeUrl(String url);
  }

  public static URLhandler handler = new URLhandler() {
    public void takeUrl(String url) {
      System.out.println(url);
    }
  };

    /**
    * Parse the given buffer to fetch embedded links and call the handler to
    * process these links.
    * 
    * @param data
    *          the buffer containing the http document
    */
    public static void parseDocument(CharSequence data) {
    // call handler.takeUrl for each matched url

    String regex = "<[aA]\\s(.*?)[hH][rR][eE][fF]\\s*=\\s*[\"'][^\"']+[\"'][^>]*>";
    Matcher m = Pattern.compile(regex).matcher(data);
    String href_regex ="[\"'][^\"']+[\"']";
    String http_checker = "[hH][tT]{2}[pP]:[/][/]";


        while (m.find()) {
          // Get every a tag in HTML
            String current_a_tag = m.group();
            Matcher m_href = Pattern.compile(href_regex).matcher(current_a_tag);
            while (m_href.find()) {
                // Get every href in tag
                String current_url = m_href.group();
                current_url = current_url.substring(1, current_url.length() - 1);

                // check if http protocol in href
                Matcher m_http_checker = Pattern.compile(http_checker).matcher(current_url);
                if (m_http_checker.find()) {
                // validade if MyURL can be created, otherwise ignore
                    try {
                        MyURL url = new MyURL(current_url);
                        handler.takeUrl(current_url);
                    } catch (Exception e){
                        // If the URL could not be fetched, do nothing
                    } 
                }

            }

        }

    }

}
