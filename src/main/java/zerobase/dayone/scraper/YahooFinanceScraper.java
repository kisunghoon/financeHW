package zerobase.dayone.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.dayone.model.Company;
import zerobase.dayone.model.Dividend;
import zerobase.dayone.model.ScrapedResult;
import zerobase.dayone.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    private static final String URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
    private static final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final long START_TIME = 86400;

    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    @Override
    public ScrapedResult scrap(Company company) {

        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);
        try {

            long now = System.currentTimeMillis() / 1000;

            String url = String.format(URL,company.getTicker(),START_TIME,now);

            // Jsoup 연결 및 Document 가져오기
            Connection connection = Jsoup.connect(url).userAgent(agent);
            Document doc = connection.get();

            // 테이블의 각 행 가져오기
            Elements rows = doc.select("table.yf-j5d1ld tbody tr");

            List<Dividend> dividends = new ArrayList<Dividend>();

            for (Element row : rows) {
                // 열 데이터 가져오기
                Elements columns = row.select("td");

                // 행의 텍스트 추출
                String txt = row.text();

                // Dividend로 끝나는 행만 출력
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                // 날짜 추출
                String dateText = columns.get(0).text();
                String[] dateParts = dateText.split(" ");

                // 날짜를 month, day, year로 분리
                int month = Month.strToNumber(dateParts[0]);
                int day = Integer.parseInt(dateParts[1].replace(",", ""));
                int year = Integer.parseInt(dateParts[2]);

                // Dividend 값 추출
                String dividend = row.select("span").text();

                if(month <0){
                    throw new RuntimeException("Unexpected Month enum value -> "+ dateParts[0]);
                }

                // 출력
                System.out.printf("%s, Day: %d, Year: %d, Dividend: %s%n", month, day, year, dividend);

                dividends.add(new Dividend(LocalDateTime.of(year,month,day,0,0),dividend));

            }
            scrapedResult.setDividenedEntities(dividends);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL,ticker,START_TIME);

        try {
            Document document = Jsoup.connect(url).userAgent(agent).get();
            Element titleEle = document.getElementsByTag("h1").get(1);
            String title = titleEle.text().trim();

            return new Company(ticker,title);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
