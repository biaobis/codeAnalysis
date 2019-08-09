import java.util.List;

public class LogDetails {

    private long revision;  //版本号
    private String author; //log作者
    private String logDate;  //log日期
    private String message; //备注信息
    private List<ChangedPathDetails> changedPathDetailsList;  //改动的文件信息列表

    public LogDetails(long revision, String author, String date, String message, List<ChangedPathDetails> changedFileInfoList) {
        this.revision = revision;
        this.author = author;
        this.logDate = date;
        this.message = message;
        this.changedPathDetailsList = changedFileInfoList;
    }

    public long getRevision() {
        return revision;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return logDate;
    }

    public String getMessage() {
        return message;
    }

    public List<ChangedPathDetails> getChangedFileInfoList() {
        return changedPathDetailsList;
    }

    public String toString(){
        return revision + author;
    }
}
