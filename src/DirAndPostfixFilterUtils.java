import java.util.Arrays;
import java.util.List;

public class DirAndPostfixFilterUtils {

    /*过滤指定和排除的目录以及后缀*/
    public static boolean filter(String filePath, char mark, String[] includedDir, String[] excludedDir
            ,String[] includedType, String[] excludedType){
        boolean inFlag = false;
        boolean exflag = true;
        boolean typeFlag = true;

        String fileType = "";
        int idx = filePath.lastIndexOf(".");
        if(idx > -1)
            fileType = filePath.substring(idx+1).trim();


        int index = filePath.lastIndexOf(mark);
        String prefix = filePath.substring(0,index); //文件所属目录

        if(includedDir.length > 0){  //如果指定了查询目录
            for(String dir : includedDir){
                if(prefix.contains(dir)){
                    inFlag = true;
                    break;
                }
            }
        }else {
            inFlag = true;
        }
        if(excludedDir.length > 0){  //指定了排除目录
            for(String dir : excludedDir){
                if(prefix.contains(dir)){
                    exflag = false;
                    break;
                }
            }
        }

        List<String> inType = Arrays.asList(includedType);
        List<String> exType = Arrays.asList(excludedType);
        if(!inType.isEmpty()){
            typeFlag = inType.contains(fileType);
        }else if(!exType.isEmpty()){
            typeFlag = !exType.contains(fileType);
        }

        return (inFlag && exflag && typeFlag);
    }

}
