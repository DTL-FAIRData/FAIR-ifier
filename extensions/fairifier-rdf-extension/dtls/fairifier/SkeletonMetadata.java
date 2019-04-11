package org.dtls.fairifier;


public class SkeletonMetadata {
    private String fileType;
    private String title;
    private String fileName;
    private String projectId;
   
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return this.title;
    }    
    
    public void setFileName(String filename) {
        this.fileName = filename;
    }
    
    public String getFileName() {
        return this.fileName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getProjectId() {
        return this.projectId;
    }
}
