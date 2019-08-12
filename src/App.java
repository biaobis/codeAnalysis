import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    private String svnurl;
    private String userName;
    private String password;
    private Date startDate;
    private Date endDate;
    private String[] includedDir;  //指定目录
    private String[] excludedDir;  //排除目录
    private String[] includedType;  //指定文件类型
    private String[] excludedType;  //排除文件类型

    private String srcPath;  //本地代码目录
    private MimetypesFileTypeMap mimetypesFileTypeMap;

    public App(String svnurl, String userName, String password, Date startDate, Date endDate
            , String[] includedDir, String[] excludedDir, String[] includedType, String[] excludedType) {
        this.svnurl = svnurl;
        this.userName = userName;
        this.password = password;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includedDir = includedDir;
        this.excludedDir = excludedDir;
        this.includedType = includedType;
        this.excludedType = excludedType;
    }

    public App(String[] includedDir, String[] excludedDir
            , String[] includedType, String[] excludedType, String srcPath) {
        this.includedDir = includedDir;
        this.excludedDir = excludedDir;
        this.includedType = includedType;
        this.excludedType = excludedType;
        this.srcPath = srcPath;

        mimetypesFileTypeMap = new MimetypesFileTypeMap();
        mimetypesFileTypeMap.addMimeTypes("image png jpg gif jpeg bmp");
    }

    /**
     * 存储svnkit中的文件改动信息到指定文件
     */
    private void saveResultToSavePath(String savePath){

        SVNKitUtil svnKitUtil = new SVNKitUtil(svnurl, userName, password);
        List<LogDetails> logDetailsList = svnKitUtil.getLogDetails(startDate, endDate, includedDir, excludedDir
                , includedType, excludedType);

        XMLSaveUtils xmlSaveUtils = new XMLSaveUtils();
        xmlSaveUtils.formatting(logDetailsList);
        xmlSaveUtils.saveToPath(savePath);
    }

    /**
     * 统计列表中的所有文件代码行总和
     */
    private int[] codeLineSum(){
        int[] res  = new int[3];
        List<String> files = new ArrayList<>();
        LocalFileAnalyzeUtils.getAllFilePath(srcPath, files);

        List<String> filesAfterFilter=  files.stream()
                .filter(p -> DirAndPostfixFilterUtils.filter(p, '\\', includedDir, excludedDir
                        , includedType, excludedType))
                .filter(p -> !mimetypesFileTypeMap.getContentType(p).equals("image"))
                .collect(Collectors.toList());

        for(String file : filesAfterFilter){
            int[] codeLines = LocalFileAnalyzeUtils.codeLineOfFile(file);
            res[0] += codeLines[0];
            res[1] += codeLines[1];
            res[2] += codeLines[2];
        }
        return res;
    }

    public static void main(String[] args){

        Properties properties = new Properties();
        try {
            String jarPath = App.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath();
            String pathPrefix = jarPath.substring(1,jarPath.lastIndexOf("/"));
            File file = new File(pathPrefix + "/args.ini");
            if(!file.exists()){
                System.out.println("配置文件不存在，请在jar包同级目录下添加配置文件！");
                System.exit(1);
            }
            FileReader fileReader = new FileReader(file);
            properties.load(fileReader);
        } catch (IOException e) {
            System.err.println("读取配置文件错误！");
        }

        String includedDirStr = properties.getProperty("includedDir");
        String excludedDirStr = properties.getProperty( "excludedDir");
        String includedTypeStr = properties.getProperty( "includedType");
        String excludedTypeStr = properties.getProperty("excludedType");

        String[] includedDir = {};
        String[] excludedDir = {};
        String[] includedType = {};
        String[] excludedType = {};
        if(includedDirStr != null)
            includedDir = includedDirStr.trim().split("\\|");
        if(excludedDirStr != null){
            excludedDir = excludedDirStr.trim().split("\\|");
        }
        if(includedTypeStr != null){
            includedType = includedTypeStr.trim().split("\\|");
        }
        if(excludedTypeStr != null){
            excludedType = excludedTypeStr.trim().split("\\|");
        }

        Scanner sc = new Scanner(System.in);

        System.out.println();
        System.out.print("查询SVN版本库文件改动信息输入”A“，查询本地代码行数输入”B“ ： ");
        String func = sc.nextLine();
        System.out.println();

        if(func.equals("A")){
            System.out.println("-----------------------查询svn版本库中的文件改动信息-----------------------\n");
            System.out.print("请输入用户名：");
            String userName = sc.nextLine().trim();
            System.out.print("请输入密码：");
            String password = sc.nextLine().trim();
            System.out.print("请输入结果的存储目录：");
            String resultSavePath = sc.nextLine().trim();

            String svnURL = properties.getProperty("svnurl").trim();
            String startDateStr = properties.getProperty("startDate").trim();
            String endDateStr = properties.getProperty("endDate").trim();

            SimpleDateFormat formator = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = formator.parse(startDateStr);
                endDate = formator.parse(endDateStr);
            } catch (ParseException e) {
                System.err.println("输入日期格式错误！");
            }

            App app = new App(svnURL, userName, password, startDate, endDate
                    , includedDir, excludedDir, includedType, excludedType);

            app.saveResultToSavePath(resultSavePath + "/logDetails.xml");
        }else {
            System.out.println("-----------------------查询本地代码行数-----------------------\n");
            String srcPath = properties.getProperty("localurl");
            App app = new App(includedDir, excludedDir, includedType, excludedType, srcPath);

            int[] codeLineSum = app.codeLineSum();
            System.out.println("路径 " + srcPath + " 下,代码行数为：" + codeLineSum[0] + " 行； 空行数为："
                    + codeLineSum[1] + " 行；注释数为：" + codeLineSum[2] + "行。");
        }

        sc.close();
    }

}
