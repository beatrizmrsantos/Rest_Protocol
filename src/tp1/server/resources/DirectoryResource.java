package tp1.server.resources;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Result;
import tp1.client.RestFilesClient;
import tp1.client.RestUsersClient;

public class DirectoryResource implements RestDirectory {

    private final Map<String, ArrayList<FileInfo>> userfiles = new HashMap<>();

    private FileResource f;
    private UsersResource u;
    private Discovery d;

    private static Logger Log = Logger.getLogger(DirectoryResource.class.getName());

    public DirectoryResource(Discovery d) {
        this.d = d;
        d.listener();
    }


    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("writeFile : filename = " + filename + "; data = " + data + "; userId = " + userId + "; password = " + password);

        this.getUser(userId, password);

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        FileInfo file = this.getFileFromUser(userId, filename);

        URI[] uris = d.knownUrisOf("files");

        //se o ficheiro ja existe vai ao servidor e escreve por cima se nao cria ficheiro
        if(file != null){
            int number = this.getServerURIofFile(uris, userId, filename);

            RestFilesClient files = new RestFilesClient(uris[number]);

            String name = String.format("%s.%s", userId, filename);
            files.writeFile(name, data, "");

            return file;

        } else {
            int number = (int)Math.floor(Math.random()*(uris.length - 1));

            RestFilesClient files = new RestFilesClient(uris[number]);

            //randomServer(f, filename, data);

            String name = String.format("%s.%s", userId, filename);

          //  System.out.println("discovery data" + new String(data));

            files.writeFile(name, data,"");

            String uri = uris[number].toString();
            String uriComplete = uri.concat("/files/" + userId + "." + filename);

            HashSet<String> set = new HashSet<>();
            FileInfo i = new FileInfo(userId, filename, uriComplete, set);

            if(!userfiles.containsKey(userId)){
                userfiles.put(userId, new ArrayList<>());
            }

            userfiles.get(userId).add(i);

            return i;
        }
    }

    private void printURI(URI[] uris){
        System.out.println("uris do arrayURI: ");
        for (URI u: uris) {
            System.out.print(u.toString() + ",   ");
        }

        System.out.println();
    }


    private void randomServer(URI[] f, String filename, byte[] data){
        int number = (int)Math.floor(Math.random()*(f.length));

        for(int i = 0; i < 3 ;i++) {
            RestFilesClient files = new RestFilesClient(f[number]);

            try {
                files.writeFile(filename, data, null);
                break;
            }catch (Exception x){
                number = (int)Math.floor(Math.random()*(f.length));
            }
        }
    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
        Log.info("deleteFile : filename = " + filename + "; userId = " + userId + "; password = " + password);

        this.getUser(userId, password);

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        FileInfo file = getFileFromUser(userId, filename);

        if( file == null){
            Log.info("file does not exist or is not from the user.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        URI[] f = d.knownUrisOf("files");

        int uriCorrect = getServerURIofFile(f, userId, filename);

        RestFilesClient files = new RestFilesClient(f[uriCorrect]);

        files.deleteFile(String.format("%s.%s", userId, filename), "");
        userfiles.get(userId).remove(file);

    }

    private FileInfo getFileFromUser(String userId, String filename){
        FileInfo file = null;

        if(userfiles.containsKey(userId)){
            ArrayList<FileInfo> filesUser = userfiles.get(userId);

            for (FileInfo f: filesUser) {
                if (f.getFilename().equalsIgnoreCase(filename)) {
                    file = f;
                }
            }
        }

        return file;
    }

    private User getUser(String userId, String password){
        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        System.out.println("resource " + password);

        return r.getUser(userId, password);
    }

    private int getServerURIofFile(URI[] uris, String userId, String filename){
        ArrayList<FileInfo> filesUser = userfiles.get(userId);
        int uriCorrect = -1;

        for (FileInfo file: filesUser) {
            if (file.getFilename().equalsIgnoreCase(filename)) {

                for (int j = 0; j < uris.length; j++) {
                    String uriString = uris[j].toString().concat("/" + userId + "." + filename);

                    if (file.getFileURL().equalsIgnoreCase(uriString)) {
                        uriCorrect = j;
                    }
                }
            }
        }
        return uriCorrect;
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("shareFile : filename = " + filename  + "; userId = " + userId  + "; userIdShare = " + userIdShare + "; password = " + password);

        this.getUser(userId, password);

        /*
        List<User> l = r.searchUsers(userIdShare);
        boolean b = false;

        if (!l.isEmpty())
            b = true;

        if (!b) {
            Log.info("UserShared does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }*/

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        FileInfo file = this.getFileFromUser(userId, filename);

        if( file == null){
            Log.info("file does not exist or is not from the user.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        Set<String> shared = file.getSharedWith();
        shared.add(userIdShare);

        file.setSharedWith(shared);

    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("unshareFile : filename = " + filename  + "; userId = " + userId  + "; userIdShare = " + userIdShare + "; password = " + password);

        this.getUser(userId, password);

        /*
        List<User> l = r.searchUsers(userIdShare);
        boolean b = false;

        if (!l.isEmpty())
            b = true;

        if (!b) {
            Log.info("UserShared does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }*/

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        FileInfo file = this.getFileFromUser(userId, filename);

        if( file == null){
            Log.info("file does not exist or is not from the user.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        Set<String> shared = file.getSharedWith();
        shared.remove(userIdShare);

        file.setSharedWith(shared);

    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        Log.info("getFile : filename = " + filename + "; userId = " + userId + "; accUserId = " + accUserId + "; password = " + password);

        this.getUser(userId, password);

        /*
        List<User> l = r.searchUsers(accUserId);
        boolean b = false;

        if (l.size() > 0)
            b = true;

        if (!b) {
            Log.info("User does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }*/

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        FileInfo file = this.getFileFromUser(userId, filename);

        if( file == null){
            Log.info("file does not exist or is not from the user.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        if (!checkUserAccessability(accUserId, file)) {
            Log.info("User does not have permission to see file.");
            throw new WebApplicationException( Status.FORBIDDEN);
        }

        //RestFilesClient files = new RestFilesClient(f[uriCorrect]);
        //return files.getFile(filename, null);

        String uri = file.getFileURL();
        //String uriS = uri.substring(0, uri.length() - 3);

        throw new WebApplicationException(Response.temporaryRedirect(URI.create(uri)).build());

    }


    private boolean checkUserAccessability(String userId, FileInfo file){
        Set<String> list = file.getSharedWith();
        boolean found = false;

        for (String s: list) {
            if(userId.equalsIgnoreCase(s)){
                found = true;
                break;
            }
        }

        if(file.getOwner().equalsIgnoreCase(userId)){
            found = true;
        }

        return found;
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        Log.info("lsFile : userId = " + userId  + "; password = " + password);

        this.getUser(userId, password);

        List<FileInfo> listFiles = new ArrayList<>();

        //adds all the files the user has acess with because they are shared to him or are his files
        for(int i = 0; i < userfiles.size(); i++){
            ArrayList<FileInfo> files = userfiles.get(i);

            for (FileInfo f: files) {
                if(checkUserAccessability(userId, f)){
                    listFiles.add(f);
                }
            }
        }

        return listFiles;
    }


    @Override
    public void deleteUserAndFiles(String userId, String password){

        if(userfiles.containsKey(userId)){
            ArrayList<FileInfo> list = userfiles.get(userId);

            for (FileInfo f: list) {
                this.deleteFile(f.getFilename(), userId, password);
            }

            userfiles.remove(userId);
            deleteSharedFilesUser(userId);
        }
    }

    private void deleteSharedFilesUser(String id){

        for(int i = 0; i < userfiles.size(); i++){
            ArrayList<FileInfo> files = userfiles.get(i);

            for (FileInfo f: files) {
                Set<String> shared = f.getSharedWith();
                shared.remove(id);
                f.setSharedWith(shared);
            }
        }
    }


}
