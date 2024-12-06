package zerobase.dayone.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.dayone.except.impl.AlreadyExistCompanyException;
import zerobase.dayone.except.impl.NoCompanyException;
import zerobase.dayone.model.Company;
import zerobase.dayone.model.ScrapedResult;
import zerobase.dayone.persist.CompanyRepository;
import zerobase.dayone.persist.DividendRepository;
import zerobase.dayone.persist.entity.CompanyEntity;
import zerobase.dayone.persist.entity.DividendEntity;
import zerobase.dayone.scraper.Scraper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {


    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker){

        boolean exists = this.companyRepository.existsByTicker(ticker);

        if(exists){
            throw new AlreadyExistCompanyException();
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker){

        //ticker 기준으로 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);

        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker");
        }
        //해당 회사가 존재하면 회사의 배당금 정보 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        //스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntityList = scrapedResult.getDividenedEntities().stream()
                                                .map(e -> new DividendEntity(companyEntity.getId(),e))
                                                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntityList);

        return company;
    }

    public List<String> getCompanyNamesByKeyWord(String keyWord){

        Pageable limit = PageRequest.of(0,10);

        Page<CompanyEntity> companyEntities =  this.companyRepository.findByNameStartingWithIgnoreCase(keyWord,limit);

        return companyEntities.stream()
                                .map(e-> e.getName())
                                .collect(Collectors.toList());

    }

    public String deleteCompany(String ticker){

        var company = this.companyRepository.findByTicker(ticker)
                        .orElseThrow(() -> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        return company.getName();
    }

}
