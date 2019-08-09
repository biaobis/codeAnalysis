public class ChangedPathDetails {

    private String filePath;   //文件路径
    private String fileType;   //文件的后缀
    private char changeType;  //改动类型 'A':增加 、 'D':删除 、 'M':修改 、 'R': 替换

    private String mimeType; //文件的MIME_TYPE类型(用于判断是否是文本文件)
    private  int codeLines;  //文件代码行总数
    private int blankLines; //文件中的空行数
    private int addLines;  //文件增加的代码行数
    private int delLines;  //文件删除的代码行数

    public ChangedPathDetails(String filePath, char changeType
            , String mimeType, int codeLines, int blankLines, int addLines, int delLines) {
        this.filePath = filePath;
        this.changeType = changeType;
        this.mimeType = mimeType;
        this.codeLines = codeLines;
        this.blankLines = blankLines;
        this.addLines = addLines;
        this.delLines = delLines;

        this.fileType = getFileTypeFromPath(filePath);  //初始化文件后缀
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public char getChangeType() {
        return changeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public int getBlankLines() {
        return blankLines;
    }

    public int getAddLines() {
        return addLines;
    }

    public int getDelLines() {
        return delLines;
    }

    private  String getFileTypeFromPath(String filePath){
        String fileType = "";
        int index = filePath.lastIndexOf(".");
        if(index > -1)
            fileType = filePath.substring(index+1).trim();
        return fileType;
    }

    public String toString(){

        return "filePath : " + filePath + "    " + "changeType : " + changeType  + "    " + "fileType : " + fileType
                + "\ncontent : \n" ;
    }
}
