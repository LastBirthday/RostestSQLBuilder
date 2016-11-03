package com.home;

import java.io.*;
import java.util.Properties;

/**
 * Created by Dds on 02.04.2016.
 */


public class Queries {

    public static final String propFileName = System.getProperty("user.dir") + System.getProperty("file.separator") + "queries.properties";
    private static Properties props;

    public static Properties getPropertyFile() {

        try {
            InputStream is = new FileInputStream(propFileName);

            //singleton
            if(props == null){
                props = new Properties();
                props.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;

    }

    public static String getQuery(String query) {
        return getPropertyFile().getProperty(query);
    }

}

