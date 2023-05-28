package ru.privetdruk.restorder.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.privetdruk.restorder.model.entity.TavernEntity;
import ru.privetdruk.restorder.model.enums.Category;
import ru.privetdruk.restorder.service.TavernService;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final TavernService tavernService;

    public static final Map<Category, Map<String, Long>> TAVERNS = new HashMap<>();

    @PostConstruct
    private void loadCache() {
        stream(Category.values())
                .forEach(category -> TAVERNS.put(category, new HashMap<>()));

        Collection<TavernEntity> taverns = tavernService.findAll();
        if (CollectionUtils.isEmpty(taverns)) {
            return;
        }

        Map<Category, Map<String, Long>> groupingTaverns = taverns.stream()
                .filter(tavern -> tavern.getCategory() != null && tavern.getName() != null)
                .collect(groupingBy(
                        TavernEntity::getCategory,
                        toMap(TavernEntity::getName, TavernEntity::getId, (id1, id2) -> {
                            throw new RuntimeException();
                        })
                ));

        TAVERNS.putAll(groupingTaverns);
    }
}
