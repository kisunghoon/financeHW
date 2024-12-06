package zerobase.dayone.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dayone.model.Company;
import zerobase.dayone.model.ScrapedResult;
import zerobase.dayone.model.constants.CacheKey;
import zerobase.dayone.persist.CompanyRepository;
import zerobase.dayone.persist.DividendRepository;
import zerobase.dayone.persist.entity.CompanyEntity;
import zerobase.dayone.persist.entity.DividendEntity;
import zerobase.dayone.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

    //일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE,allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){

        //저장된 회사 목록을 조회

        List<CompanyEntity> companies = this.companyRepository.findAll();

        //회사마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            log.info("Scraping scheduler is started "+company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getTicker(),company.getName()));

            //스크래핑 배당금 정보 중 데이터베이스에 없는 값은 저장

            scrapedResult.getDividenedEntities().stream()
                    .map(e -> new DividendEntity(company.getId(),e))
                    .forEach(e-> {
                        boolean exists =
                                this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate().atStartOfDay());

                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend ->"+e.toString());
                        }
                    });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시 정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();

            }
        }






    }
}
