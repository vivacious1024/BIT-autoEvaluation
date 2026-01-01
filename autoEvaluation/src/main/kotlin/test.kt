import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.logging.Logger



fun test(username: String, password: String) {
    // 提取并设置 MSEdgeDriver 路径
    //val driverPath = extractDriver()
    //System.setProperty("webdriver.edge.driver", driverPath)

    if(isEdgeInstalled()){
        // 使用 WebDriverManager 自动管理 EdgeDriver
        setupEdgeDriver()

        // 创建 WebDriver 实例和用于处理 JavaScript 逻辑的操作器
        val options = EdgeOptions()
        options.addArguments("--start-fullscreen")
        val driver = EdgeDriver(options)
        val jsExecutor = driver as JavascriptExecutor
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))

        try {
            // 打开网页
            driver.get("https://jwms.bit.edu.cn/jsxsd/xspj/xspj_rk.do?Ves632DSdyV=NEW_XSD_JXPJ")

            // println(driver.pageSource)

            // 先找到并输入账号密码还要点击登录按钮
            val usernameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("username"))
            )
            usernameInput.sendKeys(username)

            val passwordInput: WebElement = driver.findElement(
                By.xpath("//input[@placeholder='请输入密码']")
            )

            passwordInput.sendKeys(password)

            val loginSubmit: WebElement = driver.findElement(By.id("submitBtn"))
            loginSubmit.click()

            wait
            // 找到学生评教模块
            val block3tex: WebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("block3tex")))
            block3tex.click()

            // 找到学生评教入口
            val submot: WebElement = driver.findElement(By.id("submot"))
            submot.click()

            // 等待新标签页打开
            wait.until { driver.windowHandles.size > 1 }
            val newWindowHandle = driver.windowHandles.last()
            driver.switchTo().window(newWindowHandle)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))
            wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
            wait.until { jsExecutor.executeScript("return jQuery.active == 0") }

            var hasNextPage = true
            while (hasNextPage) {
                // 获取 iframe 列表并确保 iframe 存在
                val iframeList = driver.findElements(By.name("iframe0"))
                if (iframeList.isNotEmpty() && iframeList.size > 1) {
                    try {
                        driver.switchTo().frame(iframeList[1]) // 在这个 iframe 中可以找到“评教”按钮和“下页”按钮
                        // 评教按钮
                        val evaluationList: List<WebElement> = driver.findElements(By.linkText("评教"))
                        if (evaluationList.isNotEmpty()) {
                            for (btn in evaluationList) { // 对于每个页面
                                jsExecutor.executeScript("arguments[0].click();", btn)

                                // 等待新标签页打开
                                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))
                                wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
                                wait.until { jsExecutor.executeScript("return jQuery.active == 0") }
                                val newEvaluationWindowHandle = driver.windowHandles.last()
                                driver.switchTo().window(newEvaluationWindowHandle)

                                // 获取 iframe 列表并确保 iframe 存在
                                val newIframeList = driver.findElements(By.name("iframe0"))
                                if (newIframeList.isNotEmpty() && newIframeList.size > 1) {
                                    driver.switchTo().frame(newIframeList[1])

                                    // 用 XPath 查找所有 id 符合pjnr_x_1格式的元素
                                    val elements = driver.findElements(
                                        By.xpath("//*[starts-with(@id, 'pjnr_') and substring(@id, string-length(@id) - 1) = '_1']")
                                    )
                                    // 打印所有找到的元素的 id
                                    for (element in elements) {
                                        println("找到的按钮 id: ${element.getAttribute("id")}")
                                    }
                                    // 遍历点击
                                    for (element in elements) {
                                        wait.until(ExpectedConditions.elementToBeClickable(element))
                                        jsExecutor.executeScript("arguments[0].click();", element)
                                    }

                                    driver.findElement(By.linkText("提交")).click()

                                    val okBtn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("OK")))
                                    jsExecutor.executeScript("arguments[0].click();", okBtn)

                                } else {
                                    println("newIframeList有问题")
                                }
                                driver.switchTo().defaultContent()
                                driver.switchTo().window(newWindowHandle) // 切换回评教总页面
                                driver.switchTo().frame(iframeList[1]) // 在这个 iframe 中可以找到“评教”按钮和“下页”按钮
                            }
                        } else {
                            println("没有找到评教按钮，可能是当前页评教已完成")

                            val nextPageBtn = driver.findElement(By.linkText("下页"))
                            if (nextPageBtn != null) {
                                val onClickValue = nextPageBtn.getAttribute("onclick")
                                if (onClickValue != null && onClickValue.isNotEmpty()) {
                                    jsExecutor.executeScript("arguments[0].click();", nextPageBtn)
                                    driver.switchTo().defaultContent()
                                } else {
                                    hasNextPage = false
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("有问题：${e.message}")
                    } finally {
                        // 切换回主页面
                        driver.switchTo().defaultContent()
                    }
                } else {
                    println("没有找到iframe或iframe数量不足")
                }
            }

        } catch (e: Exception) {
            println("有问题：${e.message}")
        } finally {
            driver.quit()
        }
    }else{
        val logger = Logger.getLogger("EdgeCheck")
        logger.severe("未检测到 Microsoft Edge 浏览器。请确保已安装浏览器。")
    }

}
