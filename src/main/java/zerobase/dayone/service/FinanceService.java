package zerobase.dayone.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.dayone.except.impl.NoCompanyException;
import zerobase.dayone.model.Company;
import zerobase.dayone.model.Dividend;
import zerobase.dayone.model.ScrapedResult;
import zerobase.dayone.model.constants.CacheKey;
import zerobase.dayone.persist.CompanyRepository;
import zerobase.dayone.persist.DividendRepository;
import zerobase.dayone.persist.entity.CompanyEntity;
import zerobase.dayone.persist.entity.DividendEntity;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository DividendRepository;

    @Cacheable(key ="#companyName", value=CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName){

        log.info("search company -> "+companyName);

        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        //2. 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntityList = this.DividendRepository.findAllByCompanyId(company.getId());



        //3. 결과 조합 후 반환

        List<Dividend> dividends = dividendEntityList.stream()
                    .map(e -> new Dividend(e.getDate().atStartOfDay(),e.getDividend()))
                    .collect(Collectors.toList());


        return new ScrapedResult(
                new Company(company.getTicker(), company.getName()),dividends);



    }
}
