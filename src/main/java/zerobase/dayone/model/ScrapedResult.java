package zerobase.dayone.model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {

    private Company company;

    private List<Dividend> dividenedEntities;

    public ScrapedResult() {
        this.dividenedEntities = new ArrayList<>();
    }
}
