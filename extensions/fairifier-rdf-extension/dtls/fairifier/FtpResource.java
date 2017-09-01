package org.dtls.fairifier;

import java.net.URL;
import java.io.File;
import java.io.OutputStream;
import java.net.URLConnection;
import java.io.IOException;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * A resource to store FAIR data in FTP
 * @author Shamanou van Leeuwen
 * @date 13-12-2016
 *
 */

/**
 * 
 * A extension of the abstract class Resource 
 * to be able to push FAIR data to FTP.
 * @author Shamanou van Leeuwen
 *
 */
public class FtpResource extends Resource{
    private String out;
    private String username;
    private String password;
    private String host;
    private String location;
    private String filename;

    
    /**
     * 
     * A constructor which defines the resource with the necessary parameters for FTP
     * 
     * host : is the full hostname including the protocol, for example: http://dtls.nl
     * username : the FTP username
     * password : the FTP password
     * location : the path where the file should be store on the FTP server.
     * filename : the name with which the file will be saved on the FTP server.
     * 
     * @param host
     * @param username
     * @param password
     * @param location
     * @param filename
     */
    public FtpResource(String host, String username, String password, String location, String filename){
        this.host = host;
        this.password = password;
        this.username = username;
        this.location = location;
        this.filename = filename;
    }
    
    /**
     * This method overrides the push method from the abstract class Resource
     * and implements it. It allows pushing to an FTP server with the in constructor
     * predefined parameters.
     * 
     */
    @Override
    public void push(){
        if (!this.hasModel()){
            throw new IllegalArgumentException("Data of Resource object not set!");
        }else{
            this.out = this.getModelString(); 
        }
        try{
            FTPClient ftp = new FTPClient();
            ftp.setBufferSize(1024000);
            ftp.connect(this.host);
            ftp.login(this.username, this.password);
            ftp.changeWorkingDirectory(this.location);
            ftp.setFileTransferMode(ftp.BINARY_FILE_TYPE);
            ftp.storeFile(this.filename, new BufferedInputStream(new ByteArrayInputStream(this.out.getBytes())));
            ftp.logout();
        }catch(IOException ex){
            System.out.println(ex.toString());
        }
    }
}
