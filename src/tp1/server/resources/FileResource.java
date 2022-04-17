package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import tp1.api.service.rest.RestFiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileResource implements RestFiles {

    private final Map<String, byte[]> files = new HashMap<>();

    private static Logger Log = Logger.getLogger(FileResource.class.getName());

    private Discovery d;

    public FileResource(Discovery d) {
        this.d = d;
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : fileId = " + fileId + "; data = "+data+ "; token = " + token);

        // Check if fileId is valid
        if(fileId == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        try {
            FileWriter file = new FileWriter(fileId);
            file.write(data.toString());
            file.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


        // Check if token is valid
       /* if(token == null) {
            Log.info("token is invalid.");
            throw new WebApplicationException( Status.FORBIDDEN);
        }*/


       //Add data to the map of file's data
       //files.put(fileId, data);
    }

    @Override
    public void deleteFile(String fileId, String token) {
        Log.info("deleteFile : fieldId = " + fileId+ "; token = "+token);

        // Check if fileId is valid
        if(fileId == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        /*if(!files.containsKey(fileId)){
            Log.info("fileId does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }*/

        File file = new File(fileId);

        //file.delete();


        if(!file.delete()){
            Log.info("fileId does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        // Check if token is valid
        /*if(token == null) {
            Log.info("token is invalid.");
            throw new WebApplicationException( Status.FORBIDDEN );
        }*/

        //deletes the data from the map
        //files.remove(fileId);
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        Log.info("getFile :  fieldId = " + fileId+ "; token = "+token);

        // Check if fileId is valid
        if(fileId == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        File f = new File(fileId);

        if(!f.exists()){
            Log.info("fileId does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }
        /*
        if(!files.containsKey(fileId)){
            Log.info("fileId does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }*/

        try {

            byte[] content = Files.readAllBytes(Path.of(f.getAbsolutePath()));
            return content;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Check if token is valid
        /*if(token == null) {
            Log.info("token is invalid.");
            throw new WebApplicationException( Status.FORBIDDEN );
        }*/


    }
}
