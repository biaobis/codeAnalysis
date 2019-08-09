import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LocalFileAnalyzeUtils {
    /**
     * 获得指定路径下的所有文件
     */
    public static void getAllFilePath(String src, List<String> resultFilePath){
        File srcFile = new File(src);
        if(!srcFile.exists()){
            System.err.println("该文件路径不存在！");
            return;
        }
        File[] files = srcFile.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isFile()){   //如果是文件
                    resultFilePath.add(file.getAbsolutePath());
                }else {
                    getAllFilePath(file.getAbsolutePath(), resultFilePath);
                }
            }
        }
    }

    /**
     * 计算单个文件代码行数目、空行数目以及注释数目
     */
    public static int[] codeLineOfFile(String filePath){
        int[] res = new int[3];
        int codeSum = 0;  //代码行数
        int blankSum = 0; //空行数
        int commentSum = 0; //注释数目

        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null) {

                if(line.matches("^\\s*$")){  //是空行
                    blankSum++;
                }else if(line.trim().startsWith("//")){  //单行注释
                    commentSum++;
                }else if(line.trim().startsWith("/*")){  //多行注释和文档注释
                    commentSum++;
                    while(!line.endsWith("*/")){
                        line = br.readLine();
                        commentSum++;
                    }
                }else { //普通代码行
                    codeSum++;
                }

            }
        } catch (IOException e) {
            System.err.println("文件读取错误！");
        }

        res[0] = codeSum;
        res[1] = blankSum;
        res[2] = commentSum;
        return res;
    }
}
