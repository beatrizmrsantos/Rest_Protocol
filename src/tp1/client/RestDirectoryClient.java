package tp1.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;

@Singleton
public class RestDirectoryClient extends RestClient implements RestDirectory {

    final WebTarget target;

    public RestDirectoryClient(URI serverURI) {
        super( serverURI );
        target = client.target( serverURI ).path( RestDirectory.PATH );
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        return super.reTry( () -> {
            return clt_writeFile( filename, data, userId, password );
        });
    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
        super.reTry( () -> {
           clt_deleteFile( filename, userId, password );
            return null;
        });
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        super.reTry( () -> {
            clt_shareFile( filename, userId, userIdShare, password );
            return null;
        });
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        super.reTry( () -> {
            clt_unshareFile( filename, userId, userIdShare, password );
            return null;
        });
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        return super.reTry( () -> {
           return  clt_getFile( filename, userId, accUserId, password );
        });
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        return super.reTry( () -> {
            return  clt_lsFile(userId, password );
        });
    }

    @Override
    public void deleteUserAndFiles(String userId, String password){
         super.reTry( () -> {
              clt_deleteUserAndFiles(userId, password);
              return null;
        });
    }

    private FileInfo clt_writeFile(String filename, byte[] data, String userId, String password ){

        Response r = target.path(userId).path(filename)
                           .queryParam("password", password).request()
                           .accept(MediaType.APPLICATION_JSON)
                           .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        System.out.println("writeFileDir: " + r.getStatus());

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ){
            return r.readEntity(FileInfo.class);
        } else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
            return null;
        }
    }

    private void clt_deleteFile(String filename, String userId, String password ){

        Response r = target.path(userId).path(filename)
                           .queryParam("password", password).request()
                           .delete();

        if ( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
           r.readEntity(FileInfo.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }

    private void clt_shareFile(String filename, String userId, String userIdShare, String password ){

        Response r = target.path(userId).path(filename).path("share").path(userIdShare)
                           .queryParam("password", password).request()
                           .post(Entity.entity(filename, MediaType.APPLICATION_JSON));

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            r.readEntity(FileInfo.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }

    private void clt_unshareFile(String filename, String userId, String userIdShare, String password ){

        Response r = target.path(userId).path(filename).path("share").path(userIdShare)
                           .queryParam("password", password).request()
                           .delete();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            r.readEntity(FileInfo.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }

    private byte[] clt_getFile(String filename, String userId, String userIdShare, String password ){
        Response r = target.path(userId).path(filename)
                           .queryParam("accUserId", userIdShare).queryParam("password", password).request()
                           .accept(MediaType.APPLICATION_OCTET_STREAM)
                           .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
            return r.readEntity(new GenericType<byte[]>() {
            });
        } else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
            return null;
        }
    }

    private List<FileInfo> clt_lsFile(String userId, String password){

        Response r = target.path(userId)
                           .queryParam("password", password).request()
                           .accept(MediaType.APPLICATION_JSON)
                           .get();

        if ( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
            return r.readEntity(new GenericType<List<FileInfo>>() {
            });
        } else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
            return null;
        }
    }

    private void clt_deleteUserAndFiles( String userId, String password){
        Response r = target.path(userId).queryParam("password", password)
                    .request().delete();

        if ( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            r.readEntity(FileInfo.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );
    }
}
