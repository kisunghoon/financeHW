package zerobase.dayone.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zerobase.dayone.model.Dividend;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "DIVIDEND")
@Getter
@ToString
@NoArgsConstructor
@Table(
        uniqueConstraints =  {
                @UniqueConstraint(
                        columnNames = {"companyId","date"}
                )
        }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private LocalDate date;

    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend) {
        this.companyId = companyId;
        this.date = LocalDate.from(dividend.getDate());
        this.dividend = dividend.getDividend();
    }
}
