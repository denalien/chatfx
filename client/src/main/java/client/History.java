package client;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class History {
    private File location;
    private ArrayList<String> contents = new ArrayList<>();

    public History(String nickname) {
        this.location = new File(String.format("client/src/main/resources/history_%s.txt",nickname));
        if(!location.exists()){
            try {
                location.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> read(){

        try(BufferedReader reader = new BufferedReader(new FileReader(location))){
            String str;
            while((str = reader.readLine()) != null){
                contents.add(str);
            }
            if(contents.size() > 100){
               (contents.subList(0, contents.size() - 100)).clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public void add(String msg){
        contents.add(msg);
    }

    public void write(){
        if(contents.size() > 100){
            contents.subList(0,100).clear();
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(location,true))){
            for (String str: contents
                 ) {
                writer.write(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
