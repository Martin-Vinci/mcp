package com.greybox.mediums.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Table(name = "efris_commodities", indexes = {
        @Index(name = "efris_commodities_un", columnList = "commodity_category_code", unique = true)
})
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EfrisCommodity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commodity_id", nullable = false)
    private Integer commodityId;

    @Column(name = "commodity_category_code", nullable = false, length = 40)
    private String commodityCategoryCode;

    @Column(name = "commodity_category_level", nullable = false, length = 40)
    private String commodityCategoryLevel;

    @Column(name = "commodity_category_name", nullable = false, length = 200)
    private String commodityCategoryName;

    @Column(name = "parent_code", nullable = false, length = 20)
    private String parentCode;
}