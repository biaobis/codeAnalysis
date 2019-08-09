import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class SVNKitUtil {

    private String svnurl;
    private String userName;
    private String password;

    private SVNRepository repository;  //版本库实例
    private SVNDiffClient svnDiffClient; //获取差异的接口

    public SVNKitUtil(String svnurl, String userName, String password) {
        this.svnurl = svnurl;
        this.userName = userName;
        this.password = password;

        init();  //svnkit初始化
    }

    private void init(){
        DAVRepositoryFactory.setup();  //使得支持http:// 和 https:// 形式的url
        SVNRepositoryFactoryImpl.setup();  //使得支持svn:// 和 svn+ssh:// 形式的url
        FSRepositoryFactory.setup();  //使得支持file:///形式的url

        try {  //实例化版本库
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnurl));
        } catch (SVNException e) {
            System.err.println("创建版本库实例时失败，版本库的URL是：" + svnurl + "  " + e.getMessage());
            System.exit(1);
        }

        //设置认证信息
        ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
        repository.setAuthenticationManager(authenticationManager);

        //配置SVNDiffClient
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        options.setDiffCommand("-x -w");
        svnDiffClient = new SVNDiffClient(authenticationManager, options);
        svnDiffClient.setGitDiffFormat(true);
    }

    /*根据起止时间获得log集合并返回*/
    private List<SVNLogEntry> getLogsByTime(Date startDate, Date endDate) throws SVNException {
        //获得起止日期对应的版本号
        long startRevision = repository.getDatedRevision(startDate);
        long endRevision = repository.getDatedRevision(endDate);

        @SuppressWarnings("unchecked")
        Collection<SVNLogEntry> svnLogEntries = repository.log(new String[]{""},null
                ,startRevision, endRevision, true, true);
        return (List<SVNLogEntry>) svnLogEntries;
    }

    /*判断svn节点路径路径是否是文件路径*/
    private boolean isFileNode(String path){
        SVNNodeKind nodeKind = null;
        try {
            nodeKind = repository.checkPath(path, -1);
        } catch (SVNException e) {
            System.err.println("解析路径节点类型出错！");
        }
        return nodeKind == SVNNodeKind.FILE;
    }

    /*统计一个文件中的代码行总量以及空行数*/
    private int[] getCodeLinesInFile(ByteArrayOutputStream baos) {
        int[] codeAndCodeBlank = new int[2];

        byte[] bytes = baos.toByteArray();
        String fileContent = new String(bytes);
        String[] lines = fileContent.split("\r\n");
        for(String line : lines){
            if(line.trim().length() < 1)
                codeAndCodeBlank[1]++;
            else
                codeAndCodeBlank[0]++;
        }

        return codeAndCodeBlank;
    }

    /*获取单个文件的diff内容，去除头部返回主体*/
    private String getDiffBody(String path, long revision) {
        String filePath = svnurl + path;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            svnDiffClient.doDiff(SVNURL.parseURIEncoded(filePath),
                    SVNRevision.HEAD,
                    SVNRevision.create(revision-1),
                    SVNRevision.create(revision),
                    SVNDepth.UNKNOWN, true, outputStream);
        } catch (SVNException e) {
            e.printStackTrace();
        }

        byte[] bytes = outputStream.toByteArray();
        String diffStr = new String(bytes);

        //去除头部
        int contentIndex = diffStr.indexOf("@@\r\n") + 4;

        return diffStr.substring(contentIndex);
    }

    /*统计文件的代码增量*/
    private int[] getNumOfChangedLines(String diffBody) {
        int[] res = new int[2];

        if(diffBody != null){
            String[] lines = diffBody.split("\r\n");
            for(String line: lines){
                char[] charsOfLine = line.toCharArray();
                boolean startWithPlus = false;  //是否以+开头
                boolean startWithMius = false;
                boolean notSpace = false;  //是否是空行

                int i = 0;
                if(charsOfLine[i] == '+'){
                    startWithPlus = true;
                    while((++i) < charsOfLine.length){
                        if(charsOfLine[i] > ' '){
                            notSpace = true;
                        }
                    }
                }else if(charsOfLine[i] == '-'){
                    startWithMius = true;
                    while((++i) < charsOfLine.length){
                        if(charsOfLine[i] > ' '){
                            notSpace = true;
                        }
                    }
                }

                if(startWithPlus && notSpace){
                    res[0]++;
                }
                if(startWithMius && notSpace)
                    res[1]++;
            }
        }

        return  res;
    }

    /*获得起止日期内log集合,将解析后的信息过滤、存入列表并返回*/
    public List<LogDetails> getLogDetails(Date startDate, Date endDate
            , String[] includedDir, String[] excludedDir
            , String[] includedType, String[] excludedType){

        List<LogDetails> logDetailsList = new ArrayList<>();

        List<SVNLogEntry> svnLogEntryList = null;
        try {
            svnLogEntryList = getLogsByTime(startDate, endDate);
        } catch (SVNException e) {
            System.err.println("获取起止期间内的log失败！");
        }

        if(svnLogEntryList.size() > 0){

            for(SVNLogEntry svnLogEntry : svnLogEntryList){

                long revision = svnLogEntry.getRevision();  //log版本号
                if (revision < 2) {                    //跳过0版本和1版本
                    continue;
                }
                String author = svnLogEntry.getAuthor();  //log作者
                String msg = svnLogEntry.getMessage();  //log备注信息

                Date logDate = svnLogEntry.getDate();   //log日期
                //利用java8中的线程安全日期类来格式化log日期
                Instant instant = logDate.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDateTime logDateTime = LocalDateTime.ofInstant(instant,zoneId); //Date转LocalDateTime
                DateUtilsJava8 dateUtilsJava8 = new DateUtilsJava8("yyyy-MM-dd  HH:mm:ss");
                String logDateTimeStr = dateUtilsJava8.format(logDateTime);  //也可以直接toString()

                List<ChangedPathDetails> changedPathDetailsList = null;
                if(svnLogEntry.getChangedPaths().size() > 0){

                    changedPathDetailsList = new ArrayList<>(); //改动文件信息列表
                    Map<String, SVNLogEntryPath> changedPathsMap = svnLogEntry.getChangedPaths();
                    Set<String> keySet = changedPathsMap.keySet();  //log中的改动文件路径集合

                    //根据文件后缀和目录进行过滤
                    Set<String> changedPathsAfterFilter = keySet.stream()
                            .filter(this::isFileNode)   //过滤非文件路径(可能是增加了一个目录)
                            .filter(p -> DirAndPostfixFilterUtils.filter(p, '/', includedDir, excludedDir
                                    , includedType, excludedType))
                            .collect(Collectors.toSet());

                    if(changedPathsAfterFilter.size() > 0){

                        for(String pathKey : changedPathsAfterFilter){

                            SVNLogEntryPath entryPath = changedPathsMap.get(pathKey);  //根据key获得SVNLogEntryPath
                            char changedType = entryPath.getType(); //改动类型

                            /*获取文件内容和属性*/
                            SVNProperties svnProperties = new SVNProperties();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream( );  //文件内容输出流
                            try {
                                repository.getFile(pathKey, -1, svnProperties, baos);
                            } catch (SVNException e) {
                                System.err.println("获取文件属性和内容出错！");
                            }

                            String mimeType = svnProperties.getStringValue(SVNProperty.MIME_TYPE); //文件的mimeType

                            if(SVNProperty.isTextMimeType(mimeType)){  //如果是文本文件

                                int[] codeAndCodeBlank = getCodeLinesInFile(baos);  //计算文件的代码行数和空行数

                                String diffBody = getDiffBody(pathKey, revision);
                                int[] addAndDelLines = getNumOfChangedLines(diffBody);  //计算文件的代码改动量

                                changedPathDetailsList.add(new ChangedPathDetails(pathKey ,changedType
                                        , mimeType, codeAndCodeBlank[0], codeAndCodeBlank[1]
                                        , addAndDelLines[0], addAndDelLines[1]));
                            }

                        }
                    }
                }

                if(changedPathDetailsList != null && changedPathDetailsList.size() > 0){
                    logDetailsList.add(new LogDetails(revision, author, logDateTimeStr, msg, changedPathDetailsList));
                }

            }
        }
        return logDetailsList;
    }


}
