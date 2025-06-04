import java.util.*

fun main(args: Array<String>) {
    try {
        // 使用scanner读取账号密码
            val scanner = Scanner(System.`in`)
            print("请输入账号：")
            val username = scanner.nextLine()
            print("请输入密码：")
            val password = scanner.nextLine()
            test(username, password)
    } catch (e: Exception) {
        e.printStackTrace() // 打印堆栈跟踪以帮助调试
    }
}