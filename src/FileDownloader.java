import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Fuad Huseynov on 10.11.2016.
 */
public class FileDownloader {

    public static void main(String[] args) {

        //No range input is given
        if (args.length == 1) {
            String indexFileURL = args[0];
            System.out.println("URL of the index file: " + indexFileURL);
            System.out.println("No range is given");

            try {
                //Create URL Object
                URL url = new URL(indexFileURL);

                //Create connection with the server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {    //Response Code is '200 OK' for the index file
                    System.out.println("Index file is downloaded");

                    //Count the number of lines
                    int count = 0;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    ArrayList<String> urlList = new ArrayList<>();  //This list will store the URLs from the index file
                    while ((line = br.readLine()) != null) {
                        count++;
                        urlList.add(line);
                    }
                    System.out.println("There are " + count + " files in the index");

                    //Download the files one by one
                    /*
                    * For each url in the ArrayList:
                    * --Check if it exists
                    * --Get the content of the file
                    * --Create a new file in your directory with the same name
                    * --Write the content of the file in the URL to your file and save it
                    * --Go to the next URL
                    * */
                    HttpURLConnection connection;
                    int file_size;
                    String file_name;

                    for(int i = 0; i < urlList.size(); i++) {
                        //Send HEAD request to the URL
                        url = new URL("http://" + urlList.get(i));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {    //Response is 200 OK for the URL IN THE INDEX FILE
                            file_size = connection.getContentLength();  //Get the length of the file
                            //File exists --> send GET request to the URL
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            file_name = urlList.get(i).substring(urlList.get(i).lastIndexOf('/') + 1, urlList.get(i).length());
                            BufferedWriter bw = new BufferedWriter(new FileWriter(file_name));

                            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while((line = br.readLine()) != null) {
                                bw.write(line);
                                bw.newLine();
                            }
                            bw.close();
                            System.out.println(i + 1 + ") " + urlList.get(i) + " (size = " + file_size + ") is downloaded");
                        } else {    //Response is other than 200 OK for the URL IN THE INDEX FILE
                            System.out.println(i + 1 + ") " + urlList.get(i) + " is not found");
                        }
                        br.close();
                    }
                }
                else {    //Response Code is other than '200 OK' for the index file
                    System.out.println("Requested file is not found");
                }
            } catch (MalformedURLException e) {
                System.out.println("Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO Exception: " + e.getMessage());
            }

        }

        //Range is given as a command line argument
        else if (args.length == 2){
            String indexFileURL = args[0];
            String range = args[1];
            String lowerEndPoint = range.substring(0, range.indexOf('-'));
            String upperEndPoint = range.substring(range.indexOf('-') + 1, range.length());

            System.out.println("URL of the index file: " + indexFileURL);
            System.out.println("Lower endpoint = " + lowerEndPoint);
            System.out.println("Upper endpoint = " + upperEndPoint);

            try {
                //Create URL Object
                URL url = new URL(indexFileURL);

                //Create connection with the server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {    //Response Code is '200 OK' for the index file
                    System.out.println("Index file is downloaded");

                    //Count the number of lines
                    int count = 0;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    ArrayList<String> urlList = new ArrayList<>();  //This list will store the URLs from the index file
                    while ((line = br.readLine()) != null) {
                        count++;
                        urlList.add(line);
                    }
                    System.out.println("There are " + count + " files in the index");

                    //Download the files one by one
                    /*
                    * For each url in the ArrayList:
                    * --Check if it exists
                    * --Check if there are enough bytes for the given range in the file
                    * --Get the content of the file
                    * --Create a new file in your directory with the same name
                    * --Write the content of the file in the URL to your file and save it
                    * --Go to the next URL
                    * */
                    HttpURLConnection connection;
                    int file_size;
                    String file_name;

                    for(int i = 0; i < urlList.size(); i++) {
                        //Send HEAD request to the URL
                        url = new URL("http://" + urlList.get(i));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {    //Response is 200 OK for the URL IN THE INDEX FILE
                            file_size = connection.getContentLength();  //Get the length of the file
                            //Check if there are enough bytes
                            if (file_size < Integer.parseInt(lowerEndPoint)) {  //The size of the file is less than given lowerend
                                System.out.println(i + 1 + ") " + urlList.get(i) + " (size = " + file_size + ") is not downloaded");
                            } else {    //The size of the file is not less than the given lowerend - can download
                                //File exists --> send GET request to the URL
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setRequestProperty("Range", "bytes=" + lowerEndPoint + "-" + upperEndPoint);
                                file_name = urlList.get(i).substring(urlList.get(i).lastIndexOf('/') + 1, urlList.get(i).length());
                                BufferedWriter bw = new BufferedWriter(new FileWriter(file_name));

                                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                while((line = br.readLine()) != null) {
                                    bw.write(line);
                                    bw.newLine();
                                }
                                bw.close();
                                if (file_size < Integer.parseInt(upperEndPoint))
                                    System.out.println(i + 1 + ") " + urlList.get(i) + " (range = " + lowerEndPoint + "-" + file_size + ") is downloaded");
                                else
                                    System.out.println(i + 1 + ") " + urlList.get(i) + " (range = " + lowerEndPoint + "-" + upperEndPoint + ") is downloaded");
                            }
                        } else {    //Response is other than 200 OK for the URL IN THE INDEX FILE
                            System.out.println(i + 1 + ") " + urlList.get(i) + " is not found");
                        }
                        br.close();
                    }
                }
                else {    //Response Code is other than '200 OK' for the index file
                    System.out.println("Requested file is not found");
                }
            } catch (MalformedURLException e) {
                System.out.println("Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO Exception: " + e.getMessage());
            }
        }

    }

}
