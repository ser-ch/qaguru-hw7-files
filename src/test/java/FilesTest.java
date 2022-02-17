import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Configuration;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты на файлы")
class FilesTest {
    @BeforeAll
    static void beforeAll() {
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 60000;
    }

    @Test
    @DisplayName("Когда загружаем файл на сайте, то отображается имя загруженного файла")
    void filenameShouldDisplayedAfterUploadActionFromClasspathTest() {
        open("https://demoqa.com/upload-download");
        $("#uploadFile").uploadFromClasspath("example.txt");

        $("#uploadedFilePath").shouldHave(text("example.txt"));
    }

    @Test
    @DisplayName("Скаченный файл PDF содержит 1 страницу и содержит нужный заголовок")
    void pdfFileDownloadTest() throws IOException {
        open("https://filesamples.com/formats/pdf");
        File pdf = $("a[href$=\"sample2.pdf\"]").scrollIntoView("{block: \"center\"}").download();
        PDF parsedPdf = new PDF(pdf);

        assertThat(parsedPdf.numberOfPages).isEqualTo(1);
        assertThat(parsedPdf.title).contains("PDF Form Example");
    }

    @Test
    @DisplayName("Скачивание XLS файла и проверка, что лист имеет определенное имя и содержит 10 трок")
    void xlsFileDownloadTest() throws IOException {
        open("https://sample-videos.com/download-sample-xls.php");
        File file = $("a[download='SampleXLSFile_19kb.xls']")
                .download();
        Workbook workbook = new HSSFWorkbook(new FileInputStream(file));

        assertThat(workbook.getSheetName(0)).isEqualTo("Sample-spreadsheet-file");
        assertThat(workbook.getSheetAt(0).getLastRowNum()).isEqualTo(9);
    }

    @Test
    @DisplayName("Парсинг csv файла и проверка на количество строк")
    void parseCsvFileTest() throws IOException, CsvException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("addresses.csv");
             Reader reader = new InputStreamReader(is)) {
            CSVReader csvReader = new CSVReader(reader);

            List<String[]> strings = csvReader.readAll();
            assertThat(strings.size()).isEqualTo(6);
        }
    }

    @Test
    @DisplayName("Парсинг ZIP файлов, проверка содержимого")
    void parseZipFileTest() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        ZipFile zipFile = new ZipFile(new File(classLoader.getResource("zipExample.zip").toURI()));

        ZipEntry textEntry = zipFile.getEntry("example.txt");
        try (InputStream is = zipFile.getInputStream(textEntry)) {
            String text = IOUtils.toString(is, StandardCharsets.UTF_8);

            assertThat(text).contains("Hello", "world");
        }
    }
}
