package zerobase.dayone.scraper;

import zerobase.dayone.model.Company;
import zerobase.dayone.model.ScrapedResult;

public interface Scraper {

    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
