import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;


public class TakeVacation extends Base {

    SoftAssertions softAssert = new SoftAssertions();

    String saveInformation;
    String deleteInformation;

    public Map<String, String> firstVacation = new HashMap<>();
    public Map<String, String> secondVacation = new HashMap<>();

    @DisplayName("Тест отпусков")
    @Test
    public void testVacationSystem() throws IOException {
        byte[] jsonVacation = Files.readAllBytes(Paths.get("src/main/resources/vacation.json"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode titleNode = mapper.readTree(jsonVacation);
        JsonNode childNode = titleNode.get("data");

        saveInformation = childNode.get("saveInformation").asText();
        deleteInformation = childNode.get("deleteInformation").asText();

        firstVacation = mapper.readValue(String.valueOf(childNode.get("firstVacation")), new TypeReference<Map<String, Object>>() {
        });
        secondVacation = mapper.readValue(String.valueOf(childNode.get("firstVacation")), new TypeReference<Map<String, Object>>() {
        });


        login(User.Manager.getLogin(), User.Manager.getPassword(), User.Manager.getShortDescription(), softAssert);

        addVacation(firstVacation,softAssert);
        addVacation(secondVacation, softAssert);

        editVacation(firstVacation, "10-12-2021", "21-12-2021", softAssert);

        deleteVacation(secondVacation, softAssert);

        softAssert.assertAll();
        logout();


    }


    @Step("Добавление отпуска")
    void addVacation(Map<String, String> vacation, SoftAssertions softAssert) {
        clickButton("Добавить отпуск");

        softAssert.assertThat($(".modal-content").shouldBe(Condition.visible).isDisplayed())
                .as("Модальное окно [Добавление отпуска] не отображается\r\n")
                .isEqualTo(true);

        if ($(".modal-content").isDisplayed()) {
            postVacation(vacation.get("startDate"), vacation.get("endDate"), softAssert);
        }
    }

    @Step("Редактирование отпуска")
    void editVacation(Map<String, String> oldVacation, String newStartDate, String newEndDate, SoftAssertions softAssert) {
        clickButton("Будущие годы");

        String vacationForEditStartDate = oldVacation.get("startDate").replace("-", ".");
        String vacationForEditEndDate = oldVacation.get("endDate").replace("-", ".");

        SelenideElement vacationForEdit = findMyVacation(vacationForEditStartDate, vacationForEditEndDate);

        if (vacationForEdit != null) {
            vacationForEdit.$(".modal-btn[title = Обновить]").click();

            softAssert.assertThat($(".modal-content").shouldBe(Condition.visible).isDisplayed())
                    .as("Модальное окно [Добавление отпуска / Редактирования отпуска] не отображается\r\n")
                    .isEqualTo(true);

            postVacation(newStartDate, newEndDate, softAssert);
        }


    }


    @Step("Ввод данных")
    void postVacation(String startDate, String endDate, SoftAssertions softAssert) {
        $("#schedule-date_start").clear();
        $("#schedule-date_end").clear();

        $("#schedule-date_start").shouldBe(Condition.visible);
        $("#schedule-date_start").setValue(startDate);
        $("#schedule-date_end").setValue(endDate);

        clickButton("Сохранить");

        softAssert.assertThat($(".ajax-result").shouldBe(Condition.visible).isDisplayed())
                .as("Ввод данных об отпуске сохранен\r\n")
                .isEqualTo(true);

        softAssert.assertThat($(".ajax-result").getText())
                .as("Текст подсказки не соответствует ожидаемому\r\n")
                .isEqualTo(saveInformation);

        $(".close").click();
    }

    @Step("Удаление отпуска")
    void deleteVacation(Map<String, String> deleteVacation, SoftAssertions softAssert) {
        String vacationForDeleteStartDate = deleteVacation.get("startDate").replace("-", ".");
        String vacationForDeleteEndDate = deleteVacation.get("endDate").replace("-", ".");

        SelenideElement vacationForDelete = findMyVacation(vacationForDeleteStartDate, vacationForDeleteEndDate);
        vacationForDelete.$(".ajax-btn[title = Удалить]").click();

        softAssert.assertThat(Selenide.switchTo().alert().getText())
                .as("Ошибка информационного сообщения браузера об удалении отпуска\r\n")
                .isEqualTo(deleteInformation);

        Selenide.switchTo().alert().accept();
    }

    @Step("Поиск отпуска")
    SelenideElement findMyVacation(String startDate, String endDate) {
        ElementsCollection rows = $$("tr");

        for (int i = 0; i < rows.size(); i++) {
            SelenideElement currentRow = rows.get(i);
            String currentRowDates = currentRow.getText();
            if (currentRowDates.contains(startDate) && currentRowDates.contains(endDate)) {
                return currentRow;
            }
        }
        return null;
    }
}
