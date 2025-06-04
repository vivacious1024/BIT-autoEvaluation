import io.github.bonigarcia.wdm.WebDriverManager
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Logger

fun isEdgeInstalled(): Boolean {    //检查电脑有没有edge浏览器
    val logger = Logger.getLogger("EdgeCheck")
    val edgePath = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"

    return if (File(edgePath).exists()) {
        logger.info("检测到 Microsoft Edge 浏览器已安装。")
        true
    } else {
        logger.severe("未检测到 Microsoft Edge 浏览器。请确保已安装浏览器。")
        false
    }
}

fun setupEdgeDriver() {     //自动下载合适版本的edge浏览器的自动化驱动程序webdriver
    val logger = Logger.getLogger("EdgeDriverSetup")

    // 开始下载 EdgeDriver
    logger.info("正在下载 EdgeDriver...")

    try {
        WebDriverManager.edgedriver().setup()
        logger.info("EdgeDriver 下载成功并已配置。")
    } catch (e: Exception) {
        logger.severe("EdgeDriver 下载失败: ${e.message}")
        return
    }
}

//这个方法是调用项目文件夹中自己下载的webdriver，不过使用了webdrivermanager之后就不需要了
//fun extractDriver(): String {
//    val resourcePath = "/msedgedriver.exe"
//    val inputStream: InputStream = object {}.javaClass.getResourceAsStream(resourcePath)
//        ?: throw IllegalArgumentException("Driver not found at $resourcePath")
//
//    val tempFile = File.createTempFile("msedgedriver", ".exe")
//    Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
//    tempFile.deleteOnExit()
//    return tempFile.absolutePath
//}
