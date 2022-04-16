package tp1.server.resources;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.Set;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
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

        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        URI[] f = d.knownUrisOf("files");

        int number = (int)Math.floor(Math.random()*(f.length-1));

        RestFilesClient files1 = new RestFilesClient(f[number]);

        //randomServer(f, filename, data);

        String uri = f.toString();
        uri.concat("/"+userId+"/"+filename);

        HashSet<String> set = new HashSet<>();
        FileInfo i = new FileInfo(userId,filename,uri,set);


        if(userfiles.containsKey(userId)){
            ArrayList<FileInfo> files = userfiles.get(userId);
            if(!files.isEmpty()) {
                for (FileInfo file : userfiles.get(userId)) {
                    if (file.getFilename().equalsIgnoreCase(filename)) {
                        userfiles.get(userId).remove(file);
                    }
                }
            }
        } else {
            userfiles.put(userId, new ArrayList<>());
        }

        userfiles.get(userId).add(i);

        files1.writeFile(filename,data, null);

        return i;
    }

    /*
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
    }*/

    @Override
    public void deleteFile(String filename, String userId, String password) {
        Log.info("writeFile : filename = " + filename + "; userId = " + userId + "; password = " + password);

        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

        URI[] f = d.knownUrisOf("files");

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        if(userfiles.containsKey(userId)) {
            ArrayList<FileInfo> filesUser = userfiles.get(userId);
            int uriCorrect = -1;

            for (int i = 0; i < filesUser.size(); i++) {
                FileInfo file = filesUser.get(i);
                if (file.getFilename().equalsIgnoreCase(filename)) {
                    for (int j = 0; j < f.length; j++) {

                        String uriS = f[j].toString().concat("/" + userId + "/" + filename);
                        if (file.getFileURL().equalsIgnoreCase(uriS)) {
                            uriCorrect = j;
                        }
                    }
                }
            }
            RestFilesClient files = new RestFilesClient(f[uriCorrect]);

            for (FileInfo file: userfiles.get(userId)) {
                if(file.getFilename().equalsIgnoreCase(filename)){

                    files.deleteFile(filename, null);

                    userfiles.get(userId).remove(file);
                }
            }

        }

    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("writeFile : filename = " + filename  + "; userId = " + userId  + "; userIdShare = " + userIdShare + "; password = " + password);

        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

        List<User> l = r.searchUsers(userIdShare);
        boolean b = false;

        if (!l.isEmpty())
            b = true;

        if (!b) {
            Log.info("UserShared does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        boolean found = false;
        for (FileInfo file: userfiles.get(userId)) {
            if(file.getFilename().equalsIgnoreCase(filename)){

                Set<String> shared = file.getSharedWith();
                shared.add(userIdShare);

                file.setSharedWith(shared);

                found = true;

            }
        }

        if(!found){
            Log.info("File does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }


    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("writeFile : filename = " + filename  + "; userId = " + userId  + "; userIdShare = " + userIdShare + "; password = " + password);


        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

        List<User> l = r.searchUsers(userIdShare);
        boolean b = false;

        if (!l.isEmpty())
            b = true;

        if (!b) {
            Log.info("UserShared does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        if(filename == null) {
            Log.info("fileId is invalid.");
            throw new WebApplicationException( Status.BAD_REQUEST);
        }

        boolean found = false;
        for (FileInfo file: userfiles.get(userId)) {
            if(file.getFilename().equalsIgnoreCase(filename)){

                Set<String> shared = file.getSharedWith();
                shared.remove(userIdShare);

                file.setSharedWith(shared);
                found = true;
            }
        }

        if(!found){
            Log.info("File does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        Log.info("writeFile : filename = " + filename + "; userId = " + userId + "; accUserId = " + accUserId + "; password = " + password);

        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

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

        boolean foundFile = false;
        String uri = null;

        if (userfiles.containsKey(userId)) {
            ArrayList<FileInfo> filesUser = userfiles.get(userId);

            for (int i = 0; i < filesUser.size(); i++) {
                FileInfo file = filesUser.get(i);

                if (file.getFilename().equalsIgnoreCase(filename)) {
                    foundFile = true;
                    uri = file.getFileURL();

                    if (!checkUserAccessability(accUserId, file)) {
                        Log.info("User does not have permission to see file.");
                        throw new WebApplicationException( Status.FORBIDDEN);
                    }

                }
            }
        }

        if (!foundFile) {
            Log.info("File does not exist.");
            throw new WebApplicationException( Status.NOT_FOUND);
        }

        //RestFilesClient files = new RestFilesClient(f[uriCorrect]);
        //return files.getFile(filename, null);

        throw new WebApplicationException(Response.temporaryRedirect(URI.create(uri)).build());

    }

    private boolean checkUserAccessability(String userId, FileInfo file){
        Set<String> list = file.getSharedWith();
        boolean b = false;

        for (String s: list) {
            if(userId.equalsIgnoreCase(s)){
                b = true;
            }
        }

        if(file.getOwner().equalsIgnoreCase(userId)){
            b = true;
        }

        return b;
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        Log.info("writeFile : userId = " + userId  + "; password = " + password);

        URI[] u = d.knownUrisOf("users");

        RestUsersClient r = new RestUsersClient(u[0]);

        User user = r.getUser(userId, password);

        return userfiles.get(userId);
    }

    @Override
    public void deleteUserAndFiles(String userId, String password){

        if(userfiles.containsKey(userId)){
            ArrayList<FileInfo> list = userfiles.get(userId);

            for(int i = 0; i < list.size(); i++){
                FileInfo file = list.get(i);
                this.deleteFile(file.getFilename(), userId, password);
            }

            userfiles.remove(userId);
            deleteSharedFilesUser(userId);
        }
    }

    private void deleteSharedFilesUser(String id){

        for(int i = 0; i < userfiles.size(); i++){
            ArrayList<FileInfo> files = userfiles.get(i);

            for(int j = 0; j < files.size(); j++){
                Set<String> shared = files.get(j).getSharedWith();
                shared.remove(id);
                userfiles.get(i).get(j).setSharedWith(shared);
            }
        }
    }
}
