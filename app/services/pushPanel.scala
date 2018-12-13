package services

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import java.util.UUID

import com.pharbers.driver.PhRedisDriver
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.max.phmaxjob
import com.pharbers.pattern.frame.Brick
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.xssf.usermodel.{XSSFRow, XSSFWorkbook}
import play.api.mvc.Request

case class pushPanel()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    var maxjob: phmaxjob = _
    lazy val configDir : String = System.getProperty("user.dir")

    override val brick_name: String = "save xlsx as csv in hdfs"

    override def prepare: Unit = {
        maxjob = formJsonapi[phmaxjob](rq.body)
    }

    override def exec: Unit = {
        val xlsxPath = s"$configDir/resource/xlsx/" + maxjob.panel
        maxjob.panel = ""
        val name = UUID.randomUUID().toString
        val csvPath = s"$configDir/resource/csv/$name" //todo:去掉魔法值
        val wb = new XSSFWorkbook(xlsxPath)
        checkXlsx(wb)
        val mkt = wb.getSheetAt(0).getRow(1).getCell(8).toString
        maxjob.yms = wb.getSheetAt(0).getRow(1).getCell(2).toString
        saveXlsxAsCsv(wb, csvPath, xlsxPath)
        saveInHdfs(csvPath)
        saveRedis(name,mkt)
        println(name)
        maxjob.panel = name
        maxjob.panelfime = name
    }
    override def done: Option[String] = {
        None
    }
    override def goback: model.RootObject = toJsonapi(maxjob)
    private def checkXlsx(wb: XSSFWorkbook): Unit = {
        val panelHead = List("ID", "Hosp_name", "Date", "Prod_Name", "Prod_CNAME", "HOSP_ID", "Strength", "DOI", "DOIE", "Units", "Sales")
        val sheet = wb.getSheetAt(0)
        val head = sheet.getRow(sheet.getFirstRowNum)
        (head.getFirstCellNum until head.getLastCellNum).zip(panelHead).map(x => head.getCell(x._1).toString.equals(x._2))
                .find(x => !x) match {
            case Some(_) => throw new Exception("xlsx error")
            case _ => Unit
        }
    }

    private def saveXlsxAsCsv(wb: XSSFWorkbook, csvPath: String, xlsxPath: String): Unit ={
        val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(csvPath))))
        val sheet = wb.getSheetAt(0)
        (sheet.getFirstRowNum to sheet.getLastRowNum).foreach(x => {
            write(sheet.getRow(x), writer)(row => {
                (row.getFirstCellNum until row.getLastCellNum).map(x => row.getCell(x).toString).mkString(31.toChar.toString)
            })
        })
        writer.flush()
        writer.close()
        new File(xlsxPath).delete()
    }

    private def write(row: XSSFRow, writer: BufferedWriter)(fun:XSSFRow => String): Unit = {
        writer.write(fun(row))
        writer.newLine()
    }

    private def saveInHdfs(path: String): Unit ={
        val localDir = path
        val hdfsDir = "hdfs:///workData/Panel"  //todo:去掉魔法值
        val conf = new Configuration()
        val localPath = new Path(localDir)
        val hdfsPath = new Path(hdfsDir)
        val hdfs = FileSystem.get(conf)
        hdfs.copyFromLocalFile(localPath, hdfsPath)
        new File(path).delete()
    }

    private def saveRedis(panelName: String, mkt: String): Unit ={
        val jobId = maxjob.job_id
        val ym = maxjob.yms
        val rd = new PhRedisDriver()
        rd.addSet(jobId, panelName)
        rd.addSet(jobId + "ym", ym)
        rd.expire(jobId, 60 * 60 * 24)
        rd.expire(jobId + "ym", 60 * 60 * 24)
        rd.addMap(panelName, "ym", ym)
        rd.addMap(panelName, "mkt", mkt)
        rd.expire(panelName, 60 * 60 * 24)
    }

}

//object run extends App {
//    findAllPaperById().saveAsCsvInHdfs("/home/alfred/Documents/对数bayer1806/数据/AHP_Panel+201806 (copy).xlsx")
//}