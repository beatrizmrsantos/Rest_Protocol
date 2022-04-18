package tp1.server.resources;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.client.RestDirectoryClient;
import tp1.client.RestUsersClient;

@Singleton
public class UsersResource implements RestUsers {

    private final Map<String, User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    private Discovery d;

    public UsersResource(Discovery d) {
        this.d = d;
        d.listener();
    }

    @Override
    public String createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || user.getEmail() == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        // Check if userId already exists
        if (users.containsKey(user.getUserId())) {
            Log.info("User already exists.");
            throw new WebApplicationException(Status.CONFLICT);
        }

        //Add the user to the map of users
        users.put(user.getUserId(), user);
        return user.getUserId();
    }


    @Override
    public User getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        // Check if user is valid
        if (userId == null) {
            Log.info("UserId is null.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        System.out.println("user  " + password);

        User user = users.get(userId);

        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Check if the password is correct
        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        return user;
    }


    @Override
    public User updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

        if (userId == null  || user == null) {
            Log.info("UserId, password or user null.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        User userOld = users.get(userId);

        // Check if user exists
        if (userOld == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Check if the password is correct
        if (!userOld.getPassword().equals(password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        user.setUserId(userId);

        if (user.getEmail() == null) {
            user.setEmail(userOld.getEmail());
        }

        if (user.getFullName() == null) {
            user.setFullName(userOld.getFullName());
        }

        if (user.getPassword() == null) {
            user.setPassword(userOld.getPassword());
        }


        users.put(userId, user);
        return user;
    }


    @Override
    public User deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        if (userId == null) {
            Log.info("UserId is null.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        User user = users.get(userId);

        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        //Check if the password is correct
        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        /*
        URI[] dir = d.knownUrisOf("directory");

        RestDirectoryClient directory = new RestDirectoryClient(dir[0]);

        //List<FileInfo> f = directory.lsFile(userId, password);
        //deleteFilesUser(f, directory, userId, password);

        directory.deleteUserAndFiles(userId, password);
        //directory.deleteSharedFilesUser(userId);

        */

        User deleted = users.remove(userId);

        return deleted;
    }

    /*
    private void deleteFilesUser(List<FileInfo> f, RestDirectoryClient d, String id, String psw){
        for(int i = 0; i < f.size(); i++){
            FileInfo file = f.get(i);
            d.deleteFile(file.getFilename(),id ,psw);
        }
    }*/


    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        // checks if the pattern is null
        if (pattern == null) {
            Log.info("Pattern is null.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        List<User> listUsers = new ArrayList<>();

        for (User u : users.values()) {
            String name = u.getFullName().toLowerCase();
            if (name.contains(pattern.toLowerCase())) {
                listUsers.add(u);
            }
        }

        return listUsers;

    }

}
