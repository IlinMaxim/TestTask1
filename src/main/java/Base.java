import com.codeborne.selenide.*;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;


public class Base {
    public static final String ULR = "http://mragazza.rf.gd/schedule/index";

    static void clickButton(String text) {
        $x("//*[contains(text(), '" + text + "')] | //*[contains(text(), '" + text.toLowerCase() + "')] | //*[contains(text(), '" + text.toUpperCase() + "')]").waitUntil(Condition.visible, 3000).click();
    }

    @Step("Вход в систему пользователем {description}")
    public static void login(String login, String password, String description, SoftAssertions softAssert) {
        Configuration.browserSize = "1920x1080";
        open(ULR);

        $("#loginform-username").setValue(login);
        $("#loginform-password").setValue(password);

        clickButton("Войти");

        softAssert.assertThat($x(".//*[contains(text(), 'Профиль ("+ login +")')]").shouldBe(visible).isDisplayed())
                .as("Вход в систему под пользователем %s не выполнен\r\n", description)
                .isEqualTo(true);
    }

    @Step("Выход из системы")
    public static void logout() {
        $x("//*[contains(text(), 'Выйти')]").waitUntil(Condition.visible, 3000).click();
    }
}
