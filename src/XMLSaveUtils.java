import com.alibaba.fastjson.JSONArray;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XMLSaveUtils {

    private Document document = new Document();
    private Element SVNlogsEle = new Element("SVNLogs");

    public void formatting(List<LogDetails> logDetailsList){

        document.addContent(SVNlogsEle);

        for(LogDetails logDetails : logDetailsList){

            Element logDetailsEle = new Element("logDetails");
            Element changedPathsEle = new Element("changedPaths");


            SVNlogsEle.addContent(logDetailsEle);
            logDetailsEle.addContent(changedPathsEle);

            logDetailsEle.setAttribute("revision", String.valueOf(logDetails.getRevision())); //增加属性
            logDetailsEle.setAttribute("author", logDetails.getAuthor());
            Date logDate = logDetails.getDate();
            //使用java8中的线程安全日期类来格式化log日期
            Instant instant = logDate.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime logDateTime = LocalDateTime.ofInstant(instant, zoneId);
            DateUtilsJava8 dateUtilsJava8 = new DateUtilsJava8("yyyy-MM-dd  HH:mm:ss");
            String logDateTimeStr = dateUtilsJava8.format(logDateTime);
            logDetailsEle.setAttribute("date", logDateTimeStr);


            String changedPathDetailsListJSON  = logDetails.getChangedPathDetailsListJSON();
            List<ChangedPathDetails> changedPathDetailsList = JSONArray.parseArray(changedPathDetailsListJSON, ChangedPathDetails.class);

            int sumOfAddLines = 0;

            for(ChangedPathDetails changedPathDetails : changedPathDetailsList){

                Element changedPathDetailsEle = new Element("changedPathDetails");
                Element filePathEle = new Element("filePath");
                Element changedTypeEle = new Element("changedType");
                Element codeLinesEle = new Element("codeLines");
                Element blankLinesEle = new Element("blankLines");
                Element addLinesEle = new Element("addLines");
                Element delLinesEle = new Element("delLines");

                changedPathsEle.addContent(changedPathDetailsEle);
                changedPathDetailsEle.addContent(filePathEle);
                changedPathDetailsEle.addContent(changedTypeEle);
                changedPathDetailsEle.addContent(codeLinesEle);
                changedPathDetailsEle.addContent(blankLinesEle);
                changedPathDetailsEle.addContent(addLinesEle);
                changedPathDetailsEle.addContent(delLinesEle);

                filePathEle.setText(changedPathDetails.getFilePath());
                changedTypeEle.setText(String.valueOf(changedPathDetails.getChangeType()));
                codeLinesEle.setText(String.valueOf(changedPathDetails.getCodeLines()));
                blankLinesEle.setText(String.valueOf(changedPathDetails.getBlankLines()));
                addLinesEle.setText(String.valueOf(changedPathDetails.getAddLines()));
                delLinesEle.setText(String.valueOf(changedPathDetails.getDelLines()));

                int codeIncreOfPath = changedPathDetails.getAddLines() - changedPathDetails.getDelLines();
                sumOfAddLines += codeIncreOfPath;

                changedPathDetailsEle.setAttribute("codeIncre", String.valueOf(codeIncreOfPath));
            }
            logDetailsEle.setAttribute("codeIncre", String.valueOf(sumOfAddLines));
        }

    }

    public void saveToPath(String path){
        //设置xml输出格式
        Format format = Format.getPrettyFormat();
        format.setEncoding("utf-8");
        format.setIndent("    ");

        //得到xml输出流
        XMLOutputter outputter = new XMLOutputter(format);

        //把数据输出到xml中
        try {
            outputter.output(document, new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
