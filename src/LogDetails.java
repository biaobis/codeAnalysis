import java.util.Date;

public class LogDetails {
    private long revision;  //版本号

    private String author; //log作者

    private Date logDate;  //log日期

    private String message; //备注信息

    private String changedPathDetailsListJSON;  //改动的文件信息列表的JSON格式

    public LogDetails(long revision, String author, Date logDate, String changedPathDetailsListJSON) {
        this.revision = revision;
        this.author = author;
        this.logDate = logDate;
        this.changedPathDetailsListJSON = changedPathDetailsListJSON;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getChangedPathDetailsListJSON() {
        return changedPathDetailsListJSON;
    }

    public void setChangedPathDetailsListJSON(String changedPathDetailsListJSON) {
        this.changedPathDetailsListJSON = changedPathDetailsListJSON;
    }

    public long getRevision() {
        return revision;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return logDate;
    }

    public String getMessage() {
        return message;
    }

    public String toString(){
        return revision + author;
    }
}
