import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class Main {
    public static void main(String[] args) {

        // 设置 Chrome 驱动的路径
        System.setProperty("webdriver.chrome.driver", "C:/Program Files/Google/Chrome/Application/chrome.exe");

        // 创建 ChromeDriver 对象
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        // 打开网页
        driver.get("https://ksefile.hpccube.com:65241/efile/share/cG9zdA==&");


    }
}
